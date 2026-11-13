package br.com.zynox.pratodossp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Eixo tematico do Programa de Metas (ex.: "Viver São Paulo"). Tabela de referencia
 * do modelo da equipe: a aplicacao apenas consulta.
 */
@Entity
@Table(name = "EIXO")
public class Eixo {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOME", nullable = false, length = 40, unique = true)
    private String nome;

    protected Eixo() {
    }

    public Eixo(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
}
