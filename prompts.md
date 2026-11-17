# Registro de uso de IA

Registro exigido pela Global Solution 2026.2 ("IA como copiloto"). A equipe Zynox usou o **Claude Code**
(extensão do VS Code) como assistente de desenvolvimento na disciplina Domain Driven Design - Java.

**Como a ferramenta foi usada:** a equipe definiu o escopo, o modelo de dados e as regras do domínio. A IA
implementou e revisou o código a partir dessas definições. Cada etapa foi conferida pela equipe com testes
automatizados e com a aplicação rodando no Oracle da FIAP. Os prompts estão resumidos abaixo em ordem
cronológica, cada um com o pedido, as decisões da equipe e o resultado.

---

## 1. Implementação inicial da API (12/11/2026)

**Pedido:** implementar a API RESTful em Spring Boot a partir da documentação Agile da equipe (backlog MoSCoW e
diagrama de casos de uso). Restrições definidas no prompt:
- somente as histórias **Must** (US01–US11), sem ampliar o escopo;
- arquitetura em camadas **MODEL, DAO, BO e CONTROLLER**, com tratamento global de exceções;
- persistência no **Oracle FIAP** (sem trocar de banco);
- autenticação do administrador, testes automatizados, Swagger e preparo do deploy no Render;
- nenhuma credencial no código e nenhum commit sem autorização da equipe.

**Resultado:** estrutura Maven do projeto, entidades JPA, repositórios Spring Data, BOs com as regras de negócio,
controllers REST, `GlobalExceptionHandler`, Spring Security com JWT e BCrypt, telas web, testes e documentação
técnica.

## 2. Configuração do ambiente (12/11/2026)

**Pedido:** orientação sobre as variáveis de ambiente do `.env` (credenciais do Oracle FIAP, `JWT_SECRET`,
administrador inicial) e geração de um segredo aleatório para assinar os tokens JWT.

**Resultado:** `.env` configurado fora do versionamento (`.gitignore`) e `.env.example` documentado.

## 3. Integração com o modelo de dados da equipe (12/11/2026)

**Pedido:** conectar a aplicação ao Oracle FIAP usando o modelo já criado na disciplina Database Design.

**Decisões da equipe:** usar o banco existente em vez de gerar tabelas pelo Hibernate (`ddl-auto=none`); avaliar a
inclusão da coluna `FONTE` e da subprefeitura Pinheiros (bairro piloto do Agile).

**Resultado:** mapeamento JPA ajustado às 7 tabelas reais (incluindo as tabelas de ligação N:N via `@ManyToMany`)
e testes de integração no Oracle real.

## 4. Conformidade com o contrato de integração do grupo (13/11/2026)

**Pedido:** verificar se a API seguia o pacote de integração do grupo (rotas, formato JSON consumido pelo
programa Python, scripts DDL/DML e a Ficha da CitizenTech).

**Decisões da equipe:**
- manter **JPA/Spring Data**, conforme o padrão da aula;
- não alterar o modelo do banco naquele momento, e por isso reverter `FONTE` e Pinheiros;
- exigir autenticação no `POST /admins` (o primeiro administrador é criado pelas variáveis de ambiente);
- usar na interface os rótulos de status da documentação Agile (Em planejamento / Em andamento / Concluída).

**Resultado:** rotas e JSON iguais ao contrato, termômetro por subprefeitura, filtros, ligações de metas e
promessas com subprefeituras, CORS, exportação CSV para Data Science e README com a Ficha da CitizenTech.

## 5. Checagem dos requisitos da GS e preparo do deploy (13/11/2026)

**Pedido:** comparar o projeto com os requisitos da disciplina no PDF da GS e completar o que faltasse.

**Resultado:** checklist de requisitos; ajustes para o Render (porta pela variável `PORT`, proxy HTTPS com
`forward-headers-strategy`, limite de memória do container), diagramas de classes e DER e o PDF da documentação.

## 6. Alinhamento com a nova versão do modelo (17/11/2026)

**Contexto:** a equipe alinhou o projeto com as entregas de Agile e Database Design. O modelo passou a ter
`META.FONTE`, `PROMESSA.FONTE` e `PROMESSA.EIXO_ID` obrigatórios, Pinheiros como subprefeitura 7, a regra de que
uma promessa ligada a uma meta tem a mesma área (eixo) dela, o filtro por área na tela e um novo visual.

**Pedido:** integrar essas mudanças ao repositório local e validar o projeto para a entrega.

**Resultado:** conferido que o Oracle FIAP já estava migrado (colunas `NOT NULL`, FK `FK_PROMESSA_EIXO` e
nenhum registro violando a regra de área); `mvnw verify` com todos os testes aprovados, inclusive a integração
com o Oracle.

## 7. Documentação final e verificação do deploy (17/11/2026)

**Pedido:** atualizar o PDF da documentação Java conforme os itens do enunciado (capa com RMs, índice,
diagrama de classes, protótipos de tela, tabela de endpoints, DER e instruções de execução) e conferir o deploy.

**Decisão da equipe:** refazer o deploy no Render com a versão final do código.

**Resultado:** diagramas de classes e DER regerados com os novos campos, capturas das telas reais, PDF e
arquivo .zip de entrega; rotas públicas, telas, Swagger e autenticação conferidas no endereço publicado.
