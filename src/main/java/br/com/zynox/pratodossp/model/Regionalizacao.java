package br.com.zynox.pratodossp.model;

/**
 * Regionalizacao da meta (coluna META.REGIONALIZACAO, CHECK CK_META_REGIONALIZACAO).
 * <ul>
 *   <li>REGIONALIZADA: tem subprefeituras em META_SUBPREFEITURA (pelo menos uma).</li>
 *   <li>NAO_REGIONALIZAVEL: vale para a cidade toda; nao tem ligacao.</li>
 *   <li>NAO_DIVULGADA: regionalizacao ainda nao divulgada; nao tem ligacao.</li>
 * </ul>
 */
public enum Regionalizacao {
    REGIONALIZADA,
    NAO_REGIONALIZAVEL,
    NAO_DIVULGADA;

    /** Somente metas REGIONALIZADAS podem (e devem) ter subprefeituras ligadas. */
    public boolean aceitaRegioes() {
        return this == REGIONALIZADA;
    }
}
