#!/usr/bin/env bash
# test-install-plugins.sh
# Starts dotcms/dotcms-dev:nightly, installs all built plugin JARs,
# and verifies each bundle is registered in the OSGi runtime via the REST API.
#
# Usage: .github/bin/test-install-plugins.sh [plugins-dir]
#   plugins-dir  directory containing *.jar files to install (default: /tmp/dotcms-plugins)
#
# Environment (all optional):
#   DOTCMS_IMAGE   Docker image to use            (default: dotcms/dotcms-dev:nightly)
#   DOTCMS_PORT    HTTP port to poll               (default: 8082)
#   STARTUP_WAIT   Max seconds to wait for startup (default: 300)

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

PLUGINS_DIR="${1:-/tmp/dotcms-plugins}"
DOTCMS_IMAGE="${DOTCMS_IMAGE:-dotcms/dotcms-dev:nightly}"
DOTCMS_PORT="${DOTCMS_PORT:-8082}"
STARTUP_WAIT="${STARTUP_WAIT:-300}"
CONTAINER_NAME="dotcms-plugin-test"

OSGI_CONF="${SCRIPT_DIR}/osgi-extra.conf"

# Paths inside the container
CONTAINER_PLUGINS_DIR="/srv/dotserver/plugins"
CONTAINER_OSGI_CONF="/data/shared/assets/server/osgi/osgi-extra.conf"

DOTCMS_BASE_URL="http://localhost:${DOTCMS_PORT}"
DOTCMS_AUTH="admin@dotcms.com:admin"

# ── Helpers ───────────────────────────────────────────────────────────────────

log()  { echo "[$(date '+%H:%M:%S')] $*"; }
fail() { echo "[ERROR] $*" >&2; exit 1; }

cleanup() {
  if docker ps -q -f name="${CONTAINER_NAME}" | grep -q .; then
    log "Stopping container ${CONTAINER_NAME}..."
    docker rm -f "${CONTAINER_NAME}" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

# ── Collect JARs ──────────────────────────────────────────────────────────────

log "Collecting plugin JARs into ${PLUGINS_DIR}..."
rm -rf "${PLUGINS_DIR}"
mkdir -p "${PLUGINS_DIR}"

find "${REPO_ROOT}" \
  -path "*/target/*.jar" \
  ! -path "*/target/classes/*" \
  ! -name "*-sources.jar" \
  ! -name "*-javadoc.jar" \
  -exec cp -v {} "${PLUGINS_DIR}/" \;

JAR_COUNT=$(find "${PLUGINS_DIR}" -name "*.jar" | wc -l | tr -d ' ')
[[ "${JAR_COUNT}" -gt 0 ]] || fail "No plugin JARs found in ${PLUGINS_DIR}"
log "Found ${JAR_COUNT} plugin JAR(s) to install."

# ── Start dotCMS ──────────────────────────────────────────────────────────────

docker rm -f "${CONTAINER_NAME}" >/dev/null 2>&1 || true

log "Pulling ${DOTCMS_IMAGE}..."
docker pull "${DOTCMS_IMAGE}"

log "Starting dotCMS container..."
docker run -d --rm \
  --name "${CONTAINER_NAME}" \
  -p 8082:8082 \
  -p 8443:8443 \
  -e DOT_INITIAL_ADMIN_PASSWORD=admin \
  -e DOT_FELIX_FELIX_FILEINSTALL_DIR="${CONTAINER_PLUGINS_DIR}" \
  -e DOT_START_CLIENT_OSGI_IN_SEPARATE_THREAD="false" \
  -e CMS_JAVA_OPTS="-Xmx1g -Xms512m" \
  -v "${PLUGINS_DIR}:${CONTAINER_PLUGINS_DIR}" \
  -v "${OSGI_CONF}:${CONTAINER_OSGI_CONF}" \
  "${DOTCMS_IMAGE}"

# ── Wait for health ───────────────────────────────────────────────────────────

HEALTH_URL="${DOTCMS_BASE_URL}/api/v1/probes/ready"
log "Waiting up to ${STARTUP_WAIT}s for dotCMS at ${HEALTH_URL}..."

ELAPSED=0
INTERVAL=10
until curl -sf "${HEALTH_URL}" >/dev/null 2>&1; do
  if [[ "${ELAPSED}" -ge "${STARTUP_WAIT}" ]]; then
    log "=== dotCMS container logs (last 100 lines) ==="
    docker logs "${CONTAINER_NAME}" 2>&1 | tail -100
    fail "dotCMS did not become healthy within ${STARTUP_WAIT}s"
  fi
  log "  not ready yet (${ELAPSED}s elapsed), retrying in ${INTERVAL}s..."
  sleep "${INTERVAL}"
  ELAPSED=$((ELAPSED + INTERVAL))
done
log "dotCMS is healthy after ${ELAPSED}s."

# ── Collect Bundle-SymbolicNames from JARs ────────────────────────────────────

declare -A JAR_TO_SYMBOLIC  # jar filename -> symbolic name
for jar in "${PLUGINS_DIR}"/*.jar; do
  # Use python3 to correctly parse multi-line MANIFEST.MF continuation values
  SN=$(unzip -p "${jar}" META-INF/MANIFEST.MF 2>/dev/null | python3 -c "
import sys
manifest = {}
current_key = None
for raw in sys.stdin:
    line = raw.rstrip('\r\n')
    if line.startswith(' ') and current_key:
        manifest[current_key] += line[1:]
    elif ':' in line:
        k, _, v = line.partition(':')
        current_key = k.strip()
        manifest[current_key] = v.strip()
sn = manifest.get('Bundle-SymbolicName', '')
print(sn.split(';')[0].strip())
")
  if [[ -n "${SN}" ]]; then
    JAR_TO_SYMBOLIC["$(basename "${jar}")"]="${SN}"
  else
    log "WARN  $(basename "${jar}") — no Bundle-SymbolicName in manifest, skipping"
  fi
done

# ── Verify plugins via OSGi REST API ──────────────────────────────────────────

log "Fetching OSGi bundle list from REST API..."
OSGI_RESPONSE=$(curl -sf \
  -u "${DOTCMS_AUTH}" \
  "${DOTCMS_BASE_URL}/api/v1/osgi") \
  || fail "OSGi REST API call failed"

# OSGi bundle states:
#  1 = UNINSTALLED  32 = ACTIVE  (anything < 32 is a failure)

FAILURES=0

log "=== Bundle report ==="
printf "  %-10s  %-5s  %s\n" "STATUS" "STATE" "SYMBOLIC NAME"
printf "  %-10s  %-5s  %s\n" "----------" "-----" "-------------"

# Collect results into an array so we can sort: failures first
declare -a ROWS
declare -a SUMMARY_ROWS

for jar in "${!JAR_TO_SYMBOLIC[@]}"; do
  SN="${JAR_TO_SYMBOLIC[$jar]}"

  STATE=$(echo "${OSGI_RESPONSE}" | python3 -c "
import sys, json
data = json.load(sys.stdin)
for b in data.get('entity', []):
    if b.get('symbolicName') == '${SN}':
        print(b.get('state', -1))
        break
" 2>/dev/null || echo "")

  if [[ "${STATE}" == "32" ]]; then
    ROWS+=("0|  ACTIVE      32     ${SN}")
    SUMMARY_ROWS+=("0| ✅ ACTIVE | \`${SN}\` |")
  elif [[ -z "${STATE}" ]]; then
    ROWS+=("1|  FAIL        -      ${SN}  (not found in OSGi runtime)")
    SUMMARY_ROWS+=("1| ❌ not found | \`${SN}\` |")
    FAILURES=$((FAILURES+1))
  else
    STATE_LABEL="INACTIVE"
    [[ "${STATE}" == "4" ]]  && STATE_LABEL="RESOLVED"
    [[ "${STATE}" == "2" ]]  && STATE_LABEL="INSTALLED"
    [[ "${STATE}" == "1" ]]  && STATE_LABEL="UNINSTALLED"
    ROWS+=("1|  FAIL        ${STATE}      ${SN}  (${STATE_LABEL} — expected ACTIVE/32)")
    SUMMARY_ROWS+=("1| ❌ ${STATE_LABEL} | \`${SN}\` |")
    FAILURES=$((FAILURES+1))
  fi
done

# Print failures first, then passing
printf '%s\n' "${ROWS[@]}" | sort -t'|' -k1 -r | cut -d'|' -f2

# ── Summary ───────────────────────────────────────────────────────────────────

echo ""
PASSING=$((${#JAR_TO_SYMBOLIC[@]} - FAILURES))
log "=== Summary: ${PASSING}/${#JAR_TO_SYMBOLIC[@]} plugins ACTIVE ==="

# ── GitHub Actions Step Summary ───────────────────────────────────────────────

if [[ -n "${GITHUB_STEP_SUMMARY:-}" ]]; then
  {
    if [[ "${FAILURES}" -eq 0 ]]; then
      echo "## ✅ Plugin Installation: ${PASSING}/${#JAR_TO_SYMBOLIC[@]} ACTIVE"
    else
      echo "## ❌ Plugin Installation: ${PASSING}/${#JAR_TO_SYMBOLIC[@]} ACTIVE"
    fi
    echo ""
    echo "| Status | Symbolic Name |"
    echo "|--------|---------------|"
    # failures first, then passing
    printf '%s\n' "${SUMMARY_ROWS[@]}" | sort -t'|' -k1 -r | cut -d'|' -f2-
  } >> "${GITHUB_STEP_SUMMARY}"
fi

if [[ "${FAILURES}" -gt 0 ]]; then
  echo ""
  log "=== Missing packages per failing plugin ==="
  FULL_LOGS=$(docker logs "${CONTAINER_NAME}" 2>&1)

  # Dump all exception/error lines for debugging state-4 bundles
  STATE4_PRESENT=0
  for jar in "${!JAR_TO_SYMBOLIC[@]}"; do
    STATE=$(echo "${OSGI_RESPONSE}" | python3 -c "
import sys,json
data=json.load(sys.stdin)
for b in data.get('entity',[]):
    if b.get('symbolicName')=='${JAR_TO_SYMBOLIC[$jar]}':
        print(b.get('state',-1)); break
" 2>/dev/null || echo "")
    [[ "${STATE}" == "4" ]] && STATE4_PRESENT=1 && break
  done
  if [[ "${STATE4_PRESENT}" == "1" ]]; then
    echo ""
    echo "  --- OSGi activation errors from container logs ---"
    echo "${FULL_LOGS}" | python3 -c "
import sys, re
lines = sys.stdin.readlines()
output = []
i = 0
while i < len(lines) and len(output) < 80:
    line = lines[i]
    if re.search(r'BundleException|Activator start error|Error activating|activator start|start error', line):
        block = [line.rstrip()]
        j = i + 1
        while j < len(lines) and j < i + 20:
            next_line = lines[j].rstrip()
            if re.match(r'\s*(Caused by:|at |\.\.\.)', next_line):
                block.append(next_line)
                j += 1
            else:
                break
        output.extend(block)
        i = j
    else:
        i += 1
print('\n'.join(output))
" | sed 's/^/  /'
    echo "  ---"
  fi

  for jar in "${!JAR_TO_SYMBOLIC[@]}"; do
    SN="${JAR_TO_SYMBOLIC[$jar]}"
    STATE=$(echo "${OSGI_RESPONSE}" | python3 -c "
import sys, json
data = json.load(sys.stdin)
for b in data.get('entity', []):
    if b.get('symbolicName') == '${SN}':
        print(b.get('state', -1))
        break
" 2>/dev/null || echo "")

    if [[ "${STATE}" != "32" ]]; then
      echo ""
      echo "  Plugin: ${SN}"
      # Extract missing package requirements from Felix resolve errors
      MISSING=$(echo "${FULL_LOGS}" \
        | grep -i "unable to resolve\|missing requirement\|Cannot find a solution" \
        | grep "${SN}" \
        | grep -oE "osgi\.wiring\.package=[^)>]+" \
        | sed 's/osgi\.wiring\.package=//' \
        | sort -u)
      if [[ -n "${MISSING}" ]]; then
        echo "  Missing packages:"
        echo "${MISSING}" | sed 's/^/    /'
      elif [[ "${STATE}" == "4" ]]; then
        # RESOLVED but activator failed — extract stack trace from logs
        ACTIVATION_ERR=$(echo "${FULL_LOGS}" | python3 -c "
import sys, re
lines = sys.stdin.readlines()
sn = '${SN}'
output = []
i = 0
while i < len(lines):
    line = lines[i]
    # Find the BundleException line that mentions this bundle
    if ('BundleException' in line or 'Activator start error' in line) and sn in line:
        # Collect this line plus following stack trace lines
        block = [line.rstrip()]
        j = i + 1
        while j < len(lines) and j < i + 30:
            next_line = lines[j].rstrip()
            if re.match(r'\s*(Caused by:|at |\.\.\.)', next_line) or next_line.strip() == '':
                block.append(next_line)
                j += 1
            else:
                break
        output.extend(block)
        i = j
    else:
        i += 1
print('\n'.join(output[:50]))
" 2>/dev/null)
        if [[ -n "${ACTIVATION_ERR}" ]]; then
          echo "  Activator error:"
          echo "${ACTIVATION_ERR}" | sed 's/^/    /'
        else
          # Fallback: show any log lines mentioning this bundle near an error
          FALLBACK=$(echo "${FULL_LOGS}" | grep -i "${SN}" | grep -iE "error|exception|fail|warn" | head -10)
          if [[ -n "${FALLBACK}" ]]; then
            echo "  Relevant log lines:"
            echo "${FALLBACK}" | sed 's/^/    /'
          else
            echo "  (RESOLVED but activator failed — no matching log lines found for ${SN})"
          fi
        fi
      else
        echo "  (no errors found in logs — state=${STATE:-not found})"
      fi
    fi
  done

  echo ""
  fail "${FAILURES} plugin(s) did not reach ACTIVE state (32)."
fi

log "All ${JAR_COUNT} plugins verified ACTIVE."
