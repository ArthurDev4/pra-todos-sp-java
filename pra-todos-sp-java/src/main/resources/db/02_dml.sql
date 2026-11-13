-- =====================================================================
-- Pra Todos SP (CitizenTech)
-- Database Design | FIAP Global Solution 2026.2 | Engenharia de Software | Equipe Zynox
-- Script DML (Oracle): dados de exemplo (mínimo de 5 linhas por tabela)
--
-- DADOS FICTÍCIOS: o prefeito, as promessas, as metas e seus números não existem
-- e não atribuem nada a pessoas reais. São reais apenas os nomes dos 4 eixos e das
-- subprefeituras de São Paulo. Os administradores são contas de teste.
-- As senhas (senha_hash) são valores FICTÍCIOS no formato do BCrypt e não
-- correspondem a nenhuma senha: para testar o login, crie uma conta pela API.
-- Rode depois do 01_ddl.sql. As tabelas são populadas na ordem das dependências (USUARIO_ADMIN primeiro,
-- porque META e PROMESSA guardam o admin que as cadastrou em criado_por).
--
-- Fonte: META e PROMESSA guardam de onde vem a informação (campo fonte). Área: toda promessa
-- tem um eixo (eixo_id); se ela virou meta, é o mesmo eixo da meta (regra do BO da API).
--
-- Regra de uso: a região de uma promessa que virou meta vem da própria meta
-- (META_SUBPREFEITURA); a região de uma promessa que NÃO virou meta vem de
-- PROMESSA_SUBPREFEITURA. Assim todo bairro mostra suas promessas dos dois tipos.
--
-- Exceção justificada: EIXO tem 4 linhas porque o Programa de Metas de São Paulo
-- tem exatamente 4 eixos (Universo SP, Viver São Paulo, Cidade Empreendedora e
-- Capital do Futuro); o domínio não tem 5 ocorrências.
-- =====================================================================

-- EIXO (4 linhas)
INSERT INTO EIXO (id, nome) VALUES (1, 'Universo SP');
INSERT INTO EIXO (id, nome) VALUES (2, 'Viver São Paulo');
INSERT INTO EIXO (id, nome) VALUES (3, 'Cidade Empreendedora');
INSERT INTO EIXO (id, nome) VALUES (4, 'Capital do Futuro');

-- SUBPREFEITURA (7 linhas)
INSERT INTO SUBPREFEITURA (id, nome) VALUES (1, 'Itaquera');
INSERT INTO SUBPREFEITURA (id, nome) VALUES (2, 'Capela do Socorro');
INSERT INTO SUBPREFEITURA (id, nome) VALUES (3, 'Butantã');
INSERT INTO SUBPREFEITURA (id, nome) VALUES (4, 'Sé');
INSERT INTO SUBPREFEITURA (id, nome) VALUES (5, 'Penha');
INSERT INTO SUBPREFEITURA (id, nome) VALUES (6, 'Santana/Tucuruvi');
INSERT INTO SUBPREFEITURA (id, nome) VALUES (7, 'Pinheiros');

-- USUARIO_ADMIN (5 linhas)
INSERT INTO USUARIO_ADMIN (id, nome, email, senha_hash, data_cadastro) VALUES (1, 'Administrador de Teste 1', 'admin1@example.com', '$2a$10$CuGLFVHg7TI3KtV39tmw1U8wgF74f8RfU0bRk5oAD1qVtwqtM/FFi', DATE '2026-10-20');
INSERT INTO USUARIO_ADMIN (id, nome, email, senha_hash, data_cadastro) VALUES (2, 'Administrador de Teste 2', 'admin2@example.com', '$2a$10$ApOglAOinJGrutD8BWyuyF4Bgs6Cphxt4FOlx083KQZWGkSXdriDc', DATE '2026-10-20');
INSERT INTO USUARIO_ADMIN (id, nome, email, senha_hash, data_cadastro) VALUES (3, 'Administrador de Teste 3', 'admin3@example.com', '$2a$10$OcknzMVrlIqxUz7sIDu6rj2Jr2AKph.FNTPmf/BaEi5IE/0NP8t0L', DATE '2026-10-21');
INSERT INTO USUARIO_ADMIN (id, nome, email, senha_hash, data_cadastro) VALUES (4, 'Administrador de Teste 4', 'admin4@example.com', '$2a$10$AavNT4UREyu3VoHm6NswOJbPVHjrHm2IrZnPn/LvG6Jg7Y.7k7rN9', DATE '2026-10-22');
INSERT INTO USUARIO_ADMIN (id, nome, email, senha_hash, data_cadastro) VALUES (5, 'Administrador de Teste 5', 'admin5@example.com', '$2a$10$5BJ.a73Me5rzOhPAhUzfEI9U5FOznDoA/siwJ8Os5d8XpcDUb6Teb', DATE '2026-10-22');

-- META (12 linhas)
INSERT INTO META (id, numero, texto, eixo_id, status, valor_atual, valor_alvo, regionalizacao, fonte, criado_por) VALUES (1, 1, 'Ampliar a rede de creches para atender mais crianças pequenas', 1, 'EM_PROGRESSO', 12, 40, 'REGIONALIZADA', 'Programa de Metas (fictício)', 1);
INSERT INTO META (id, numero, texto, eixo_id, status, valor_atual, valor_alvo, regionalizacao, fonte, criado_por) VALUES (2, 2, 'Reformar unidades básicas de saúde da cidade', 1, 'EM_PROGRESSO', 8, 30, 'REGIONALIZADA', 'Programa de Metas (fictício)', 2);
INSERT INTO META (id, numero, texto, eixo_id, status, valor_atual, valor_alvo, regionalizacao, fonte, criado_por) VALUES (3, 3, 'Reduzir a fila de consultas com especialistas', 1, 'EM_PLANEJAMENTO', NULL, NULL, 'NAO_DIVULGADA', 'Programa de Metas (fictício)', 3);
INSERT INTO META (id, numero, texto, eixo_id, status, valor_atual, valor_alvo, regionalizacao, fonte, criado_por) VALUES (4, 4, 'Plantar árvores em ruas e praças', 2, 'ATINGIDA', 5000, 5000, 'REGIONALIZADA', 'Programa de Metas (fictício)', 4);
INSERT INTO META (id, numero, texto, eixo_id, status, valor_atual, valor_alvo, regionalizacao, fonte, criado_por) VALUES (5, 5, 'Entregar unidades de moradia popular', 2, 'EM_PROGRESSO', 600, 2000, 'REGIONALIZADA', 'Programa de Metas (fictício)', 5);
INSERT INTO META (id, numero, texto, eixo_id, status, valor_atual, valor_alvo, regionalizacao, fonte, criado_por) VALUES (6, 6, 'Ampliar os corredores de ônibus', 2, 'EM_PLANEJAMENTO', 0, 15, 'NAO_DIVULGADA', 'Programa de Metas (fictício)', 1);
INSERT INTO META (id, numero, texto, eixo_id, status, valor_atual, valor_alvo, regionalizacao, fonte, criado_por) VALUES (7, 7, 'Capacitar jovens em cursos profissionalizantes', 3, 'EM_PROGRESSO', 3000, 10000, 'NAO_REGIONALIZAVEL', 'Programa de Metas (fictício)', 2);
INSERT INTO META (id, numero, texto, eixo_id, status, valor_atual, valor_alvo, regionalizacao, fonte, criado_por) VALUES (8, 8, 'Apoiar microempreendedores com crédito', 3, 'EM_PROGRESSO', 900, 1500, 'NAO_REGIONALIZAVEL', 'Programa de Metas (fictício)', 3);
INSERT INTO META (id, numero, texto, eixo_id, status, valor_atual, valor_alvo, regionalizacao, fonte, criado_por) VALUES (9, 9, 'Revitalizar feiras livres e mercados', 3, 'EM_PLANEJAMENTO', NULL, NULL, 'REGIONALIZADA', 'Programa de Metas (fictício)', 4);
INSERT INTO META (id, numero, texto, eixo_id, status, valor_atual, valor_alvo, regionalizacao, fonte, criado_por) VALUES (10, 10, 'Levar internet gratuita a praças e bibliotecas', 4, 'EM_PROGRESSO', 70, 100, 'REGIONALIZADA', 'Programa de Metas (fictício)', 5);
INSERT INTO META (id, numero, texto, eixo_id, status, valor_atual, valor_alvo, regionalizacao, fonte, criado_por) VALUES (11, 11, 'Digitalizar os serviços da prefeitura', 4, 'ATINGIDA', 25, 25, 'NAO_REGIONALIZAVEL', 'Programa de Metas (fictício)', 1);
INSERT INTO META (id, numero, texto, eixo_id, status, valor_atual, valor_alvo, regionalizacao, fonte, criado_por) VALUES (12, 12, 'Ampliar a iluminação em LED nas ruas', 2, 'EM_PROGRESSO', 40000, 100000, 'REGIONALIZADA', 'Programa de Metas (fictício)', 2);

-- PROMESSA (15 linhas)
INSERT INTO PROMESSA (id, texto, eixo_id, meta_id, fonte, criado_por) VALUES (1, 'Construir creches em todos os bairros', 1, 1, 'Plano de governo (fictício)', 1);
INSERT INTO PROMESSA (id, texto, eixo_id, meta_id, fonte, criado_por) VALUES (2, 'Acabar com a fila das creches', 1, 1, 'Plano de governo (fictício)', 2);
INSERT INTO PROMESSA (id, texto, eixo_id, meta_id, fonte, criado_por) VALUES (3, 'Reformar todos os postos de saúde', 1, 2, 'Plano de governo (fictício)', 3);
INSERT INTO PROMESSA (id, texto, eixo_id, meta_id, fonte, criado_por) VALUES (4, 'Acabar com a fila de especialistas', 1, 3, 'Plano de governo (fictício)', 4);
INSERT INTO PROMESSA (id, texto, eixo_id, meta_id, fonte, criado_por) VALUES (5, 'Plantar mais árvores em toda a cidade', 2, 4, 'Plano de governo (fictício)', 5);
INSERT INTO PROMESSA (id, texto, eixo_id, meta_id, fonte, criado_por) VALUES (6, 'Entregar casas populares para famílias de baixa renda', 2, 5, 'Plano de governo (fictício)', 1);
INSERT INTO PROMESSA (id, texto, eixo_id, meta_id, fonte, criado_por) VALUES (7, 'Criar faixa exclusiva de ônibus nos corredores principais', 2, 6, 'Plano de governo (fictício)', 2);
INSERT INTO PROMESSA (id, texto, eixo_id, meta_id, fonte, criado_por) VALUES (8, 'Oferecer cursos gratuitos para jovens', 3, 7, 'Plano de governo (fictício)', 3);
INSERT INTO PROMESSA (id, texto, eixo_id, meta_id, fonte, criado_por) VALUES (9, 'Facilitar crédito para pequenos negócios', 3, 8, 'Plano de governo (fictício)', 4);
INSERT INTO PROMESSA (id, texto, eixo_id, meta_id, fonte, criado_por) VALUES (10, 'Wi-Fi gratuito em todas as praças', 4, 10, 'Plano de governo (fictício)', 5);
INSERT INTO PROMESSA (id, texto, eixo_id, meta_id, fonte, criado_por) VALUES (11, 'Fazer um grande parque linear na zona leste', 2, NULL, 'Plano de governo (fictício)', 1);
INSERT INTO PROMESSA (id, texto, eixo_id, meta_id, fonte, criado_por) VALUES (12, 'Tarifa zero no transporte aos domingos', 2, NULL, 'Plano de governo (fictício)', 2);
INSERT INTO PROMESSA (id, texto, eixo_id, meta_id, fonte, criado_por) VALUES (13, 'Criar o museu da cidade', 1, NULL, 'Plano de governo (fictício)', 3);
INSERT INTO PROMESSA (id, texto, eixo_id, meta_id, fonte, criado_por) VALUES (14, 'Reduzir o IPTU de moradias populares', 2, NULL, 'Plano de governo (fictício)', 4);
INSERT INTO PROMESSA (id, texto, eixo_id, meta_id, fonte, criado_por) VALUES (15, 'Iluminar todas as ruas com LED', 2, 12, 'Plano de governo (fictício)', 5);

-- META_SUBPREFEITURA (20 linhas)
INSERT INTO META_SUBPREFEITURA (meta_id, subprefeitura_id) VALUES (1, 1);
INSERT INTO META_SUBPREFEITURA (meta_id, subprefeitura_id) VALUES (1, 2);
INSERT INTO META_SUBPREFEITURA (meta_id, subprefeitura_id) VALUES (1, 5);
INSERT INTO META_SUBPREFEITURA (meta_id, subprefeitura_id) VALUES (2, 1);
INSERT INTO META_SUBPREFEITURA (meta_id, subprefeitura_id) VALUES (2, 3);
INSERT INTO META_SUBPREFEITURA (meta_id, subprefeitura_id) VALUES (4, 3);
INSERT INTO META_SUBPREFEITURA (meta_id, subprefeitura_id) VALUES (4, 4);
INSERT INTO META_SUBPREFEITURA (meta_id, subprefeitura_id) VALUES (4, 7);
INSERT INTO META_SUBPREFEITURA (meta_id, subprefeitura_id) VALUES (5, 1);
INSERT INTO META_SUBPREFEITURA (meta_id, subprefeitura_id) VALUES (5, 5);
INSERT INTO META_SUBPREFEITURA (meta_id, subprefeitura_id) VALUES (9, 4);
INSERT INTO META_SUBPREFEITURA (meta_id, subprefeitura_id) VALUES (9, 6);
INSERT INTO META_SUBPREFEITURA (meta_id, subprefeitura_id) VALUES (10, 2);
INSERT INTO META_SUBPREFEITURA (meta_id, subprefeitura_id) VALUES (10, 3);
INSERT INTO META_SUBPREFEITURA (meta_id, subprefeitura_id) VALUES (10, 6);
INSERT INTO META_SUBPREFEITURA (meta_id, subprefeitura_id) VALUES (10, 7);
INSERT INTO META_SUBPREFEITURA (meta_id, subprefeitura_id) VALUES (12, 1);
INSERT INTO META_SUBPREFEITURA (meta_id, subprefeitura_id) VALUES (12, 2);
INSERT INTO META_SUBPREFEITURA (meta_id, subprefeitura_id) VALUES (12, 6);
INSERT INTO META_SUBPREFEITURA (meta_id, subprefeitura_id) VALUES (12, 7);

-- PROMESSA_SUBPREFEITURA (9 linhas)
INSERT INTO PROMESSA_SUBPREFEITURA (promessa_id, subprefeitura_id) VALUES (11, 1);
INSERT INTO PROMESSA_SUBPREFEITURA (promessa_id, subprefeitura_id) VALUES (11, 5);
INSERT INTO PROMESSA_SUBPREFEITURA (promessa_id, subprefeitura_id) VALUES (12, 1);
INSERT INTO PROMESSA_SUBPREFEITURA (promessa_id, subprefeitura_id) VALUES (12, 4);
INSERT INTO PROMESSA_SUBPREFEITURA (promessa_id, subprefeitura_id) VALUES (12, 7);
INSERT INTO PROMESSA_SUBPREFEITURA (promessa_id, subprefeitura_id) VALUES (13, 4);
INSERT INTO PROMESSA_SUBPREFEITURA (promessa_id, subprefeitura_id) VALUES (13, 7);
INSERT INTO PROMESSA_SUBPREFEITURA (promessa_id, subprefeitura_id) VALUES (14, 1);
INSERT INTO PROMESSA_SUBPREFEITURA (promessa_id, subprefeitura_id) VALUES (14, 2);

COMMIT;
