package br.com.zynox.pratodossp.model;

/**
 * Estado de uma promessa no termometro (campo "estado" do JSON). Nao e uma coluna:
 * e derivado de META_ID (preenchido = VIROU_META, vazio = NAO_VIROU_META).
 */
public enum EstadoPromessa {
    VIROU_META,
    NAO_VIROU_META
}
