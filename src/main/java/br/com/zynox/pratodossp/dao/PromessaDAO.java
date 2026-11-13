package br.com.zynox.pratodossp.dao;

import br.com.zynox.pratodossp.model.Promessa;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Acesso as tabelas PROMESSA e PROMESSA_SUBPREFEITURA.
 * O @EntityGraph carrega meta e regioes na mesma consulta (evita N+1 consultas).
 */
@Repository
public interface PromessaDAO extends JpaRepository<Promessa, Long> {

    @EntityGraph(attributePaths = {"meta", "meta.eixo", "subprefeituras"})
    List<Promessa> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = {"meta", "meta.eixo", "subprefeituras"})
    Optional<Promessa> findWithDetalhesById(Long id);

    /** Quantas promessas estao vinculadas a uma meta (impede excluir meta em uso). */
    long countByMetaId(Long metaId);
}
