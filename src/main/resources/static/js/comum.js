// Funcoes compartilhadas pelas telas do cidadao e do administrador.

/** Como cada estado aparece na tela: sempre icone + texto + cor (US07, rotulos do Agile). */
const ESTADOS = {
    EM_PLANEJAMENTO: { texto: "Em planejamento", icone: "○", cor: "azul" },
    EM_PROGRESSO: { texto: "Em andamento", icone: "◔", cor: "amarelo" },
    ATINGIDA: { texto: "Concluída", icone: "✔", cor: "verde" },
    VIROU_META: { texto: "Virou meta oficial", icone: "★", cor: "roxo" },
    NAO_VIROU_META: { texto: "Não entrou no plano da prefeitura", icone: "–", cor: "cinza" }
};

/** Mensagens de regionalizacao (US06). */
const REGIONALIZACAO = {
    REGIONALIZADA: "Esta meta tem regiões definidas.",
    NAO_REGIONALIZAVEL: "Esta meta vale para a cidade toda e não é dividida por região.",
    NAO_DIVULGADA: "Atenção: a regionalização desta meta ainda não foi divulgada pela prefeitura.",
    SEM_REGIAO: "Esta meta ainda está sem região definida."
};

/** Escapa texto antes de colocar no HTML (evita injecao de codigo). */
function esc(valor) {
    return String(valor ?? "").replace(/[&<>"']/g, c => (
        { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}

/** Chama a API e devolve o JSON; em erro lanca Error com a mensagem da API. */
async function api(caminho, opcoes = {}) {
    const resp = await fetch(caminho, {
        ...opcoes,
        headers: { "Content-Type": "application/json", ...(opcoes.headers || {}) }
    });
    if (resp.status === 204) return null;
    const corpo = await resp.json().catch(() => null);
    if (!resp.ok) {
        let msg = corpo?.message || ("Erro " + resp.status);
        if (corpo?.campos?.length) msg += " " + corpo.campos.map(c => c.mensagem).join("; ");
        const erro = new Error(msg);
        erro.status = resp.status;
        throw erro;
    }
    return corpo;
}

/** Selo de estado a partir do codigo (ex.: "EM_PROGRESSO"). */
function selo(codigo) {
    const e = ESTADOS[codigo] || { texto: codigo, icone: "?", cor: "cinza" };
    return `<span class="estado cor-${e.cor}">
              <span class="icone" aria-hidden="true">${e.icone}</span>${esc(e.texto)}
            </span>`;
}

/** No cartao: o status da meta, ou "Nao entrou no plano". */
function seloDaPromessa(p) {
    return p.meta ? selo(p.meta.status) : selo("NAO_VIROU_META");
}

function fonteHtml(rotulo, fonte) {
    // se a fonte for um endereco http(s), vira link (US05: "link da fonte"); senao, texto simples
    const valor = /^https?:\/\/\S+$/i.test(fonte)
        ? `<a href="${esc(fonte)}" target="_blank" rel="noopener">${esc(fonte)}</a>` : esc(fonte);
    return `<div class="fonte"><b>${esc(rotulo)}:</b> ${valor}</div>`;
}

function formatarNumero(n) {
    return Number(n).toLocaleString("pt-BR", { maximumFractionDigits: 2 });
}