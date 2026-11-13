# Ficha da CitizenTech

| Campo | Resposta |
|---|---|
| **Nome** | Pra Todos SP |
| **Cidade e bairro** | São Paulo. Bairro piloto: [a definir pelo grupo]. O protótipo usa subprefeituras reais de exemplo. |
| **Cidadão beneficiado** | Morador de São Paulo com pouco tempo, que quer acompanhar a gestão da cidade sem ler PDFs. |
| **Problema em uma frase** | O cidadão tem dificuldade de acompanhar o que o prefeito prometeu na campanha, o que virou meta oficial no Programa de Metas e o que isso muda no bairro dele. |
| **Como funciona** | O cidadão escolhe o bairro (subprefeitura) e vê as promessas relacionadas à região. Cada promessa aparece como "virou meta oficial" (com status e, quando existir, o resultado numérico) ou "não virou meta". Um administrador cadastra e edita promessas e metas. Todos os dados são fictícios e a tela avisa isso. |
| **O que cada parte faz** | **Database:** guarda eixos, subprefeituras, metas, promessas e administradores. **Java:** API REST (Spring Boot) que entrega os dados. **Dynamic Programming:** busca de promessas por semelhança, mesmo com erro de digitação, e responde se virou meta. **Data Science:** [a definir]. **AR-VR:** [a definir]. **Rede:** [a definir]. |
| **Link do repositório** | [a preencher] |
| **Indicador de impacto** | [a definir pelo grupo] |
| **ODS conectados** | **ODS 10 (Redução das desigualdades):** combate a desigualdade territorial e de acesso à informação, dando ferramentas para que bairros historicamente menos atendidos fiscalizem se as promessas de campanha chegam às suas regiões como chegam às áreas centrais. **ODS 11 (Cidades e comunidades sustentáveis):** traduz um plano da cidade inteira para o nível da subprefeitura e do bairro do cidadão. |

---

# Pra Todos SP — API Java (Domain Driven Design)

Equipe **Zynox** · Global Solution FIAP 2026.2 · Engenharia de Software, 2º ano.

> ⚠ **Dados fictícios.** Prefeito, promessas e metas são inventados; reais são só os nomes dos eixos e das
> subprefeituras. O banco e o contrato da API são os do grupo (`src/main/resources/db/` e `docs/INTEGRACAO.md`).

| Integrante | RM |
|---|---|
| Arthur Serrano Veloso | 561542 |
| Carlos Eduardo Goes | 562389 |
| Hyann dos Santos Espindas | 563421 |
| Israel Araujo Henriques de Moura | 559068 |
| Arthur Cardoso Carinhanha | 573573 |

## Tecnologias

| Ferramenta | Versão |
|---|---|
| Java (JDK Temurin) | 17 |
| Spring Boot | 3.5.16 (Web, Data JPA, Validation, Security) |
| Maven | 3.9.16 via Maven Wrapper (`mvnw`) — não precisa instalar |
| Banco de dados | Oracle FIAP (`oracle.fiap.com.br:1521/ORCL`), driver `ojdbc11` |
| Autenticação | JWT (jjwt 0.13) + BCrypt |
| Documentação da API | springdoc-openapi 2.8 (Swagger UI) |
| Testes | JUnit 5, Mockito, MockMvc |
| Deploy | Render (Docker) |
| IDE | VS Code ou IntelliJ IDEA |

## Arquitetura (camadas)

```
src/main/java/br/com/zynox/pratodossp/
├── model/       Entidades JPA = as 7 tabelas do 01_ddl.sql (as 2 de ligação viram listas em Meta e Promessa)
├── dao/         Acesso ao Oracle (interfaces Spring Data JPA)
├── bo/          Regras de negócio: valores, regionalização, percentual, termômetro, BCrypt
├── controller/  Endpoints REST (GET, POST, PUT, DELETE) — sem regra de negócio
├── dto/         Formato do JSON (igual aos exemplos do grupo)
├── exception/   Exceções próprias + tratador global (JSON com 400/401/403/404/409/500)
├── security/    Spring Security + JWT + CORS
└── config/      Swagger e inicialização (scripts do banco e primeiro admin)
src/main/resources/
├── db/          01_ddl.sql, 02_dml.sql, 03_dql.sql (scripts oficiais do grupo, sem alteração)
└── static/      Telas: index.html (cidadão) e admin.html (administrador)
```

Fluxo: `Controller → BO → DAO → Oracle`. Diagramas e rastreabilidade em
[docs/DOCUMENTACAO_TECNICA.md](docs/DOCUMENTACAO_TECNICA.md).

## Como executar localmente

### 1. Pré-requisitos
JDK 17+ (`java -version`) e usuário/senha do Oracle FIAP.

### 2. Banco
No SQL Developer, conectado ao Oracle da FIAP, rode `src/main/resources/db/01_ddl.sql` e depois `02_dml.sql`
(abra em UTF-8). Alternativa: deixe `DB_INICIALIZAR=true` e a aplicação roda os dois scripts **se a tabela
PROMESSA ainda não existir** (se já existir, não mexe em nada).

### 3. Variáveis
Copie `.env.example` para `.env` na raiz e preencha (o `.env` está no `.gitignore` — nunca vá para o GitHub):

| Variável | Obrigatória | Descrição |
|---|---|---|
| `DB_URL` | não | Padrão `jdbc:oracle:thin:@oracle.fiap.com.br:1521/ORCL` |
| `DB_USERNAME` / `DB_PASSWORD` | **sim** | Credenciais Oracle FIAP |
| `JWT_SECRET` | **sim** | Texto aleatório com 32+ caracteres |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | sim | Primeiro admin, criado na 1ª subida (senha 8+). Os admins do `02_dml` têm senha fictícia e não logam |
| `ADMIN_NOME` | não | Padrão `Administrador` |
| `DB_INICIALIZAR` | não | `true` = roda 01/02 se o banco estiver vazio |
| `PORT` | não | Padrão 8080 (o Render define sozinho) |

### 4. Subir
```bash
mvnw.cmd spring-boot:run     # Windows
./mvnw spring-boot:run       # Linux / macOS / Git Bash
```

| Endereço | O quê |
|---|---|
| http://localhost:8080/ | Telas do cidadão (escolha do bairro, termômetro, detalhe) |
| http://localhost:8080/admin.html | Tela do administrador |
| http://localhost:8080/swagger-ui.html | Swagger (testar a API) |

Rotas de admin: faça `POST /login`, copie o `token` e envie `Authorization: Bearer <token>`
(no Swagger: botão **Authorize**). Outros admins são criados por `POST /admins` (exige login).

## Testes

```bash
./mvnw test     # unitários (BO, MODEL) e de camada web (Controllers + segurança): não usam banco
./mvnw verify   # + OracleIntegracaoIT no Oracle REAL (só roda com credenciais; desfaz tudo com rollback)
```

## Endpoints (contrato do grupo — `docs/INTEGRACAO.md`)

| Método | Rota | Acesso | Para quê |
|---|---|---|---|
| GET | `/subprefeituras` | público | Bairros para o cidadão escolher |
| GET | `/subprefeituras/{id}/termometro` | público | **Tela principal**: promessas que viraram meta e as que não viraram |
| GET | `/termometro` | público | Termômetro de todas as subprefeituras |
| GET | `/eixos` | público | Os 4 eixos |
| GET | `/metas?eixoId=&status=&subprefeituraId=` | público | Metas com filtros opcionais |
| GET | `/metas/{id}` | público | Uma meta com percentual e subprefeituras |
| GET | `/metas/export` | público | CSV para Data Science |
| GET | `/promessas?semMeta=&eixoId=` | público | Promessas com estado e meta (**usada pelo Python**) |
| GET | `/promessas/{id}` | público | Uma promessa + subprefeituras próprias |
| POST | `/login` | público | E-mail e senha → token JWT (401 se errado) |
| POST | `/admins` | admin | Cria administrador (hash BCrypt; 409 se e-mail repetido) |
| POST / PUT / DELETE | `/promessas`, `/promessas/{id}` | admin | Cadastrar, editar, apagar promessa |
| POST / PUT / DELETE | `/metas`, `/metas/{id}` | admin | Cadastrar, editar, apagar meta (409 se tiver promessas) |
| POST / DELETE | `/metas/{id}/subprefeituras/{subId}` | admin | Ligar/desligar meta e subprefeitura |
| POST / DELETE | `/promessas/{id}/subprefeituras/{subId}` | admin | Ligar/desligar promessa **sem meta** (409 se tiver meta) |

CORS liberado. Para o projeto Python: `PROMESSAS=https://<url-do-deploy>/promessas python app.py`.

## Deploy no Render

1. Suba o repositório no GitHub.
2. No Render: **New → Web Service** → conecte o repositório (ele usa o `Dockerfile`), ou **New → Blueprint** com o `render.yaml`.
3. Em **Environment**: `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`.
4. Teste `https://<servico>.onrender.com/subprefeituras/1/termometro` e `/swagger-ui.html`.

No plano gratuito o serviço "dorme"; a primeira requisição pode levar cerca de 1 minuto.

## Documentação (PDF da disciplina)

- PDF único: [docs/Pra_Todos_SP_Documentacao_Java.pdf](docs/Pra_Todos_SP_Documentacao_Java.pdf) (capa, sumário,
  especificação, diagramas de classes, protótipos, endpoints, DER, instruções e rastreabilidade).
- Diagramas (fonte Mermaid + PNG): `docs/diagramas/`. Capturas reais das telas: `docs/prints/`.
- Para regerar o PDF com os links do GitHub e do deploy:
  ```bash
  uv run --no-project --with playwright python docs/ferramentas/gerar_diagramas.py
  uv run --no-project --with reportlab --with pillow python docs/ferramentas/gerar_pdf.py \
      --github https://github.com/<usuario>/<repo> --deploy https://<servico>.onrender.com
  ```

## Fora do escopo do MVP
US12 (filtro por área, Should): aparece desabilitado na tela com "em breve". US13 (notícias e agenda, Could) e
US14–US21 (Won't): não implementados.

## Uso de IA
Prompts usados com IA como copiloto: [prompts.md](prompts.md).
