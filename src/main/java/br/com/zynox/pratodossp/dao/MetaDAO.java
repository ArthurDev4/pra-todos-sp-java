package br.com.zynox.pratodossp.dao;

import br.com.zynox.pratodossp.model.Meta;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Acesso as tabelas META e META_SUBPREFEITURA.
 */
@Repository
public interface MetaDAO extends JpaRepository<Meta, Long> {

    /** Lista as metas pelo numero, ja carregando eixo e regioes (evita N+1 consultas). */
    @EntityGraph(attributePaths = {"eixo", "subprefeituras"})
    List<Meta> findAllByOrderByNumeroAsc();

    /** Usado para impedir duas metas com o mesmo numero no cadastro. */
    boolean existsByNumero(Integer numero);

    /** Usado para impedir numero repetido na edicao (ignorando a propria meta). */
    boolean existsByNumeroAndIdNot(Integer numero, Long id);
}
