# Notas do alinhamento com Database/Agile/Dynamic (ler antes de aplicar)

**Nada disto foi compilado nem rodado**: o ambiente onde foi feito não acessa o Maven Central nem o Oracle/Render.

1. Rode `./mvnw verify`. Se algum teste/compilação falhar, mande a mensagem de erro.
2. Novo banco (`src/main/resources/db/01_ddl.sql`, `02_dml.sql`): `META.fonte` e `PROMESSA.fonte` obrigatórios; `PROMESSA.eixo_id` obrigatório; Pinheiros = subprefeitura 7.
   Para recriar no Oracle FIAP: rode o bloco DROP no topo do `01_ddl.sql`, depois `01` e `02`
   (`DB_INICIALIZAR=true` só funciona se a tabela PROMESSA não existir).
3. API: `fonte` (meta e promessa) e `eixoId` (promessa) no POST/PUT; respostas trazem `fonte` e `area`. Exemplos em `docs/exemplos/`.
4. Regra nova: promessa com meta precisa ter a mesma área (eixo) da meta.
5. Telas: fonte e área reais, filtro por área na tela do bairro, formulários do admin com fonte e área.
6. As capturas em `docs/prints/` foram feitas com um servidor simulado (dados de `docs/exemplos`). Refaça com o app real se quiser.
7. Depois de testar: refaça o deploy no Render, reteste e só então faça push (antes do prazo: 19/11/2026 23h55).
8. A seção "Uso de IA" e o `prompts.md` não foram alterados.
