package br.com.zynox.pratodossp.dao;

import br.com.zynox.pratodossp.model.UsuarioAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Acesso a tabela USUARIO_ADMIN.
 */
@Repository
public interface UsuarioAdminDAO extends JpaRepository<UsuarioAdmin, Long> {

    Optional<UsuarioAdmin> findByEmail(String email);

    boolean existsByEmail(String email);
}
