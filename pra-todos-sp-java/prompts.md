# Prompts utilizados com IA (copiloto)

Registro exigido pela Global Solution. A IA (Claude Code, no VS Code) foi usada como copiloto de desenvolvimento;
as decisões de escopo seguem a documentação Agile da equipe.

## Prompt 1 — 13/11/2026 — Prompt mestre de implementação

Ferramenta: Claude Code (VS Code).

Resumo do prompt enviado pela equipe:

> "Prompt Mestre — Pra Todos SP — Global Solution FIAP 2026.2 — Implementação completa da disciplina Domain Driven
> Design — Java — Equipe Zynox."
>
> Pediu que a IA: lesse o documento Agile (`Pra_Todos_SP_Documentacao_Agile.docx`) e o diagrama de casos de uso
> (`DiagramaDeCasosDeUsosPraTodosSP.asta`); auditasse o repositório; implementasse somente as histórias Must
> (US01–US11) em Java/Spring Boot com camadas MODEL, DAO, BO e CONTROLLER, tratamento global de exceções,
> Oracle FIAP, login administrativo, testes automatizados, Swagger, preparação de deploy no Render e documentação;
> sem ampliar o escopo, sem inventar credenciais ou resultados, sem trocar o Oracle por outro banco e sem fazer
> commit/push sem autorização.

O texto integral do prompt está guardado pela equipe e pode ser anexado aqui.

Resultado: estrutura do projeto, scripts Oracle, API REST, segurança JWT, telas, testes e documentação técnica
(ver `docs/DOCUMENTACAO_TECNICA.md`).

## Prompt 2 — 13/11/2026 — Configuração do ambiente

> "eu nao entendi da onde tiro essas infos" (sobre as variáveis do `.env`)

Resultado: explicação de cada variável; geração de um segredo aleatório para o JWT.

## Prompt 3 — 13/11/2026 — Conexão com o Oracle e uso do modelo da equipe

> "certo o .env esta preenchido"

A IA conectou ao Oracle FIAP, encontrou o modelo de dados já criado pela equipe e perguntou como seguir.
Decisões da equipe: usar o banco existente; adicionar a coluna FONTE em PROMESSA e META; incluir Pinheiros
(piloto do Agile). Resultado: Java adaptado ao modelo real, script `03_migracao_fonte_pinheiros.sql`,
testes com Oracle real e capturas das telas em `docs/prints/`.

## Prompt 4 — 13/11/2026 — Conformidade com o pacote do grupo

> "verifique se o projeto está nos conformes, adicionei a pasta pacote-java com os arquivos necessários, olhe o prompt"

Anexos: `INTEGRACAO.md`, `LEIA-ME_PRIMEIRO.md`, `FICHA_CITIZENTECH.md`, `PROMPT_JAVA.md`, SQLs do grupo,
`exemplos/` e o PDF da GS. A IA comparou o projeto com o contrato e perguntou o padrão da aula.
Decisões da equipe: manter JPA; reverter as mudanças no banco (coluna FONTE e Pinheiros); `POST /admins`
exige login; rótulos de status do Agile na tela. Resultado: rotas e JSON iguais ao contrato (conferido com os
exemplos), termômetro, filtros, ligações, CORS, CSV, README com a Ficha e testes no Oracle real.

## Prompt 5 — 13/11/2026 — Requisitos do PDF da GS

> "baseado nas informações do pdf [...] meu projeto cumpre todos requisitos pedidos??"
> "quero que você analise os componentes que faltam e deixe o projeto 100% como você acha que ele deve ser"

Resultado: checklist contra o PDF da GS; preparação do deploy (porta do Render, proxy HTTPS e limite de memória
testados com o JAR); diagramas de classes e DER em imagem; PDF único da documentação gerado a partir do código
(`docs/Pra_Todos_SP_Documentacao_Java.pdf`), com os links do GitHub e do deploy pendentes.

<!-- Adicione abaixo os próximos prompts usados, com data e objetivo. -->
