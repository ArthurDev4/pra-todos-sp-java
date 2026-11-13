package br.com.zynox.pratodossp.dao;

import br.com.zynox.pratodossp.model.Eixo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Acesso a tabela EIXO. */
@Repository
public interface EixoDAO extends JpaRepository<Eixo, Long> {

    List<Eixo> findAllByOrderByIdAsc();
}
