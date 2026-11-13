# Ficha da CitizenTech

| Campo | Resposta |
|---|---|
| **Nome da CitizenTech** | Pra Todos SP |
| **Cidade e bairro** | São Paulo (SP). Bairro piloto: Pinheiros, na zona oeste. O protótipo usa 7 subprefeituras de exemplo. |
| **Cidadão beneficiado** | Morador da Grande São Paulo que quer acompanhar a gestão da prefeitura sem ler PDFs: o jovem que usa redes e apps e sente falta de fontes, e o morador idoso que pede letras grandes, linguagem simples e ver as regiões afetadas por cada promessa. |
| **Problema em uma frase** | O morador de São Paulo não consegue saber, de forma simples, quais promessas do prefeito viraram meta e como estão andando no bairro dele. |
| **Como funciona** | O cidadão escolhe o bairro (subprefeitura) e vê as promessas ligadas à região. Cada promessa mostra o estado: **virou meta oficial** (com o status em 3 etapas, o resultado numérico quando existir e a fonte) ou **não entrou no plano da prefeitura**. O cidadão só consulta, sem login. Um administrador, com login, cadastra e edita promessas e metas. Todos os dados do protótipo são **fictícios** e a tela avisa isso. |
| **O que cada parte faz** | **Agile:** ideia, personas (das entrevistas), casos de uso, backlog com MoSCoW e protótipo de telas. **Database:** modelo Oracle com eixos, subprefeituras, metas, promessas e administradores. **Java:** API REST (Spring Boot) que entrega os dados do termômetro. **Dynamic Programming:** programa em Python com grafo das subprefeituras (BFS) e memoização, que mostra as promessas e metas do bairro e dos vizinhos. **Data Science:** análise de uma amostra de metas do Programa de Metas (regressão linear e logística) [a confirmar pelo grupo]. **AR-VR:** vídeo no Unreal Engine em uma biblioteca com um totem que mostra o termômetro. **Rede:** rede no Packet Tracer dessa biblioteca pública, com Wi-Fi do cidadão, servidor e segurança. |
| **Link do repositório** | https://github.com/ArthurDev4/pra-todos-sp-java |
| **Indicador de impacto** | Quantas pessoas conseguem descobrir, sozinhas e rápido, se uma promessa do bairro virou meta e como ela está andando. Meta: 8 de cada 10 pessoas acertarem em até 1 minuto. Como vamos medir: pedimos para algumas pessoas testarem o protótipo e anotamos quantas conseguem. |
| **ODS conectados** | **ODS 10 (Redução das desigualdades):** combate a desigualdade territorial e de acesso à informação, dando ferramentas para que bairros historicamente menos atendidos fiscalizem se as promessas de campanha chegam às suas regiões como chegam às áreas centrais. **ODS 11 (Cidades e comunidades sustentáveis):** traduz um plano da cidade inteira para o nível da subprefeitura e do bairro do cidadão. |

*Dados fictícios: prefeito, promessas, metas e números são inventados para o protótipo e não se referem a pessoas reais. São reais apenas os nomes dos eixos e das subprefeituras de São Paulo.*

---

# Pra Todos SP — API Java (Domain Driven Design)

Equipe **Zynox** · Global Solution FIAP 2026.2 · Engenharia de Software, 2º ano.

> ⚠ **Dados fictícios.** Prefeito, promessas e metas são inventados; reais são só os nomes dos eixos e das
> subprefeituras. O banco e o contrato da API são os do grupo (scripts em `src/main/resources/db/`).

| Integrante | RM |
|---|---|
| Arthur Cardoso Carinhanha | 573573 |
| Arthur Serrano Veloso | 561542 |
| Carlos Eduardo Goes | 562389 |
| Hyann dos Santos Espindas | 563421 |
| Israel Araujo Henriques de Moura | 559068 |

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

Fluxo: `Controller → BO → DAO → Oracle`.

## Como executar localmente

### 1. Pré-requisitos
JDK 17+ (`java -version`) e usuário/senha do Oracle FIAP.

### 2. Banco
No SQL Developer, conectado ao Oracle da FIAP, rode `src/main/resources/db/01_ddl.sql` e depois `02_dml.sql`
(abra em UTF-8). Alternativa: deixe `DB_INICIALIZAR=true` e a aplicação roda os dois scripts **se a tabela
PROMESSA ainda não existir** (se já existir, não mexe em nada).

Para **recriar o banco do zero** (por exemplo, ao receber uma versão nova do esquema), abra o `01_ddl.sql`, tire o
comentário (`--`) do bloco `DROP TABLE` / `DROP SEQUENCE` que fica no topo do arquivo, rode o script inteiro e depois o
`02_dml.sql`. Atenção: isso apaga todos os dados das tabelas.

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
Rode na pasta onde estão o `pom.xml` e o `mvnw` (o `.env` precisa estar nessa mesma pasta):

```bash
./mvnw spring-boot:run       # Git Bash, Linux ou macOS
.\mvnw.cmd spring-boot:run   # PowerShell
mvnw.cmd spring-boot:run     # Prompt de Comando (CMD)
```

Espere a mensagem `Started PraTodosSpApplication` antes de abrir o navegador.

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

## Endpoints (contrato do grupo)

| Método | Rota | Acesso | Para quê |
|---|---|---|---|
| GET | `/subprefeituras` | público | Bairros para o cidadão escolher |
| GET | `/subprefeituras/{id}/termometro` | público | **Tela principal**: promessas que viraram meta e as que não viraram |
| GET | `/termometro` | público | Termômetro de todas as subprefeituras |
| GET | `/eixos` | público | Os 4 eixos |
| GET | `/metas?eixoId=&status=&subprefeituraId=` | público | Metas com filtros opcionais |
| GET | `/metas/{id}` | público | Uma meta com percentual e subprefeituras |
| GET | `/metas/export` | público | CSV para Data Science |
| GET | `/promessas?semMeta=&eixoId=` | público | Promessas com estado e meta |
| GET | `/promessas/{id}` | público | Uma promessa + subprefeituras próprias |
| POST | `/login` | público | E-mail e senha → token JWT (401 se errado) |
| POST | `/admins` | admin | Cria administrador (hash BCrypt; 409 se e-mail repetido) |
| POST / PUT / DELETE | `/promessas`, `/promessas/{id}` | admin | Cadastrar, editar, apagar promessa |
| POST / PUT / DELETE | `/metas`, `/metas/{id}` | admin | Cadastrar, editar, apagar meta (409 se tiver promessas) |
| POST / DELETE | `/metas/{id}/subprefeituras/{subId}` | admin | Ligar/desligar meta e subprefeitura |
| POST / DELETE | `/promessas/{id}/subprefeituras/{subId}` | admin | Ligar/desligar promessa **sem meta** (409 se tiver meta) |

CORS liberado. O programa Python (Dynamic) usa dados de exemplo embutidos; opcionalmente lê o `GET /termometro` pela variável `TERMOMETRO`.

## Deploy no Render

**Publicado em: https://pratodossp.onrender.com** (Swagger: https://pratodossp.onrender.com/swagger-ui.html).

1. Suba o repositório no GitHub.
2. No Render: **New → Web Service** → conecte o repositório (ele usa o `Dockerfile`), ou **New → Blueprint** com o `render.yaml`.
3. Em **Environment**: `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`.
4. Teste `https://<servico>.onrender.com/subprefeituras/7/termometro` e `/swagger-ui.html`.

No plano gratuito o serviço "dorme"; a primeira requisição pode levar cerca de 1 minuto.

## Documentação
A documentação em PDF da disciplina (capa, sumário, especificação, diagramas de classes, protótipos, endpoints, DER e
instruções de execução) é entregue junto no ZIP do Portal do Aluno.

## Escopo do MVP
A US12 (filtro por área) faz parte do MVP do Agile e já funciona na tela do bairro (filtro no navegador, sem rota nova).
Fora do MVP e não implementados: US13 (notícias e agenda, Could) e US14–US21 (Won't).

## Uso de IA
Prompts usados com IA como copiloto: [prompts.md](prompts.md).
