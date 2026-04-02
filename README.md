# dotCMS OSGi Plugin Examples

[![Build Check](https://github.com/dotcms-community/plugin-examples/actions/workflows/build-check.yml/badge.svg)](https://github.com/dotcms-community/plugin-examples/actions/workflows/build-check.yml)
[![Plugin Installation Test](https://github.com/dotcms-community/plugin-examples/actions/workflows/test-install-plugins.yml/badge.svg)](https://github.com/dotcms-community/plugin-examples/actions/workflows/test-install-plugins.yml)

A collection of working, tested example plugins showing how to extend and customize dotCMS using OSGi bundles. Every plugin in this repo is verified to build and reach ACTIVE state in a live dotCMS container on every push — and automatically tested against every new dotCMS release, making it a reference for how to confidently ship plugins in an evergreen dotCMS environment.

## Plugins

| Plugin | What It Does |
|--------|-------------|
| [`com.dotcms.3rd.party`](com.dotcms.3rd.party) | Package and use a third-party Java library inside a dotCMS OSGi bundle |
| [`com.dotcms.actionlet`](com.dotcms.actionlet) | Add a custom Workflow Actionlet |
| [`com.dotcms.aop`](com.dotcms.aop) | Expose custom Jersey REST endpoints with AOP interceptors |
| [`com.dotcms.app.example`](com.dotcms.app.example) | Create a dotCMS App with secure configuration and change listeners |
| [`com.dotcms.content.validation`](com.dotcms.content.validation) | Validate content at workflow time using a custom Actionlet |
| [`com.dotcms.contenttype`](com.dotcms.contenttype) | Create and remove a custom Content Type programmatically |
| [`com.dotcms.dynamic.skeleton`](com.dotcms.dynamic.skeleton) | Minimal OSGi bundle skeleton with the standard activator lifecycle |
| [`com.dotcms.fixasset`](com.dotcms.fixasset) | Register a custom CMS Maintenance Fix Task |
| [`com.dotcms.hooks`](com.dotcms.hooks) | Attach pre/post Contentlet API hooks |
| [`com.dotcms.hooks.pubsub`](com.dotcms.hooks.pubsub) | Publish cluster-aware Pub/Sub events from a content publish hook |
| [`com.dotcms.hooks.validations`](com.dotcms.hooks.validations) | Pre-checkin validation hooks using a pluggable strategy pattern |
| [`com.dotcms.job`](com.dotcms.job) | Schedule recurring jobs from an OSGi bundle |
| [`com.dotcms.portlet`](com.dotcms.portlet) | Add custom admin portlets to the dotCMS back-end |
| [`com.dotcms.pushpublish.listener`](com.dotcms.pushpublish.listener) | Listen and respond to Push Publish lifecycle events |
| [`com.dotcms.rest`](com.dotcms.rest) | Expose custom Jersey REST endpoints |
| [`com.dotcms.ruleengine.velocityscriptingactionlet`](com.dotcms.ruleengine.velocityscriptingactionlet) | Example Rules Engine actionlet that executes Velocity script |
| [`com.dotcms.ruleengine.visitoripconditionlet`](com.dotcms.ruleengine.visitoripconditionlet) | Example Rules Engine conditionlet that matches visitor IP/CIDR |
| [`com.dotcms.servlet`](com.dotcms.servlet) | Servlet/filter-style request handling via WebInterceptors |
| [`com.dotcms.simpleService`](com.dotcms.simpleService) | Publish a reusable OSGi service for other bundles to consume |
| [`com.dotcms.staticpublish.listener`](com.dotcms.staticpublish.listener) | Hook into Static Push Publish events |
| [`com.dotcms.tuckey`](com.dotcms.tuckey) | Register Tuckey URL rewrite, redirect, and forward rules |
| [`com.dotcms.viewtool`](com.dotcms.viewtool) | Create and register a custom Velocity ViewTool |
| [`com.dotcms.webinterceptor`](com.dotcms.webinterceptor) | Intercept and wrap HTTP requests and responses |

## Getting Started

```sh
git clone https://github.com/dotcms-community/plugin-examples.git
cd plugin-examples
```

Copy the plugin folder closest to what you need, rename it, and update the `artifactId` and package names. Each plugin folder has its own README with details.

## Build

Requires Java 21 and Maven 3.8+.

Build all plugins from the repo root:

```sh
mvn package -DskipTests
```

Or build a single plugin:

```sh
cd com.dotcms.hooks
mvn package
```

The resulting `.jar` in `target/` can be dropped into the `plugins/` directory of any dotCMS instance.

## Installing a Plugin

Drop the built JAR into your dotCMS `plugins/` directory (watched by Felix FileInstall):

```sh
cp target/my-plugin-1.0.jar /path/to/dotcms/plugins/
```

dotCMS will hot-deploy the bundle automatically. To verify it activated:

```
GET /api/v1/osgi
```

Look for your bundle's `symbolicName` with `"state": 32` (ACTIVE).

## CI

Every push and pull request runs two checks:

| Workflow | What It Does |
|----------|-------------|
| **Build Check** | Compiles all plugins with Java 21 / Maven |
| **Plugin Installation Test** | Starts a `dotcms/dotcms-dev:nightly` container, installs all JARs, and asserts every bundle reaches OSGi ACTIVE state (32) |

Results are posted as a step summary on each Actions run.

## Evergreen Compatibility

dotCMS ships releases continuously. This repo automatically stays current with them:

- Every 4 hours, a workflow polls `dotcms/core` for new releases
- When a new version is detected, all plugins are built and tested against the matching `dotcms/dotcms-dev` image
- If tests pass, a pull request is opened automatically bumping `dotcms-core.version` in the parent `pom.xml`
- The last verified dotCMS version is tracked as a repository variable so checks are skipped when nothing has changed

This gives plugin developers a continuously-validated reference for what works — and when something breaks, it's caught within hours of a dotCMS release.

## Documentation

- [dotCMS Plugin Documentation](https://dotcms.com/docs/latest/plugins)
- [dotCMS Public Plugin Repository](https://github.com/dotcms-plugins)
