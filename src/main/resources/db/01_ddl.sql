-- =====================================================================
-- Pra Todos SP (CitizenTech)
-- Database Design | FIAP Global Solution 2026.2 | Engenharia de Software | Equipe Zynox
-- Script DDL (Oracle): tabelas, constraints, sequences e comentários
--
-- Como executar: abra no Oracle SQL Developer (conectado ao Oracle da FIAP)
-- e rode como script (F5). Depois rode 02_dml.sql e 03_dql.sql.
-- Arquivo salvo em UTF-8 (há acentos nos dados).
-- Nota: o DML informa os ids manualmente; as sequences já começam depois do
-- último id inserido, então novos registros da API usam SEQ_<TABELA>.NEXTVAL.
-- =====================================================================

-- Para recriar o banco do zero, descomente e rode este bloco antes:
-- DROP TABLE USUARIO_ADMIN CASCADE CONSTRAINTS;
-- DROP TABLE PROMESSA_SUBPREFEITURA CASCADE CONSTRAINTS;
-- DROP TABLE META_SUBPREFEITURA CASCADE CONSTRAINTS;
-- DROP TABLE PROMESSA CASCADE CONSTRAINTS;
-- DROP TABLE META CASCADE CONSTRAINTS;
-- DROP TABLE SUBPREFEITURA CASCADE CONSTRAINTS;
-- DROP TABLE EIXO CASCADE CONSTRAINTS;

-- DROP SEQUENCE SEQ_USUARIO_ADMIN;
-- DROP SEQUENCE SEQ_PROMESSA;
-- DROP SEQUENCE SEQ_META;
-- DROP SEQUENCE SEQ_SUBPREFEITURA;
-- DROP SEQUENCE SEQ_EIXO;

-- ---------------------------------------------------------------------
-- 1) Tabelas, chaves primárias, unicidade e regras (CHECK)
-- ---------------------------------------------------------------------

CREATE TABLE EIXO (
    id NUMBER(2) NOT NULL,
    nome VARCHAR2(40) NOT NULL,
    CONSTRAINT PK_EIXO PRIMARY KEY (id),
    CONSTRAINT UK_EIXO_NOME UNIQUE (nome)
);

CREATE TABLE SUBPREFEITURA (
    id NUMBER(3) NOT NULL,
    nome VARCHAR2(60) NOT NULL,
    CONSTRAINT PK_SUBPREFEITURA PRIMARY KEY (id),
    CONSTRAINT UK_SUBPREFEITURA_NOME UNIQUE (nome)
);

CREATE TABLE META (
    id NUMBER(4) NOT NULL,
    numero NUMBER(4) NOT NULL,
    texto VARCHAR2(300) NOT NULL,
    eixo_id NUMBER(2) NOT NULL,
    status VARCHAR2(20) DEFAULT 'EM_PLANEJAMENTO' NOT NULL,
    valor_atual NUMBER(12,2),
    valor_alvo NUMBER(12,2),
    regionalizacao VARCHAR2(20) DEFAULT 'NAO_DIVULGADA' NOT NULL,
    criado_por NUMBER(4) NOT NULL,
    CONSTRAINT PK_META PRIMARY KEY (id),
    CONSTRAINT UK_META_NUMERO UNIQUE (numero),
    CONSTRAINT CK_META_STATUS CHECK (status IN ('EM_PLANEJAMENTO', 'EM_PROGRESSO', 'ATINGIDA')),
    CONSTRAINT CK_META_REGIONALIZACAO CHECK (regionalizacao IN ('REGIONALIZADA', 'NAO_REGIONALIZAVEL', 'NAO_DIVULGADA')),
    CONSTRAINT CK_META_VALORES CHECK ((valor_atual IS NULL AND valor_alvo IS NULL) OR (valor_atual IS NOT NULL AND valor_alvo IS NOT NULL AND valor_atual >= 0 AND valor_alvo > 0))
);

CREATE TABLE PROMESSA (
    id NUMBER(6) NOT NULL,
    texto VARCHAR2(300) NOT NULL,
    meta_id NUMBER(4),
    criado_por NUMBER(4) NOT NULL,
    CONSTRAINT PK_PROMESSA PRIMARY KEY (id)
);

CREATE TABLE META_SUBPREFEITURA (
    meta_id NUMBER(4) NOT NULL,
    subprefeitura_id NUMBER(3) NOT NULL,
    CONSTRAINT PK_META_SUBPREFEITURA PRIMARY KEY (meta_id, subprefeitura_id)
);

CREATE TABLE PROMESSA_SUBPREFEITURA (
    promessa_id NUMBER(6) NOT NULL,
    subprefeitura_id NUMBER(3) NOT NULL,
    CONSTRAINT PK_PROMESSA_SUBPREFEITURA PRIMARY KEY (promessa_id, subprefeitura_id)
);

CREATE TABLE USUARIO_ADMIN (
    id NUMBER(4) NOT NULL,
    nome VARCHAR2(100) NOT NULL,
    email VARCHAR2(120) NOT NULL,
    senha_hash VARCHAR2(100) NOT NULL,
    data_cadastro DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT PK_USUARIO_ADMIN PRIMARY KEY (id),
    CONSTRAINT UK_ADMIN_EMAIL UNIQUE (email),
    CONSTRAINT CK_ADMIN_EMAIL CHECK (email LIKE '%_@_%.__%')
);

-- ---------------------------------------------------------------------
-- 2) Chaves estrangeiras (ALTER TABLE)
-- ---------------------------------------------------------------------

ALTER TABLE META
    ADD CONSTRAINT FK_META_EIXO FOREIGN KEY (eixo_id) REFERENCES EIXO (id);

ALTER TABLE META
    ADD CONSTRAINT FK_META_ADMIN FOREIGN KEY (criado_por) REFERENCES USUARIO_ADMIN (id);

ALTER TABLE PROMESSA
    ADD CONSTRAINT FK_PROMESSA_META FOREIGN KEY (meta_id) REFERENCES META (id);

ALTER TABLE PROMESSA
    ADD CONSTRAINT FK_PROMESSA_ADMIN FOREIGN KEY (criado_por) REFERENCES USUARIO_ADMIN (id);

ALTER TABLE META_SUBPREFEITURA
    ADD CONSTRAINT FK_METASUB_META FOREIGN KEY (meta_id) REFERENCES META (id);

ALTER TABLE META_SUBPREFEITURA
    ADD CONSTRAINT FK_METASUB_SUBPREF FOREIGN KEY (subprefeitura_id) REFERENCES SUBPREFEITURA (id);

ALTER TABLE PROMESSA_SUBPREFEITURA
    ADD CONSTRAINT FK_PROMSUB_PROMESSA FOREIGN KEY (promessa_id) REFERENCES PROMESSA (id);

ALTER TABLE PROMESSA_SUBPREFEITURA
    ADD CONSTRAINT FK_PROMSUB_SUBPREF FOREIGN KEY (subprefeitura_id) REFERENCES SUBPREFEITURA (id);

-- ---------------------------------------------------------------------
-- 3) Sequences (começam depois do último id do DML)
-- ---------------------------------------------------------------------

CREATE SEQUENCE SEQ_EIXO START WITH 5 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SEQ_SUBPREFEITURA START WITH 7 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SEQ_META START WITH 13 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SEQ_PROMESSA START WITH 16 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SEQ_USUARIO_ADMIN START WITH 6 INCREMENT BY 1 NOCACHE;

-- ---------------------------------------------------------------------
-- 4) Comentários das tabelas
-- ---------------------------------------------------------------------

COMMENT ON TABLE EIXO IS 'Eixos do Programa de Metas da cidade de São Paulo.';
COMMENT ON TABLE SUBPREFEITURA IS 'Subprefeituras reais da cidade de São Paulo (a região que o cidadão escolhe).';
COMMENT ON TABLE META IS 'Metas do programa (dados fictícios no formato do Programa de Metas): status em 3 etapas e resultado numérico opcional.';
COMMENT ON TABLE PROMESSA IS 'Promessas de campanha do prefeito fictício; sem meta ligada significa "não virou meta".';
COMMENT ON TABLE META_SUBPREFEITURA IS 'Liga metas às subprefeituras (muitos para muitos). Meta sem ligação aparece como "sem região definida".';
COMMENT ON TABLE PROMESSA_SUBPREFEITURA IS 'Liga promessas que não viraram meta às subprefeituras que elas atingem (muitos para muitos). Promessa de meta herda a região da meta.';
COMMENT ON TABLE USUARIO_ADMIN IS 'Administradores que cadastram e editam promessas e metas (o cidadão não tem login).';
COMMENT ON COLUMN META.status IS 'Etapa da meta: EM_PLANEJAMENTO, EM_PROGRESSO ou ATINGIDA';
COMMENT ON COLUMN META.regionalizacao IS 'REGIONALIZADA (tem subprefeituras em META_SUBPREFEITURA), NAO_REGIONALIZAVEL ou NAO_DIVULGADA (ainda não divulgada)';
