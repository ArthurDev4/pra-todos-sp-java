// Telas do cidadao (sem login): 1 - escolha do bairro, 2 - termometro do bairro, 3 - detalhe.
const conteudo = document.getElementById("conteudo");

const LEGENDA = `
  <div class="legenda" aria-label="Legenda dos estados">
    ${selo("ATINGIDA")} ${selo("EM_PROGRESSO")} ${selo("EM_PLANEJAMENTO")} ${selo("NAO_VIROU_META")}
  </div>`;

// ------------------------------------------------------------------ Tela 1
async function telaBairros() {
    const regioes = await api("/subprefeituras");
    conteudo.innerHTML = `
      <h2>O que prometeram para o seu bairro?</h2>
      <p class="subtitulo">Escolha a sua região para ver as promessas de campanha e o que virou meta da prefeitura.</p>
      <div class="grade-bairros">
        ${regioes.map(r => `
          <button class="botao-bairro" data-id="${r.id}">
            ${esc(r.nome)}
            <small>Ver promessas da região</small>
          </button>`).join("")}
      </div>`;
    conteudo.querySelectorAll(".botao-bairro").forEach(b =>
        b.addEventListener("click", () => location.hash = "#/bairro/" + b.dataset.id));
}

// ------------------------------------------------------------------ Tela 2
function cartao(p, bairro) {
    return `
      <a class="cartao" href="#/promessa/${p.id}?bairro=${bairro}">
        ${p.meta ? `<span class="etiqueta">${esc(p.meta.eixo)}</span>` : ""}
        <p class="texto">${esc(p.texto)}</p>
        ${seloDaPromessa(p)}
        ${p.meta && p.meta.percentual !== null ? `<span class="etiqueta">${formatarNumero(p.meta.percentual)}% da meta</span>` : ""}
        ${fonteHtml("Fonte", FONTE_PROMESSA)}
      </a>`;
}

async function telaTermometro(id) {
    const t = await api("/subprefeituras/" + id + "/termometro");
    const total = t.promessas.length + t.naoViraramMeta.length;
    conteudo.innerHTML = `
      <a class="voltar" href="#/">← Trocar de bairro</a>
      <h2>Promessas para ${esc(t.subprefeitura.nome)}</h2>
      <div class="resumo">
        <div><strong>${total}</strong> promessas</div>
        <div><strong>${t.promessas.length}</strong> viraram meta oficial</div>
        <div><strong>${t.naoViraramMeta.length}</strong> não entraram no plano</div>
      </div>
      <div class="filtro-area" aria-disabled="true">
        Filtro por área:
        <select disabled aria-label="Filtro por área, indisponível"><option>Todas</option></select>
        <em>em breve — fora do MVP (US12)</em>
      </div>
      ${LEGENDA}
      <h3>Viraram meta oficial</h3>
      ${t.promessas.length ? t.promessas.map(p => cartao(p, id)).join("") : "<p>Nenhuma promessa desta região virou meta.</p>"}
      <h3>Não entraram no plano da prefeitura</h3>
      ${t.naoViraramMeta.length ? t.naoViraramMeta.map(p => cartao(p, id)).join("") : "<p>Nenhuma.</p>"}`;
}

// ------------------------------------------------------------------ Tela 3
async function telaDetalhe(id, bairro) {
    const p = await api("/promessas/" + id);
    const m = p.meta;
    let blocoMeta;
    if (!m) {
        blocoMeta = `
          <div class="alerta cinza">– Esta promessa <u>não entrou no plano da prefeitura</u>:
          não existe meta oficial ligada a ela.</div>`;
    } else {
        const [metaCompleta, regioes] = await Promise.all([api("/metas/" + m.id), api("/subprefeituras")]);
        const nomes = metaCompleta.subprefeituras.map(sid => regioes.find(r => r.id === sid)?.nome).filter(Boolean);
        const progresso = m.percentual !== null ? `
            <p class="progresso-texto">${formatarNumero(m.valorAtual)} de ${formatarNumero(m.valorAlvo)}
               (${formatarNumero(m.percentual)}%)</p>
            <div class="barra" role="progressbar" aria-valuemin="0" aria-valuemax="100"
                 aria-valuenow="${Math.min(100, m.percentual)}"
                 aria-label="Progresso da meta"><span style="width:${Math.min(100, m.percentual)}%"></span></div>`
            : `<p>Esta meta ainda não tem resultado numérico divulgado.</p>`;
        const semRegiao = m.regionalizacao === "REGIONALIZADA" && nomes.length === 0;
        const aviso = semRegiao ? REGIONALIZACAO.SEM_REGIAO : REGIONALIZACAO[m.regionalizacao];
        const alerta = semRegiao || m.regionalizacao === "NAO_DIVULGADA";
        blocoMeta = `
          <div class="cartao">
            ${selo("VIROU_META")}
            <h3 style="margin-top:.6rem">Virou a meta oficial nº ${m.numero}</h3>
            <p>${esc(m.texto)}</p>
            <p><b>Eixo do Programa de Metas:</b> ${esc(m.eixo)}</p>
            <p><b>Status:</b> ${selo(m.status)}</p>
            ${progresso}
            ${nomes.length ? `<p><b>Regiões afetadas:</b> ${nomes.map(esc).join(", ")}</p>` : ""}
            <div class="alerta ${alerta ? "" : "info"}">${alerta ? "⚠ " : "ℹ "}${esc(aviso)}</div>
            ${fonteHtml("Fonte da meta", FONTE_META)}
          </div>`;
    }
    conteudo.innerHTML = `
      <a class="voltar" href="${bairro ? "#/bairro/" + esc(bairro) : "#/"}">← Voltar para a lista</a>
      <h2>Detalhe da promessa</h2>
      <div class="cartao">
        <p class="texto">${esc(p.texto)}</p>
        ${fonteHtml("Fonte da promessa", FONTE_PROMESSA)}
      </div>
      ${blocoMeta}`;
}

// ------------------------------------------------------------------ rotas
async function rotear() {
    const [caminho, query] = location.hash.replace(/^#/, "").split("?");
    const partes = caminho.split("/").filter(Boolean);
    const params = new URLSearchParams(query || "");
    conteudo.innerHTML = '<p class="carregando">Carregando...</p>';
    try {
        if (partes[0] === "bairro" && partes[1]) await telaTermometro(partes[1]);
        else if (partes[0] === "promessa" && partes[1]) await telaDetalhe(partes[1], params.get("bairro"));
        else await telaBairros();
        window.scrollTo(0, 0);
    } catch (e) {
        conteudo.innerHTML = `<p class="erro">Não foi possível carregar: ${esc(e.message)}</p>
                              <a class="voltar" href="#/">← Voltar ao início</a>`;
    }
}
window.addEventListener("hashchange", rotear);
rotear();
