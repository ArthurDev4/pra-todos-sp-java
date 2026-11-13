"""Converte os diagramas Mermaid de docs/diagramas/*.mmd em PNG (para o PDF).

Uso (na raiz do projeto, com o Edge instalado):
    uv run --no-project --with playwright python docs/ferramentas/gerar_diagramas.py
"""
import json
from pathlib import Path

from playwright.sync_api import sync_playwright

PASTA = Path(__file__).resolve().parents[1] / "diagramas"
MERMAID = "https://cdn.jsdelivr.net/npm/mermaid@11/dist/mermaid.min.js"

HTML = """<!DOCTYPE html><html><head><meta charset="utf-8">
<script src="%s"></script>
<style>body{margin:0;background:#fff;font-family:Arial} #d{display:inline-block;padding:16px}</style>
</head><body><div id="d"></div><script>
mermaid.initialize({startOnLoad:false, theme:'neutral', securityLevel:'strict',
  themeVariables:{fontSize:'15px'}, class:{useMaxWidth:false}, er:{useMaxWidth:false}});
mermaid.render('g', %s).then(r => {
    const d = document.getElementById('d');
    d.innerHTML = r.svg;
    const svg = d.querySelector('svg');
    const vb = svg.viewBox.baseVal;               // tamanho real do desenho
    svg.style.maxWidth = 'none';
    svg.setAttribute('width', vb.width * 1.4);
    svg.setAttribute('height', vb.height * 1.4);
    document.title = 'ok'; })
  .catch(e => { document.title = 'erro: ' + e.message; });
</script></body></html>"""

with sync_playwright() as p:
    nav = p.chromium.launch(channel="msedge")
    pg = nav.new_page(device_scale_factor=2, viewport={"width": 4000, "height": 3000})
    for mmd in sorted(PASTA.glob("*.mmd")):
        pg.set_content(HTML % (MERMAID, json.dumps(mmd.read_text(encoding="utf-8"))))
        pg.wait_for_function("document.title !== ''", timeout=30000)
        if pg.title() != "ok":
            raise SystemExit(f"{mmd.name}: {pg.title()}")
        pg.locator("#d").screenshot(path=str(mmd.with_suffix(".png")))
        print("ok", mmd.with_suffix(".png").name)
    nav.close()
