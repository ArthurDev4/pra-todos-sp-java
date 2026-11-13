package br.com.zynox.pratodossp.model;

/**
 * Etapa da meta (coluna META.STATUS, CHECK CK_META_STATUS).
 * Na tela: Em planejamento / Em andamento / Concluida, sempre com icone + texto + cor (US07).
 */
public enum StatusMeta {
    EM_PLANEJAMENTO,
    EM_PROGRESSO,
    ATINGIDA
}
