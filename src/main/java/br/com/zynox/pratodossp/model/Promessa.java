package br.com.zynox.pratodossp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Tabela PROMESSA (01_ddl.sql). META_ID vazio = "nao virou meta".
 * Regiao: a promessa que virou meta herda a regiao da META (META_SUBPREFEITURA);
 * a que nao virou meta tem regiao propria em PROMESSA_SUBPREFEITURA.
 */
@Entity
@Table(name = "PROMESSA")
public class Promessa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqPromessa")
    @SequenceGenerator(name = "seqPromessa", sequenceName = "SEQ_PROMESSA", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TEXTO", nullable = false, length = 300)
    private String texto;

    /** Area da promessa (EIXO_ID). Se a promessa virou meta, e o mesmo eixo da meta (regra do BO). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "EIXO_ID", nullable = false)
    private Eixo eixo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "META_ID")
    private Meta meta;

    /** De onde vem a promessa (texto ou link), por exemplo o plano de governo. Obrigatoria. */
    @Column(name = "FONTE", nullable = false, length = 200)
    private String fonte;

    /** Admin que cadastrou (CRIADO_POR). Nao aparece no JSON. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CRIADO_POR", nullable = false, updatable = false)
    private UsuarioAdmin criadoPor;

    /** Ligacao muitos-para-muitos PROMESSA_SUBPREFEITURA (so para promessa sem meta). */
    @ManyToMany
    @JoinTable(name = "PROMESSA_SUBPREFEITURA",
            joinColumns = @JoinColumn(name = "PROMESSA_ID"),
            inverseJoinColumns = @JoinColumn(name = "SUBPREFEITURA_ID"))
    private Set<Subprefeitura> subprefeituras = new LinkedHashSet<>();

    public Promessa() {
    }

    public EstadoPromessa getEstado() {
        return meta == null ? EstadoPromessa.NAO_VIROU_META : EstadoPromessa.VIROU_META;
    }

    public boolean atingeSubprefeitura(Long subprefeituraId) {
        return subprefeituras.stream().anyMatch(s -> s.getId().equals(subprefeituraId));
    }

    public void definirSubprefeituras(Set<Subprefeitura> novas) {
        subprefeituras.clear();
        subprefeituras.addAll(novas);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public Eixo getEixo() { return eixo; }
    public void setEixo(Eixo eixo) { this.eixo = eixo; }
    public String getFonte() { return fonte; }
    public void setFonte(String fonte) { this.fonte = fonte; }
    public Meta getMeta() { return meta; }
    public void setMeta(Meta meta) { this.meta = meta; }
    public UsuarioAdmin getCriadoPor() { return criadoPor; }
    public void setCriadoPor(UsuarioAdmin criadoPor) { this.criadoPor = criadoPor; }
    public Set<Subprefeitura> getSubprefeituras() { return subprefeituras; }
}
