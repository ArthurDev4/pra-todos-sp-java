// Tela 4 - Administrador: login (US08), promessas (US09, US11) e metas (US10, UC11).
const $ = id => document.getElementById(id);
let token = sessionStorage.getItem("token");
let regioes = [];
let metas = [];
let eixos = [];

function apiAdmin(caminho, opcoes = {}) {
    return api(caminho, { ...opcoes, headers: { Authorization: "Bearer " + token } }).catch(e => {
        if (e.status === 401) sair();
        throw e;
    });
}

function mensagem(id, texto, ok) {
    const el = $(id);
    el.textContent = texto;
    el.className = "mensagem " + (ok ? "ok" : "falha");
}

// ------------------------------------------------------------------ login
$("form-login").addEventListener("submit", async ev => {
    ev.preventDefault();
    try {
        const r = await api("/login", {
            method: "POST",
            body: JSON.stringify({ email: $("login-email").value, senha: $("login-senha").value })
        });
        token = r.token;
        sessionStorage.setItem("token", token);
        sessionStorage.setItem("nome", r.admin.nome);
        await entrar();
    } catch (e) {
        mensagem("msg-login", e.message, false);
    }
});

function sair() {
    token = null;
    sessionStorage.clear();
    $("tela-admin").classList.add("oculto");
    $("tela-login").classList.remove("oculto");
    $("sair").classList.add("oculto");
    $("usuario-logado").textContent = "";
}
$("sair").addEventListener("click", sair);

async function entrar() {
    $("usuario-logado").textContent = "Olá, " + (sessionStorage.getItem("nome") || "administrador") + " · ";
    $("tela-login").classList.add("oculto");
    $("tela-admin").classList.remove("oculto");
    $("sair").classList.remove("oculto");
    regioes = await api("/subprefeituras");
    eixos = await api("/eixos");
    await carregarMetas();
    await carregarPromessas();
}

// ------------------------------------------------------------------ abas
document.querySelectorAll("[data-aba]").forEach(b => b.addEventListener("click", () => {
    document.querySelectorAll("[data-aba]").forEach(x => x.setAttribute("aria-selected", String(x === b)));
    $("aba-promessas").classList.toggle("oculto", b.dataset.aba !== "promessas");
    $("aba-metas").classList.toggle("oculto", b.dataset.aba !== "metas");
}));

// ------------------------------------------------------------------ apoio
const caixasRegioes = () => regioes.map(r =>
    `<label><input type="checkbox" value="${r.id}"> ${esc(r.nome)}</label>`).join("");
const idsMarcados = id => [...$(id).querySelectorAll("input:checked")].map(c => Number(c.value));
const marcar = (id, ids) => {
    const texto = ids.map(String);
    $(id).querySelectorAll("input").forEach(c => c.checked = texto.includes(c.value));
};

function preencherSelects() {
    $("p-meta").innerHTML = '<option value="">Sem meta — não entrou no plano da prefeitura</option>' +
        metas.map(m => `<option value="${m.id}">Meta ${m.numero} — ${esc(m.texto)}</option>`).join("");
    $("p-regioes").innerHTML = caixasRegioes();
    $("m-regioes").innerHTML = caixasRegioes();
    $("m-eixo").innerHTML = eixos.map(e => `<option value="${e.id}">${esc(e.nome)}</option>`).join("");
    atualizarRegioesBox();
    atualizarRegioesPromessa();
}

// ------------------------------------------------------------------ promessas
async function carregarPromessas() {
    const lista = await api("/promessas");
    $("lista-promessas").innerHTML = lista.map(p => `
      <tr>
        <td>${p.id}</td><td>${esc(p.texto)}</td>
        <td>${p.meta ? "Meta " + p.meta.numero + " · " + selo(p.meta.status) : selo("NAO_VIROU_META")}</td>
        <td>
          <button class="botao secundario pequeno" data-editar-promessa="${p.id}">Editar</button>
          <button class="botao perigo pequeno" data-excluir-promessa="${p.id}">Excluir</button>
        </td>
      </tr>`).join("");
    document.querySelectorAll("[data-editar-promessa]").forEach(b => b.addEventListener("click", async () =>
        editarPromessa(await api("/promessas/" + b.dataset.editarPromessa))));
    document.querySelectorAll("[data-excluir-promessa]").forEach(b => b.addEventListener("click", () =>
        excluir("/promessas/" + b.dataset.excluirPromessa, "msg-promessa", carregarPromessas)));
}

/** Promessa com meta herda a regiao da meta: as caixas so valem para promessa sem meta. */
function atualizarRegioesPromessa() {
    const semMeta = $("p-meta").value === "";
    $("p-regioes-box").disabled = !semMeta;
    if (!semMeta) $("p-regioes").querySelectorAll("input").forEach(c => c.checked = false);
}
$("p-meta").addEventListener("change", atualizarRegioesPromessa);

function editarPromessa(p) {
    $("titulo-promessa").textContent = "Editar promessa #" + p.id;
    $("p-id").value = p.id;
    $("p-texto").value = p.texto;
    $("p-meta").value = p.meta ? p.meta.id : "";
    atualizarRegioesPromessa();
    marcar("p-regioes", p.subprefeituras);
    $("p-texto").focus();
}

$("form-promessa").addEventListener("reset", () => {
    $("titulo-promessa").textContent = "Cadastrar promessa";
    $("p-id").value = "";
    setTimeout(atualizarRegioesPromessa);
});

$("form-promessa").addEventListener("submit", async ev => {
    ev.preventDefault();
    const id = $("p-id").value;
    const corpo = {
        texto: $("p-texto").value,
        metaId: $("p-meta").value ? Number($("p-meta").value) : null,
        subprefeituraIds: idsMarcados("p-regioes")
    };
    try {
        await apiAdmin(id ? "/promessas/" + id : "/promessas",
            { method: id ? "PUT" : "POST", body: JSON.stringify(corpo) });
        $("form-promessa").reset();
        mensagem("msg-promessa", "Promessa salva com sucesso.", true);
        await carregarPromessas();
    } catch (e) {
        mensagem("msg-promessa", e.message, false);
    }
});

// ------------------------------------------------------------------ metas
async function carregarMetas() {
    metas = await api("/metas");
    const nome = sid => regioes.find(r => r.id === sid)?.nome || sid;
    $("lista-metas").innerHTML = metas.map(m => `
      <tr>
        <td>${m.numero}</td><td>${esc(m.texto)}</td><td>${esc(m.eixo)}</td><td>${selo(m.status)}</td>
        <td>${m.percentual !== null ? formatarNumero(m.valorAtual) + " de " + formatarNumero(m.valorAlvo)
                + " (" + formatarNumero(m.percentual) + "%)" : "—"}</td>
        <td>${esc(m.regionalizacao)}${m.subprefeituras.length
                ? ": " + m.subprefeituras.map(s => esc(nome(s))).join(", ") : ""}</td>
        <td>
          <button class="botao secundario pequeno" data-editar-meta="${m.id}">Editar</button>
          <button class="botao perigo pequeno" data-excluir-meta="${m.id}">Excluir</button>
        </td>
      </tr>`).join("");
    document.querySelectorAll("[data-editar-meta]").forEach(b => b.addEventListener("click", () =>
        editarMeta(metas.find(m => m.id == b.dataset.editarMeta))));
    document.querySelectorAll("[data-excluir-meta]").forEach(b => b.addEventListener("click", () =>
        excluir("/metas/" + b.dataset.excluirMeta, "msg-meta", async () => {
            await carregarMetas();
            await carregarPromessas();
        })));
    preencherSelects();
}

function atualizarRegioesBox() {
    const regionalizada = $("m-regionalizacao").value === "REGIONALIZADA";
    $("m-regioes-box").disabled = !regionalizada;
    if (!regionalizada) $("m-regioes").querySelectorAll("input").forEach(c => c.checked = false);
}
$("m-regionalizacao").addEventListener("change", atualizarRegioesBox);

function editarMeta(m) {
    $("titulo-meta").textContent = "Editar meta nº " + m.numero;
    $("m-id").value = m.id;
    $("m-numero").value = m.numero;
    $("m-texto").value = m.texto;
    $("m-eixo").value = eixos.find(e => e.nome === m.eixo)?.id ?? "";
    $("m-status").value = m.status;
    $("m-atual").value = m.valorAtual ?? "";
    $("m-alvo").value = m.valorAlvo ?? "";
    $("m-regionalizacao").value = m.regionalizacao;
    atualizarRegioesBox();
    marcar("m-regioes", m.subprefeituras);
    $("m-numero").focus();
}

$("form-meta").addEventListener("reset", () => {
    $("titulo-meta").textContent = "Cadastrar meta";
    $("m-id").value = "";
    setTimeout(atualizarRegioesBox);
});

$("form-meta").addEventListener("submit", async ev => {
    ev.preventDefault();
    const id = $("m-id").value;
    const numeroOuNulo = v => v === "" ? null : Number(v);
    const corpo = {
        numero: Number($("m-numero").value),
        texto: $("m-texto").value,
        eixoId: Number($("m-eixo").value),
        status: $("m-status").value,
        valorAtual: numeroOuNulo($("m-atual").value),
        valorAlvo: numeroOuNulo($("m-alvo").value),
        regionalizacao: $("m-regionalizacao").value,
        subprefeituraIds: idsMarcados("m-regioes")
    };
    try {
        await apiAdmin(id ? "/metas/" + id : "/metas",
            { method: id ? "PUT" : "POST", body: JSON.stringify(corpo) });
        $("form-meta").reset();
        mensagem("msg-meta", "Meta salva com sucesso.", true);
        await carregarMetas();
        await carregarPromessas();
    } catch (e) {
        mensagem("msg-meta", e.message, false);
    }
});

// ------------------------------------------------------------------ exclusao
async function excluir(caminho, idMensagem, recarregar) {
    if (!confirm("Tem certeza que deseja excluir?")) return;
    try {
        await apiAdmin(caminho, { method: "DELETE" });
        mensagem(idMensagem, "Excluído com sucesso.", true);
        await recarregar();
    } catch (e) {
        mensagem(idMensagem, e.message, false);
    }
}

if (token) entrar().catch(sair);
