"""The Short's frame-0 image: the real failure report from evidence/logs/constructor-cycle.log (only the jar paths are
shortened), drawn as a light terminal card at 1080x1920. Runs in the Playwright Python image:
  docker run --rm -v "<lab>:/lab" mcr.microsoft.com/playwright/python:v1.48.0-jammy \
    sh -c "pip install -q playwright==1.48.0; python3 /lab/video/frame0.py"
"""
import html, re
from playwright.sync_api import sync_playwright

log = open('/lab/evidence/logs/constructor-cycle.log', encoding='utf8').read()
start = log.index('APPLICATION FAILED TO START')
end = log.index('Action:')
block = log[start:end].rstrip().splitlines()
block = [re.sub(r'defined in URL \[[^\]]*/([A-Za-z]+\.class)\]', r'defined in URL […/\1]', l) for l in block]
block = [l for l in block if l.strip() and not l.startswith('*')]
text = '\n'.join(block)
# the headline is long: wrap it at the word nearest the middle (display only; the words are unchanged)
head = 'The dependencies of some of the beans in the application context form a cycle:'
text = text.replace(head, 'The dependencies of some of the beans\nin the application context form a cycle:')

page = f"""<!doctype html><html><head><meta charset="utf-8"><style>
body{{margin:0;width:1080px;height:1920px;background:#f6f7f9;display:flex;align-items:center;justify-content:center;
font-family:"DejaVu Sans Mono",Consolas,monospace}}
.card{{width:960px;background:#fff;border:3px solid #d8dde5;border-radius:24px;padding:48px 44px;box-shadow:0 8px 30px #0001}}
.tag{{font:600 30px/1.3 "DejaVu Sans",sans-serif;color:#5b6678;margin-bottom:22px}}
pre{{margin:0;font-size:31px;line-height:1.45;color:#1a2230;white-space:pre-wrap}}
.red{{color:#b42318;font-weight:700}}
</style></head><body><div class="card"><div class="tag">Spring Boot 4.1.1 · real console output</div>
<pre>{html.escape(text).replace('APPLICATION FAILED TO START', '<span class="red">APPLICATION FAILED TO START</span>')}</pre></div></body></html>"""

with sync_playwright() as p:
    b = p.chromium.launch()
    pg = b.new_page(viewport={'width': 1080, 'height': 1920})
    pg.set_content(page)
    pg.screenshot(path='/lab/video/short-frame0.png')
    b.close()
print(text)
