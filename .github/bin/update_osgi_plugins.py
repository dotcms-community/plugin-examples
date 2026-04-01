import sys
import re

plugin_file = sys.argv[1]
replacing_text = sys.argv[2]

with open(plugin_file, 'r') as fr:
  content = fr.read()
  content_new = re.sub(r'<dotcms-core.version>\d{2}\.\d{2}(\.\d{1,2})*(-\d+)?</dotcms-core.version>', "<dotcms-core.version>{0}</dotcms-core.version>".format(replacing_text), content, flags = re.M)
  fr.close()

with open(plugin_file, 'w') as fw:
  fw.write(content_new)
  fw.close()
