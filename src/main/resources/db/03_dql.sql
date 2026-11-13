-- =====================================================================
-- Pra Todos SP (CitizenTech)
-- Database Design | FIAP Global Solution 2026.2 | Engenharia de Software | Equipe Zynox
-- Script DQL (Oracle): três consultas avaliadas + consultas extras
-- Rode depois de 01_ddl.sql e 02_dml.sql.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Consulta 1: INNER JOIN entre duas tabelas
-- Promessas que viraram meta oficial, com o número e o status da meta.
-- Resultado esperado: 11 linhas.
-- ---------------------------------------------------------------------
SELECT p.id     AS id_promessa,
       p.texto  AS promessa,
       m.numero AS numero_meta,
       m.status AS status_meta
  FROM promessa p
 INNER JOIN meta m ON m.id = p.meta_id
 ORDER BY m.numero, p.id;

-- ---------------------------------------------------------------------
-- Consulta 2: INNER JOIN com agrupamento (COUNT e GROUP BY)
-- Quantidade de metas ligadas a cada subprefeitura.
-- Resultado esperado: 6 linhas; Itaquera lidera com 4 metas.
-- ---------------------------------------------------------------------
SELECT s.nome                AS subprefeitura,
       COUNT(ms.meta_id)     AS total_metas
  FROM subprefeitura s
 INNER JOIN meta_subprefeitura ms ON ms.subprefeitura_id = s.id
 GROUP BY s.nome
 ORDER BY total_metas DESC, s.nome;

-- ---------------------------------------------------------------------
-- Consulta 3: LEFT OUTER JOIN com WHERE e ORDER BY
-- Promessas que NÃO viraram meta (o LEFT JOIN mantém a promessa mesmo sem
-- meta; o WHERE m.id IS NULL deixa só as que ficaram de fora).
-- Resultado esperado: 4 linhas.
-- ---------------------------------------------------------------------
SELECT p.id    AS id_promessa,
       p.texto AS promessa
  FROM promessa p
  LEFT OUTER JOIN meta m ON m.id = p.meta_id
 WHERE m.id IS NULL
 ORDER BY p.texto;

-- =====================================================================
-- CONSULTAS EXTRAS (além das 3 avaliadas acima): apoio para a API Java, o
-- algoritmo em Python e a análise de dados. Podem ser executadas em qualquer ordem.
-- =====================================================================

-- E1) Termômetro de uma subprefeitura (Itaquera): promessa, meta, status e percentual
SELECT s.nome                    AS subprefeitura,
       p.texto                   AS promessa,
       m.numero                  AS numero_meta,
       m.status                  AS status_meta,
       CASE WHEN m.valor_alvo IS NULL THEN NULL
            ELSE ROUND(100 * m.valor_atual / m.valor_alvo, 1) END      AS percentual
  FROM subprefeitura s
 INNER JOIN meta_subprefeitura ms ON ms.subprefeitura_id = s.id
 INNER JOIN meta m                ON m.id = ms.meta_id
 INNER JOIN promessa p            ON p.meta_id = m.id
 WHERE s.nome = 'Itaquera'
 ORDER BY m.numero, p.id;

-- E2) Quantidade de metas por eixo e por status (GROUP BY)
SELECT e.nome     AS eixo,
       m.status   AS status_meta,
       COUNT(*)   AS total_metas
  FROM eixo e
 INNER JOIN meta m ON m.eixo_id = e.id
 GROUP BY e.nome, m.status
 ORDER BY e.nome, m.status;

-- E3) Percentual de execução das metas que têm valor alvo
SELECT m.numero  AS numero_meta,
       m.texto   AS meta,
       m.valor_atual,
       m.valor_alvo,
       ROUND(100 * m.valor_atual / m.valor_alvo, 1)    AS percentual
  FROM meta m
 WHERE m.valor_alvo IS NOT NULL
 ORDER BY percentual DESC, m.numero;

-- E4) Metas sem região definida (LEFT OUTER JOIN + WHERE)
SELECT m.numero         AS numero_meta,
       m.texto          AS meta,
       m.regionalizacao AS regionalizacao
  FROM meta m
  LEFT OUTER JOIN meta_subprefeitura ms ON ms.meta_id = m.id
 WHERE ms.meta_id IS NULL
 ORDER BY m.numero;

-- E5) Percentual médio de execução por eixo (GROUP BY com HAVING)
SELECT e.nome                              AS eixo,
       COUNT(*)                            AS metas_com_valor,
       ROUND(AVG(100 * m.valor_atual / m.valor_alvo), 1) AS percentual_medio
  FROM eixo e
 INNER JOIN meta m ON m.eixo_id = e.id
 WHERE m.valor_alvo IS NOT NULL
 GROUP BY e.nome
HAVING COUNT(*) >= 2
 ORDER BY percentual_medio DESC;

-- E6) Quantas promessas viraram meta e quantas não viraram
SELECT CASE WHEN p.meta_id IS NULL THEN 'Não virou meta' ELSE 'Virou meta' END AS estado,
       COUNT(*) AS total_promessas
  FROM promessa p
 GROUP BY CASE WHEN p.meta_id IS NULL THEN 'Não virou meta' ELSE 'Virou meta' END
 ORDER BY total_promessas DESC;

-- E7) Metas que não vieram de nenhuma promessa (LEFT OUTER JOIN + WHERE)
SELECT m.numero AS numero_meta,
       m.texto  AS meta
  FROM meta m
  LEFT OUTER JOIN promessa p ON p.meta_id = m.id
 WHERE p.id IS NULL
 ORDER BY m.numero;

-- E8) Promessas que não viraram meta em cada subprefeitura (INNER JOIN + GROUP BY)
SELECT s.nome            AS subprefeitura,
       COUNT(ps.promessa_id) AS promessas_sem_meta
  FROM subprefeitura s
 INNER JOIN promessa_subprefeitura ps ON ps.subprefeitura_id = s.id
 INNER JOIN promessa p                ON p.id = ps.promessa_id
 WHERE p.meta_id IS NULL
 GROUP BY s.nome
 ORDER BY promessas_sem_meta DESC, s.nome;

-- E9) Termômetro completo de Itaquera: promessas que viraram meta e as que não viraram
SELECT p.texto AS promessa,
       'Virou meta (meta ' || m.numero || ', ' || m.status || ')' AS estado
  FROM promessa p
 INNER JOIN meta m                ON m.id = p.meta_id
 INNER JOIN meta_subprefeitura ms ON ms.meta_id = m.id
 INNER JOIN subprefeitura s       ON s.id = ms.subprefeitura_id
 WHERE s.nome = 'Itaquera'
UNION ALL
SELECT p.texto, 'Não virou meta'
  FROM promessa p
 INNER JOIN promessa_subprefeitura ps ON ps.promessa_id = p.id
 INNER JOIN subprefeitura s           ON s.id = ps.subprefeitura_id
 WHERE s.nome = 'Itaquera' AND p.meta_id IS NULL
 ORDER BY 2, 1;

-- E10) Quantas metas e promessas cada administrador cadastrou (subconsultas com COUNT)
SELECT a.nome                                         AS administrador,
       (SELECT COUNT(*) FROM meta m     WHERE m.criado_por = a.id) AS metas_cadastradas,
       (SELECT COUNT(*) FROM promessa p WHERE p.criado_por = a.id) AS promessas_cadastradas
  FROM usuario_admin a
 ORDER BY a.nome;
