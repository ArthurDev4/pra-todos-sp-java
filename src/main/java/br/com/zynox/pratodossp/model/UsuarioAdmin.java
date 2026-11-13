package br.com.zynox.pratodossp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Administrador que mantem os dados (US08). Tabela USUARIO_ADMIN.
 * A senha e guardada somente como hash BCrypt. Nao existe cadastro publico de administradores.
 */
@Entity
@Table(name = "USUARIO_ADMIN")
public class UsuarioAdmin {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqUsuarioAdmin")
    @SequenceGenerator(name = "seqUsuarioAdmin", sequenceName = "SEQ_USUARIO_ADMIN", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOME", nullable = false, length = 100)
    private String nome;

    @Column(name = "EMAIL", nullable = false, length = 120, unique = true)
    private String email;

    @Column(name = "SENHA_HASH", nullable = false, length = 100)
    private String senhaHash;

    /** Preenchida pelo Oracle (DEFAULT SYSDATE). */
    @Column(name = "DATA_CADASTRO", insertable = false, updatable = false)
    private LocalDate dataCadastro;

    protected UsuarioAdmin() {
    }

    public UsuarioAdmin(String nome, String email, String senhaHash) {
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenhaHash() { return senhaHash; }
    public LocalDate getDataCadastro() { return dataCadastro; }
}
