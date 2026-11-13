package br.com.zynox.pratodossp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

/**
 * Regiao que o cidadao escolhe na Tela 1 (US01). O Agile chama de "bairro";
 * o piloto, Pinheiros, e ao mesmo tempo bairro e subprefeitura.
 * Tabela de referencia: a aplicacao apenas consulta.
 */
@Entity
@Table(name = "SUBPREFEITURA")
public class Subprefeitura {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOME", nullable = false, length = 60, unique = true)
    private String nome;

    protected Subprefeitura() {
        // exigido pelo JPA
    }

    public Subprefeitura(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }

    /** Duas regioes sao a mesma quando tem o mesmo ID (evita repetir regiao nos conjuntos). */
    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Subprefeitura s && id != null && id.equals(s.id));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
