"""Gera o PDF unico da documentacao Java (Domain Driven Design) do Pra Todos SP.

Uso (na raiz do projeto):
    uv run --no-project --with reportlab --with pillow python docs/ferramentas/gerar_pdf.py \
        --github https://github.com/... --deploy https://....onrender.com

Sem --github/--deploy os links saem como "PENDENTE". Rode antes o gerar_diagramas.py.
"""
import argparse
from datetime import date
from pathlib import Path

from PIL import Image as PILImage
from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_JUSTIFY
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.units import cm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (BaseDocTemplate, Frame, Image, KeepTogether, NextPageTemplate, PageBreak,
                                PageTemplate, Paragraph, Preformatted, Spacer, Table, TableStyle)
from reportlab.platypus.tableofcontents import TableOfContents

RAIZ = Path(__file__).resolve().parents[2]
DOCS = RAIZ / "docs"

ap = argparse.ArgumentParser()
ap.add_argument("--github", default="PENDENTE (inserir o link do repositório)")
ap.add_argument("--deploy", default="PENDENTE (inserir o link do Render)")
ap.add_argument("--saida", default=str(DOCS / "Pra_Todos_SP_Documentacao_Java.pdf"))
args = ap.parse_args()

# ------------------------------------------------------------------ fontes e estilos
FONTES = Path("C:/Windows/Fonts")
if (FONTES / "arial.ttf").exists():
    pdfmetrics.registerFont(TTFont("Texto", str(FONTES / "arial.ttf")))
    pdfmetrics.registerFont(TTFont("Texto-B", str(FONTES / "arialbd.ttf")))
    pdfmetrics.registerFont(TTFont("Codigo", str(FONTES / "consola.ttf")))
    pdfmetrics.registerFontFamily("Texto", normal="Texto", bold="Texto-B")
    F, FB, FC = "Texto", "Texto-B", "Codigo"
elif Path("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf").exists():
    DJ = Path("/usr/share/fonts/truetype/dejavu")
    pdfmetrics.registerFont(TTFont("Texto", str(DJ / "DejaVuSans.ttf")))
    pdfmetrics.registerFont(TTFont("Texto-B", str(DJ / "DejaVuSans-Bold.ttf")))
    pdfmetrics.registerFont(TTFont("Codigo", str(DJ / "DejaVuSansMono.ttf")))
    pdfmetrics.registerFontFamily("Texto", normal="Texto", bold="Texto-B")
    F, FB, FC = "Texto", "Texto-B", "Codigo"
else:
    F, FB, FC = "Helvetica", "Helvetica-Bold", "Courier"

AZUL = colors.HexColor("#0b3d91")
CINZA = colors.HexColor("#eef1f6")
s = {
    "corpo": ParagraphStyle("corpo", fontName=F, fontSize=10.5, leading=15, alignment=TA_JUSTIFY, spaceAfter=6),
    "item": ParagraphStyle("item", fontName=F, fontSize=10.5, leading=15, leftIndent=14, bulletIndent=4, spaceAfter=2),
    "H1": ParagraphStyle("H1", fontName=FB, fontSize=17, leading=22, textColor=AZUL, spaceBefore=4, spaceAfter=10),
    "H2": ParagraphStyle("H2", fontName=FB, fontSize=12.5, leading=17, textColor=AZUL, spaceBefore=10, spaceAfter=6),
    "cel": ParagraphStyle("cel", fontName=F, fontSize=8.6, leading=11),
    "celB": ParagraphStyle("celB", fontName=FB, fontSize=8.6, leading=11, textColor=colors.white),
    "leg": ParagraphStyle("leg", fontName=F, fontSize=8.8, leading=12, alignment=TA_CENTER,
                          textColor=colors.HexColor("#444444"), spaceAfter=10),
    "cod": ParagraphStyle("cod", fontName=FC, fontSize=7.8, leading=10),
    "capaT": ParagraphStyle("capaT", fontName=FB, fontSize=34, leading=40, alignment=TA_CENTER, textColor=AZUL),
    "capaS": ParagraphStyle("capaS", fontName=F, fontSize=14, leading=20, alignment=TA_CENTER),
    "capaP": ParagraphStyle("capaP", fontName=F, fontSize=11, leading=16, alignment=TA_CENTER),
    "toc1": ParagraphStyle("toc1", fontName=FB, fontSize=10.5, leading=15),
    "toc2": ParagraphStyle("toc2", fontName=F, fontSize=9.5, leading=12.5, leftIndent=16),
}

LARG = A4[0] - 4 * cm
ALT = A4[1] - 4.6 * cm


def p(t, e="corpo"):
    return Paragraph(t, s[e])


def itens(lista):
    return [Paragraph(t, s["item"], bulletText="•") for t in lista]


def tabela(cab, linhas, larguras):
    dados = [[p(c, "celB") for c in cab]] + [[p(str(c), "cel") for c in l] for l in linhas]
    t = Table(dados, colWidths=[w * cm for w in larguras], repeatRows=1)
    t.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, 0), AZUL),
        ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, CINZA]),
        ("GRID", (0, 0), (-1, -1), 0.4, colors.HexColor("#b9bec7")),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("TOPPADDING", (0, 0), (-1, -1), 3), ("BOTTOMPADDING", (0, 0), (-1, -1), 3),
    ]))
    return t


def codigo(texto):
    t = Table([[Preformatted(texto.strip("\n"), s["cod"])]], colWidths=[LARG])
    t.setStyle(TableStyle([("BACKGROUND", (0, 0), (-1, -1), colors.HexColor("#f4f5f7")),
                           ("BOX", (0, 0), (-1, -1), 0.4, colors.HexColor("#b9bec7")),
                           ("LEFTPADDING", (0, 0), (-1, -1), 6)]))
    return t


def exemplo(titulo, texto):
    return KeepTogether([p(titulo), codigo(texto), Spacer(1, 4)])


def figura(caminho, legenda, max_alt=ALT - 1.5 * cm):
    w, h = PILImage.open(caminho).size
    largura = LARG
    altura = largura * h / w
    if altura > max_alt:
        altura = max_alt
        largura = altura * w / h
    return KeepTogether([Image(str(caminho), width=largura, height=altura), p(legenda, "leg")])


# ------------------------------------------------------------------ documento com sumario
class Documento(BaseDocTemplate):
    def afterFlowable(self, f):
        if isinstance(f, Paragraph) and f.style.name in ("H1", "H2"):
            nivel = 0 if f.style.name == "H1" else 1
            chave = f"s{id(f)}"
            self.canv.bookmarkPage(chave)
            self.canv.addOutlineEntry(f.getPlainText(), chave, level=nivel)
            self.notify("TOCEntry", (nivel, f.getPlainText(), self.page, chave))


def rodape(c, d):
    c.saveState()
    c.setFont(F, 8)
    c.setFillColor(colors.HexColor("#555555"))
    c.drawString(2 * cm, 1.2 * cm, "Pra Todos SP · Domain Driven Design - Java · Equipe Zynox · Dados fictícios")
    c.drawRightString(A4[0] - 2 * cm, 1.2 * cm, f"Página {d.page}")
    c.setStrokeColor(AZUL)
    c.line(2 * cm, A4[1] - 1.5 * cm, A4[0] - 2 * cm, A4[1] - 1.5 * cm)
    c.restoreState()


doc = Documento(args.saida, pagesize=A4, title="Pra Todos SP - Documentação Java (DDD)",
                author="Equipe Zynox", leftMargin=2 * cm, rightMargin=2 * cm, topMargin=2 * cm, bottomMargin=2 * cm)
quadro = Frame(2 * cm, 2 * cm, LARG, A4[1] - 4 * cm, id="q")
doc.addPageTemplates([PageTemplate(id="capa", frames=[quadro]),
                      PageTemplate(id="normal", frames=[quadro], onPage=rodape)])

h = []

# ------------------------------------------------------------------ capa
INTEGRANTES = [("Arthur Cardoso Carinhanha", "573573"), ("Arthur Serrano Veloso", "561542"),
               ("Carlos Eduardo Goes", "562389"), ("Hyann dos Santos Espindas", "563421"),
               ("Israel Araujo Henriques de Moura", "559068")]
h += [Spacer(1, 3 * cm), p("Pra Todos SP", "capaT"), Spacer(1, 0.4 * cm),
      p("Termômetro de promessas por bairro", "capaS"), Spacer(1, 1.6 * cm),
      p("<b>Domain Driven Design - Java</b>", "capaS"),
      p("Global Solution 2026.2 · CitizenTech", "capaS"),
      p("FIAP · Engenharia de Software · 2º ano", "capaP"), Spacer(1, 1.6 * cm),
      p("<b>Equipe Zynox</b>", "capaS"), Spacer(1, 0.4 * cm)]
cap = Table([[p("<b>Integrante</b>", "capaP"), p("<b>RM</b>", "capaP")]] +
            [[p(n, "capaP"), p(rm, "capaP")] for n, rm in INTEGRANTES], colWidths=[9 * cm, 3 * cm])
cap.setStyle(TableStyle([("LINEBELOW", (0, 0), (-1, 0), 0.8, AZUL), ("ALIGN", (0, 0), (-1, -1), "CENTER")]))
h += [cap, Spacer(1, 2.2 * cm), p(f"São Paulo · {date.today().strftime('%d/%m/%Y')}", "capaP"),
      NextPageTemplate("normal"), PageBreak()]

# ------------------------------------------------------------------ sumario
toc = TableOfContents()
toc.levelStyles = [s["toc1"], s["toc2"]]
h += [Paragraph("Sumário", ParagraphStyle("tituloSumario", parent=s["H1"])), toc, PageBreak()]

# ------------------------------------------------------------------ 1. especificacao
h += [p("1. Especificação do projeto", "H1"),
      p("1.1 Descritivo da solução", "H2"),
      p("O <b>Pra Todos SP</b> é uma CitizenTech para a cidade de São Paulo. O cidadão escolhe o seu bairro "
        "(subprefeitura) e vê as promessas de campanha do prefeito relacionadas à região. Cada promessa aparece "
        "com o seu estado: <b>virou meta oficial</b> do Programa de Metas (com status e, quando existe, o resultado "
        "numérico) ou <b>não entrou no plano da prefeitura</b>. Um administrador, com login, cadastra e mantém "
        "promessas e metas. A solução é uma plataforma web que também pode ser usada em totens de bibliotecas "
        "públicas."),
      p("Esta parte do projeto (disciplina Domain Driven Design - Java) é a <b>API RESTful em Spring Boot</b> que "
        "guarda e entrega esses dados a partir do banco Oracle da FIAP, mais as telas web que consomem a API. "
        "O bairro piloto é <b>Pinheiros</b> (zona oeste); o protótipo traz 7 subprefeituras de exemplo. O programa "
        "Python do grupo (Dynamic Programming) usa dados de exemplo embutidos e, se quiser, lê a rota "
        "<font name='%s'>GET /termometro</font> desta API." % FC),
      p("<b>Todos os dados são fictícios</b>: prefeito, promessas e metas foram inventados pela equipe; reais são "
        "apenas os nomes dos 4 eixos do Programa de Metas e das subprefeituras. As telas mostram esse aviso."),
      p("1.2 Problema", "H2"),
      p("Acompanhar o que um prefeito prometeu, o que virou meta oficial e o que foi entregue exige que o cidadão "
        "cruze, sozinho, documentos que vivem em lugares diferentes: o plano de governo registrado no TSE, o "
        "Programa de Metas da prefeitura e as notícias. Quem mais sente essa dificuldade é quem tem pouco tempo ou "
        "pouca familiaridade com tecnologia, como jovens que trabalham e estudam e idosos que se informam por TV e "
        "jornal. Nas conversas da equipe com moradores, o que mais apareceu foi a falta de fontes claras e de uma "
        "forma simples de ver o que foi cumprido, o que está em andamento e o que ficou de fora."),
      p("1.3 Justificativa", "H2"),
      p("O site do Programa de Metas já permite filtrar metas por subprefeitura; o diferencial do Pra Todos SP é "
        "<b>cruzar a promessa de campanha com o programa oficial</b>, mostrando também as promessas que ficaram de "
        "fora, algo que as soluções existentes não fazem de forma simples. Com isso o projeto contribui para o "
        "<b>ODS 10 (Redução das desigualdades)</b>, ao dar a bairros historicamente menos atendidos uma ferramenta "
        "para fiscalizar se as promessas chegam às suas regiões como chegam às áreas centrais, e para o "
        "<b>ODS 11 (Cidades e comunidades sustentáveis)</b>, ao traduzir o plano da cidade inteira para o nível do "
        "bairro."),
      p("1.4 Objetivos", "H2"),
      p("<b>Objetivo geral:</b> oferecer uma API Java, organizada em camadas e ligada ao Oracle da FIAP, que permita "
        "ao cidadão consultar o termômetro de promessas do seu bairro e ao administrador manter os dados."),
      *itens(["Representar o domínio (promessa, meta, eixo, subprefeitura, administrador) em classes Java fiéis ao "
              "banco do grupo.",
              "Separar responsabilidades em MODEL, DAO, BO e CONTROLLER, com tratamento centralizado de exceções.",
              "Expor endpoints REST com GET, POST, PUT e DELETE, protegendo as operações de administrador com "
              "login (JWT e senha com hash BCrypt).",
              "Aplicar as regras do domínio: estado da promessa, cálculo do percentual, regionalização das metas e "
              "ligações com subprefeituras.",
              "Publicar a aplicação no Render, conectada ao Oracle da FIAP.",
              "Atender a acessibilidade (US07): letras grandes, bom contraste e estados sempre com ícone, texto e "
              "cor."]),
      p("1.5 Público beneficiado", "H2"),
      p("As personas do projeto (documentação Agile) são <b>Lucas</b>, 18 anos, jovem aprendiz que quer saber de "
        "onde vem a informação; <b>Marina</b>, 19 anos, que trabalha e estuda e quer tudo num lugar só; e "
        "<b>Seu Antônio</b>, 79 anos, aposentado, que quer entender o que prometeram para o bairro sem termo "
        "técnico nem letra miúda."),
      p("1.6 Escopo do MVP (histórias Must do Product Backlog)", "H2"),
      tabela(["História", "Descrição", "Como foi atendida"], [
          ["US01", "Escolher o bairro", "GET /subprefeituras + Tela 1"],
          ["US02", "Ver as promessas do bairro com o estado", "GET /subprefeituras/{id}/termometro + Tela 2"],
          ["US03", "Saber se virou meta ou não entrou no plano", "Campo estado (VIROU_META / NAO_VIROU_META)"],
          ["US04", "Status da meta e resultado numérico", "status, valorAtual, valorAlvo e percentual"],
          ["US05", "Fonte de cada promessa e meta", "Campo fonte (obrigatório) guardado e exibido nas Telas 2 e 3"],
          ["US06", "Aviso de região não definida", "regionalizacao (NAO_DIVULGADA etc.) + aviso na Tela 3"],
          ["US07", "Letras grandes, contraste, linguagem simples", "CSS acessível; ícone + texto + cor"],
          ["US08", "Login do administrador", "POST /login (JWT) + Tela 4"],
          ["US09", "Cadastrar, editar e excluir promessas", "POST/PUT/DELETE /promessas"],
          ["US10", "Cadastrar, editar e excluir metas, com status e regionalização",
           "POST/PUT/DELETE /metas + ligações com subprefeituras"],
          ["US11", "Vincular promessa a uma meta ou deixá-la sem meta", "PUT /promessas/{id} (metaId)"],
      ], [1.6, 7.2, 8.2]),
      Spacer(1, 6),
      p("<b>Fora do MVP</b> (MoSCoW): US12 filtro por área (Should) foi implementado de forma simples, no navegador, "
        "usando a área (eixo) de cada promessa; US13 notícias e agenda (Could) e US14 a US21 (Won't) não foram implementadas."),
      PageBreak()]

# ------------------------------------------------------------------ 2. arquitetura
h += [p("2. Arquitetura e tecnologias", "H1"),
      p("2.1 Camadas", "H2"),
      p("A aplicação segue o padrão em camadas da disciplina. A requisição chega ao <b>Controller</b>, que só "
        "recebe os dados e chama o <b>BO</b>; o BO aplica as regras de negócio e usa o <b>DAO</b>, que conversa "
        "com o Oracle. Erros de qualquer camada viram uma resposta JSON padronizada no tratador global."),
      codigo("""
Navegador / Python ──HTTP/JSON──► CONTROLLER ──► BO ──► DAO ──JDBC──► Oracle FIAP
                                      │           │
                         JwtAuthFilter + CORS     GlobalExceptionHandler (erros em JSON)
"""),
      Spacer(1, 6),
      tabela(["Camada", "Pacote", "Responsabilidade"], [
          ["MODEL", "model", "Entidades JPA das tabelas do banco (Eixo, Subprefeitura, Meta, Promessa, UsuarioAdmin) e "
                             "enums (StatusMeta, Regionalizacao, EstadoPromessa). As tabelas de ligação "
                             "META_SUBPREFEITURA e PROMESSA_SUBPREFEITURA viram listas (@ManyToMany)."],
          ["DAO", "dao", "Interfaces Spring Data JPA (JpaRepository): o Spring gera o SQL a partir do nome do método."],
          ["BO", "bo", "Regras de negócio: valores da meta, regionalização, percentual, ligações, termômetro, "
                       "e-mail único, BCrypt e criado_por."],
          ["CONTROLLER", "controller", "Endpoints REST, validação (@Valid) e códigos HTTP. Sem regra de negócio."],
          ["DTO", "dto", "Formato do JSON (records). Nunca expõe senha_hash nem criado_por."],
          ["EXCEPTION", "exception", "Exceções próprias e GlobalExceptionHandler (@RestControllerAdvice)."],
          ["SECURITY", "security", "Spring Security, JWT, CORS e respostas 401/403 em JSON."],
          ["CONFIG", "config", "Swagger e inicialização (scripts do grupo e primeiro administrador)."],
      ], [2.6, 2.4, 12.0]),
      p("2.2 Tecnologias e versões", "H2"),
      tabela(["Ferramenta", "Versão / uso"], [
          ["Java", "JDK 17 (Temurin)"], ["Spring Boot", "3.5.16 (Web, Data JPA, Validation, Security)"],
          ["Maven", "3.9.16 pelo Maven Wrapper (mvnw), sem instalação"],
          ["Banco de dados", "Oracle FIAP (oracle.fiap.com.br:1521/ORCL), driver ojdbc11"],
          ["Segurança", "JWT (jjwt 0.13) e BCrypt"], ["Documentação da API", "springdoc-openapi 2.8 (Swagger UI)"],
          ["Testes", "JUnit 5, Mockito, MockMvc"], ["Front-end", "HTML, CSS e JavaScript servidos pelo Spring Boot"],
          ["Deploy", "Render (Docker)"], ["IDE", "Visual Studio Code / IntelliJ IDEA"],
      ], [5, 12]),
      PageBreak()]

# ------------------------------------------------------------------ 3. diagramas de classes
DG = DOCS / "diagramas"
h += [p("3. Diagrama de classes", "H1"),
      p("Os diagramas abaixo foram feitos a partir do código Java do repositório (mesmos nomes de classes, "
        "atributos, métodos e relacionamentos). Estão divididos em quatro partes para caber legíveis na página."),
      p("3.1 Camada MODEL (entidades e enums)", "H2"),
      figura(DG / "classes_modelo.png", "Figura 1 — Entidades JPA, enums e cardinalidades (nomes das colunas/tabelas "
                                        "do Oracle nas associações)."),
      PageBreak(),
      p("3.2 Camadas CONTROLLER e BO", "H2"),
      figura(DG / "classes_controller_bo.png", "Figura 2 — Controllers (endpoints) e os BOs que eles usam."),
      PageBreak(),
      p("3.3 Camadas BO e DAO", "H2"),
      figura(DG / "classes_bo_dao.png", "Figura 3 — Dependências dos BOs com os DAOs (interfaces JpaRepository) e o "
                                        "JwtService."),
      PageBreak(),
      p("3.4 Tratamento de exceções e segurança", "H2"),
      figura(DG / "classes_excecoes_seguranca.png", "Figura 4 — Exceções próprias, tratador global e classes de "
                                                    "segurança."),
      PageBreak()]

# ------------------------------------------------------------------ 4. prototipos
PR = DOCS / "prints"
h += [p("4. Protótipos das telas", "H1"),
      p("As imagens a seguir são <b>capturas reais</b> do front-end implementado, rodando com a API conectada ao "
        "aos dados fictícios de exemplo do grupo (docs/exemplos). As telas seguem os protótipos da documentação Agile."),
      p("4.1 Tela 1 — Escolha do bairro", "H2"),
      figura(PR / "tela1_escolha_bairro.png", "Tela 1 — o cidadão escolhe a subprefeitura (GET /subprefeituras)."),
      PageBreak(),
      p("4.2 Tela 2 — Termômetro do bairro", "H2"),
      figura(PR / "tela2_termometro_itaquera.png",
             "Tela 2 — Pinheiros (bairro piloto): resumo, filtro por área, promessas que viraram meta e as que não entraram no plano "
             "(GET /subprefeituras/7/termometro).", max_alt=ALT - 2 * cm),
      PageBreak(),
      p("4.3 Tela 3 — Detalhe da promessa e da meta", "H2"),
      figura(PR / "tela3_detalhe_meta_regionalizada.png",
             "Tela 3 — meta com resultado numérico, barra de progresso e regiões afetadas.", max_alt=ALT / 2 - 1 * cm),
      figura(PR / "tela3b_detalhe_regiao_nao_divulgada.png",
             "Tela 3 — meta sem região divulgada (aviso da US06).", max_alt=ALT / 2 - 1 * cm),
      PageBreak(),
      figura(PR / "tela3c_detalhe_sem_meta.png", "Tela 3 — promessa que não entrou no plano da prefeitura.",
             max_alt=ALT / 2 - 1 * cm),
      p("4.4 Tela 4 — Administrador", "H2"),
      figura(PR / "tela4a_login_admin.png", "Tela 4 — login do administrador (POST /login).", max_alt=ALT / 2 - 2 * cm),
      PageBreak(),
      figura(PR / "tela4b_cadastro_promessa.png", "Tela 4 — cadastro de promessa (POST/PUT /promessas).",
             max_alt=ALT / 2 - 1 * cm),
      figura(PR / "tela4c_gerenciar_metas.png", "Tela 4 — cadastro de meta (POST/PUT /metas).",
             max_alt=ALT / 2 - 1 * cm),
      PageBreak()]

# ------------------------------------------------------------------ 5. endpoints
h += [p("5. Tabela de endpoints", "H1"),
      p("Rotas definidas no contrato do grupo (INTEGRACAO.md). GET e POST /login são públicos; as demais exigem o "
        "cabeçalho <font name='%s'>Authorization: Bearer &lt;token&gt;</font>. Todas as respostas são JSON (exceto "
        "o CSV). A API também pode ser testada pelo Swagger em /swagger-ui.html." % FC),
      tabela(["Método", "Rota", "Descrição", "Acesso", "Respostas"], [
          ["GET", "/subprefeituras", "Lista os bairros", "Público", "200"],
          ["GET", "/subprefeituras/{id}/termometro", "Termômetro do bairro (tela principal)", "Público", "200, 404"],
          ["GET", "/termometro", "Termômetro de todas as subprefeituras", "Público", "200"],
          ["GET", "/eixos", "Os 4 eixos", "Público", "200"],
          ["GET", "/metas?eixoId=&status=&subprefeituraId=", "Metas com filtros opcionais", "Público", "200, 400"],
          ["GET", "/metas/{id}", "Uma meta com percentual e subprefeituras", "Público", "200, 404"],
          ["GET", "/metas/export", "Metas em CSV (Data Science)", "Público", "200"],
          ["GET", "/promessas?semMeta=&eixoId=", "Promessas com área, fonte, estado e meta", "Público", "200"],
          ["GET", "/promessas/{id}", "Uma promessa com suas subprefeituras", "Público", "200, 404"],
          ["POST", "/login", "Login: devolve o token JWT", "Público", "200, 400, 401"],
          ["POST", "/admins", "Cria administrador (hash BCrypt)", "Admin", "201, 400, 401, 409"],
          ["POST", "/promessas", "Cadastra promessa", "Admin", "201, 400, 401, 409"],
          ["PUT", "/promessas/{id}", "Edita promessa (inclui ligar/desligar meta)", "Admin", "200, 400, 401, 404, 409"],
          ["DELETE", "/promessas/{id}", "Apaga promessa", "Admin", "204, 401, 404"],
          ["POST", "/promessas/{id}/subprefeituras/{subId}", "Liga promessa sem meta a um bairro", "Admin",
           "200, 401, 404, 409"],
          ["DELETE", "/promessas/{id}/subprefeituras/{subId}", "Desliga promessa de um bairro", "Admin", "200, 401, 404"],
          ["POST", "/metas", "Cadastra meta", "Admin", "201, 400, 401, 409"],
          ["PUT", "/metas/{id}", "Edita meta (status, valores, regionalização)", "Admin", "200, 400, 401, 404, 409"],
          ["DELETE", "/metas/{id}", "Apaga meta (409 se houver promessas)", "Admin", "204, 401, 404, 409"],
          ["POST", "/metas/{id}/subprefeituras/{subId}", "Liga meta REGIONALIZADA a um bairro", "Admin",
           "200, 400, 401, 404, 409"],
          ["DELETE", "/metas/{id}/subprefeituras/{subId}", "Desliga meta de um bairro", "Admin", "200, 401, 404, 409"],
      ], [1.75, 5.4, 4.9, 1.7, 3.25]),
      PageBreak(), p("5.1 Exemplos de requisições", "H2"),
      exemplo("<b>GET /subprefeituras/1/termometro</b> (200, trecho):", """
{ "aviso": "Dados fictícios",
  "subprefeitura": {"id": 1, "nome": "Itaquera"},
  "promessas": [
    {"id": 1, "texto": "Construir creches em todos os bairros", "area": "Universo SP",
     "fonte": "Plano de governo (fictício)", "estado": "VIROU_META",
     "meta": {"id": 1, "numero": 1, "texto": "Ampliar a rede de creches...", "eixo": "Universo SP",
              "status": "EM_PROGRESSO", "valorAtual": 12, "valorAlvo": 40, "percentual": 30.0,
              "regionalizacao": "REGIONALIZADA", "fonte": "Programa de Metas (fictício)"}} ],
  "naoViraramMeta": [
    {"id": 11, "texto": "...", "area": "Viver São Paulo", "fonte": "Plano de governo (fictício)",
     "estado": "NAO_VIROU_META", "meta": null} ] }
"""),
      exemplo("<b>POST /login</b> → 200:", """
Requisição: {"email": "admin@pratodossp.dev", "senha": "********"}
Resposta:   {"token": "eyJhbGciOiJIUzI1NiJ9...", "tipo": "Bearer", "expiraEmSegundos": 7200,
             "admin": {"id": 6, "nome": "Administrador", "email": "admin@pratodossp.dev"}}
"""),
      exemplo("<b>POST /metas</b> (Authorization: Bearer ...) → 201 Created:", """
{"numero": 13, "texto": "Construir 5 novos parques urbanos", "eixoId": 2, "status": "EM_PROGRESSO",
 "valorAtual": 2, "valorAlvo": 5, "regionalizacao": "REGIONALIZADA",
 "fonte": "Programa de Metas (fictício)", "subprefeituraIds": [1, 4]}
"""),
      exemplo("<b>PUT /promessas/11</b> → 200 (liga a promessa à meta 9; as ligações com subprefeituras são apagadas):", """
{"texto": "Fazer um grande parque linear na zona leste", "eixoId": 2,
 "fonte": "Plano de governo (fictício)", "metaId": 9}
"""),
      exemplo("<b>DELETE /metas/1</b> → 409 Conflict (erro padronizado):", """
{"timestamp": "2026-11-13T10:00:00", "status": 409, "error": "CONFLICT",
 "message": "A meta 1 tem 2 promessa(s) ligada(s). Desligue ou apague essas promessas antes.",
 "path": "/metas/1"}
"""),
      PageBreak()]

# ------------------------------------------------------------------ 6. DER
h += [p("6. Diagrama Entidade-Relacionamento (DER)", "H1"),
      p("Banco Oracle definido pelo grupo (script 01_ddl.sql, disciplina Database Design), usado pela API "
        "(versão alinhada ao Agile: fonte em META e PROMESSA, área da promessa em eixo_id). Chaves primárias com sequences SEQ_&lt;TABELA&gt;; as tabelas de ligação têm chave composta."),
      figura(DG / "der.png", "Figura 5 — DER do Pra Todos SP (7 tabelas).", max_alt=ALT - 12.5 * cm),
      tabela(["Tabela", "Descrição"], [
          ["EIXO", "Os 4 eixos do Programa de Metas."],
          ["SUBPREFEITURA", "Subprefeituras de São Paulo (o bairro que o cidadão escolhe)."],
          ["META", "Meta oficial: número único, eixo, status, valores opcionais (juntos), regionalização e fonte."],
          ["PROMESSA", "Promessa de campanha com área (EIXO_ID) e fonte; META_ID vazio = não virou meta."],
          ["META_SUBPREFEITURA", "Liga metas REGIONALIZADAS às subprefeituras (N:N)."],
          ["PROMESSA_SUBPREFEITURA", "Liga promessas sem meta às subprefeituras (N:N)."],
          ["USUARIO_ADMIN", "Administradores; senha só como hash BCrypt; cadastram metas e promessas (CRIADO_POR)."],
      ], [4.2, 12.8]),
      PageBreak()]

# ------------------------------------------------------------------ 7. regras e excecoes
h += [p("7. Regras de negócio e tratamento de exceções", "H1"),
      tabela(["Regra", "Onde", "HTTP"], [
          ["Percentual = valor atual ÷ valor alvo × 100, com 1 casa; null sem valor alvo", "MetaBO.calcularPercentual", "—"],
          ["Valor atual e valor alvo juntos ou nenhum; atual não negativo; alvo maior que zero", "MetaBO.validarValores", "400"],
          ["REGIONALIZADA precisa de ao menos uma subprefeitura; as demais não podem ter", "MetaBO.validarRegionalizacao", "400 / 409"],
          ["Fonte obrigatória (meta e promessa); área da promessa obrigatória e igual à do eixo da meta, se houver", "MetaBO.fonteObrigatoria, PromessaBO", "400"],
          ["Número de meta único", "MetaBO.cadastrar / atualizar", "409"],
          ["Não apagar meta com promessas ligadas", "MetaBO.excluir", "409"],
          ["Promessa com meta herda a região da meta; só promessa sem meta liga a subprefeituras", "PromessaBO", "409"],
          ["Ligar promessa a uma meta apaga as ligações dela com subprefeituras", "PromessaBO.aplicarDados", "—"],
          ["Termômetro: promessas pela região da meta; sem meta pela PROMESSA_SUBPREFEITURA", "TermometroBO", "—"],
          ["E-mail de admin único; senha só em BCrypt; hash nunca devolvido", "AdminBO", "409"],
          ["CRIADO_POR = administrador do token; não aparece no JSON", "MetaBO / PromessaBO", "—"],
          ["IDs inexistentes: na URL = 404; no corpo (meta, eixo, subprefeitura) = 400", "BOs", "404 / 400"],
      ], [9.2, 5.2, 2.6]),
      Spacer(1, 8),
      p("O <b>GlobalExceptionHandler</b> (@RestControllerAdvice) transforma cada exceção em JSON com timestamp, "
        "status, error, message e path, sem stack trace. RegraNegocioException → 400, CredenciaisInvalidasException "
        "→ 401, RecursoNaoEncontradoException → 404, ConflitoException → 409, erros de validação (@Valid) → 400 com "
        "a lista de campos, e qualquer erro inesperado → 500 com mensagem genérica (o detalhe vai só para o log). "
        "Os erros 401 e 403 da camada de segurança são escritos no mesmo formato pelo ErroSegurancaHandler."),
      p("7.1 Testes automatizados", "H2"),
      p("<font name='%s'>./mvnw verify</font> executa os testes unitários e de camada web (BO e MODEL com "
        "Mockito; Controllers com MockMvc e a segurança real) e os testes de integração no Oracle FIAP "
        "(OracleIntegracaoIT, com rollback). Os testes devem ser executados pela equipe com o esquema novo do banco antes da entrega; o JSON da API "
        "deve ser comparado com os exemplos do grupo em docs/exemplos." % FC),
      PageBreak()]

# ------------------------------------------------------------------ 8. instrucoes
h += [p("8. Instruções de execução", "H1"),
      p("8.1 Links", "H2"),
      tabela(["Item", "Endereço"], [
          ["Repositório GitHub", args.github],
          ["Deploy (Render)", args.deploy],
          ["Swagger no deploy", (args.deploy.rstrip("/") + "/swagger-ui.html") if args.deploy.startswith("http") else "PENDENTE"],
      ], [4.5, 12.5]),
      p("8.2 Ferramentas e versões", "H2"),
      *itens(["JDK 17 ou superior (testado com Temurin 17).",
              "Maven: não precisa instalar; use o Maven Wrapper (mvnw / mvnw.cmd).",
              "Banco Oracle da FIAP e Oracle SQL Developer (para rodar os scripts do grupo).",
              "IDE: Visual Studio Code ou IntelliJ IDEA. Navegador atualizado para as telas e o Swagger."]),
      p("8.3 Passo a passo (local)", "H2"),
      *itens(["Clone o repositório: <font name='%s'>git clone &lt;link do GitHub&gt;</font>." % FC,
              "No SQL Developer, conectado ao Oracle FIAP, rode <font name='%s'>src/main/resources/db/01_ddl.sql</font> "
              "e depois <font name='%s'>02_dml.sql</font> (UTF-8). Alternativa: DB_INICIALIZAR=true faz isso se o "
              "banco estiver vazio." % (FC, FC),
              "Copie <font name='%s'>.env.example</font> para <font name='%s'>.env</font> e preencha DB_USERNAME, "
              "DB_PASSWORD, JWT_SECRET (32+ caracteres), ADMIN_EMAIL e ADMIN_PASSWORD (8+). O .env não vai para o "
              "GitHub." % (FC, FC),
              "Suba a aplicação: <font name='%s'>mvnw.cmd spring-boot:run</font> (Windows) ou "
              "<font name='%s'>./mvnw spring-boot:run</font>." % (FC, FC),
              "Acesse http://localhost:8080/ (cidadão), /admin.html (administrador) e /swagger-ui.html (API).",
              "Para rotas de admin: POST /login, copie o token e use Authorization: Bearer &lt;token&gt; "
              "(no Swagger, botão Authorize).",
              "Testes: <font name='%s'>./mvnw test</font> (sem banco) e <font name='%s'>./mvnw verify</font> "
              "(inclui o teste no Oracle real)." % (FC, FC)]),
      p("8.4 Deploy no Render", "H2"),
      *itens(["No render.com: New → Web Service → conectar o repositório do GitHub (o Render usa o Dockerfile do "
              "projeto) ou New → Blueprint com o render.yaml.",
              "Em Environment, cadastrar DB_USERNAME, DB_PASSWORD, JWT_SECRET, ADMIN_EMAIL e ADMIN_PASSWORD "
              "(DB_URL já vem com o endereço do Oracle FIAP). As credenciais nunca ficam no código.",
              "O Render informa a porta pela variável PORT; a aplicação já lê esse valor.",
              "Após o deploy, testar /subprefeituras/1/termometro e /swagger-ui.html no endereço publicado.",
              "No plano gratuito o serviço hiberna sem uso; a primeira requisição pode levar cerca de 1 minuto."]),
      p("O projeto de Dynamic Programming roda com dados de exemplo embutidos; opcionalmente lê a API com "
        "<font name='%s'>TERMOMETRO=&lt;link do deploy&gt;/termometro python app.py</font>." % FC),
      PageBreak()]

# ------------------------------------------------------------------ 9. rastreabilidade
h += [p("9. Rastreabilidade", "H1"),
      tabela(["História", "Caso de uso", "Endpoint", "Classe Java", "Teste"], [
          ["US01", "UC01", "GET /subprefeituras", "SubprefeituraController, SubprefeituraBO", "SubprefeituraControllerTest"],
          ["US02", "UC02", "GET /subprefeituras/{id}/termometro", "TermometroBO", "TermometroBOTest, OracleIntegracaoIT"],
          ["US03", "UC03", "GET /promessas?semMeta=", "Promessa.getEstado, PromessaBO", "ModelTest, PromessaBOTest"],
          ["US04", "UC04", "GET /metas/{id}", "MetaBO.calcularPercentual", "MetaBOTest.percentual"],
          ["US05", "UC04", "campo fonte em /metas e /promessas", "MetaBO.fonteObrigatoria, PromessaBO", "MetaBOTest, PromessaBOTest"],
          ["US06", "UC06", "GET /metas/{id}", "MetaBO.validarRegionalizacao", "MetaBOTest.regionalizacao"],
          ["US07", "todos", "—", "static/css/estilo.css", "capturas da seção 4"],
          ["US08", "UC07", "POST /login, POST /admins", "AdminBO, JwtService, SecurityConfig", "AdminBOTest, AdminControllerTest"],
          ["US09", "UC08", "POST/PUT/DELETE /promessas", "PromessaBO, PromessaController", "PromessaBOTest, PromessaControllerTest"],
          ["US10", "UC09, UC11", "POST/PUT/DELETE /metas e ligações", "MetaBO, MetaController", "MetaBOTest, MetaControllerTest"],
          ["US11", "UC10", "PUT /promessas/{id}", "PromessaBO.aplicarDados", "PromessaBOTest, OracleIntegracaoIT"],
      ], [1.75, 1.75, 4.3, 4.7, 4.5]),
      p("9.1 Uso de IA", "H2"),
      p("A equipe usou IA (Claude Code) como copiloto de desenvolvimento, conforme permitido pela GS. Os prompts "
        "estão registrados no arquivo prompts.md do repositório, e as decisões de escopo seguiram a documentação "
        "Agile e o contrato do grupo.")]

doc.multiBuild(h)
print("PDF gerado:", args.saida)
