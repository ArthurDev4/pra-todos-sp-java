package br.com.zynox.pratodossp.dao;

import br.com.zynox.pratodossp.model.Subprefeitura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Acesso a tabela SUBPREFEITURA. O Spring Data gera o SQL a partir do nome dos metodos.
 */
@Repository
public interface SubprefeituraDAO extends JpaRepository<Subprefeitura, Long> {

    /** SELECT * FROM SUBPREFEITURA ORDER BY NOME */
    List<Subprefeitura> findAllByOrderByNomeAsc();

    /** SELECT * FROM SUBPREFEITURA ORDER BY ID (ordem dos exemplos do termometro) */
    List<Subprefeitura> findAllByOrderByIdAsc();
}
