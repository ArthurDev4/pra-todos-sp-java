package br.com.zynox.pratodossp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Tabela META (01_ddl.sql). O percentual NAO e coluna: e calculado no MetaBO.
 */
@Entity
@Table(name = "META")
public class Meta {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqMeta")
    @SequenceGenerator(name = "seqMeta", sequenceName = "SEQ_META", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NUMERO", nullable = false, unique = true)
    private Integer numero;

    @Column(name = "TEXTO", nullable = false, length = 300)
    private String texto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "EIXO_ID", nullable = false)
    private Eixo eixo;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private StatusMeta status;

    @Column(name = "VALOR_ATUAL", precision = 12, scale = 2)
    private BigDecimal valorAtual;

    @Column(name = "VALOR_ALVO", precision = 12, scale = 2)
    private BigDecimal valorAlvo;

    @Enumerated(EnumType.STRING)
    @Column(name = "REGIONALIZACAO", nullable = false, length = 20)
    private Regionalizacao regionalizacao;

    /** De onde vem a informacao da meta (texto ou link). Obrigatoria; sempre mostrada na tela. */
    @Column(name = "FONTE", nullable = false, length = 200)
    private String fonte;

    /** Admin que cadastrou (CRIADO_POR). Nao aparece no JSON. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CRIADO_POR", nullable = false, updatable = false)
    private UsuarioAdmin criadoPor;

    /** Ligacao muitos-para-muitos META_SUBPREFEITURA. */
    @ManyToMany
    @JoinTable(name = "META_SUBPREFEITURA",
            joinColumns = @JoinColumn(name = "META_ID"),
            inverseJoinColumns = @JoinColumn(name = "SUBPREFEITURA_ID"))
    private Set<Subprefeitura> subprefeituras = new LinkedHashSet<>();

    public Meta() {
    }

    public boolean atendeSubprefeitura(Long subprefeituraId) {
        return subprefeituras.stream().anyMatch(s -> s.getId().equals(subprefeituraId));
    }

    public void definirSubprefeituras(Set<Subprefeitura> novas) {
        subprefeituras.clear();
        subprefeituras.addAll(novas);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public Eixo getEixo() { return eixo; }
    public void setEixo(Eixo eixo) { this.eixo = eixo; }
    public StatusMeta getStatus() { return status; }
    public void setStatus(StatusMeta status) { this.status = status; }
    public BigDecimal getValorAtual() { return valorAtual; }
    public void setValorAtual(BigDecimal valorAtual) { this.valorAtual = valorAtual; }
    public BigDecimal getValorAlvo() { return valorAlvo; }
    public void setValorAlvo(BigDecimal valorAlvo) { this.valorAlvo = valorAlvo; }
    public Regionalizacao getRegionalizacao() { return regionalizacao; }
    public void setRegionalizacao(Regionalizacao regionalizacao) { this.regionalizacao = regionalizacao; }
    public String getFonte() { return fonte; }
    public void setFonte(String fonte) { this.fonte = fonte; }
    public UsuarioAdmin getCriadoPor() { return criadoPor; }
    public void setCriadoPor(UsuarioAdmin criadoPor) { this.criadoPor = criadoPor; }
    public Set<Subprefeitura> getSubprefeituras() { return subprefeituras; }
}
