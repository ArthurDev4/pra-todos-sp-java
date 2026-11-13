package br.com.zynox.pratodossp.dto;

import br.com.zynox.pratodossp.model.UsuarioAdmin;
import io.swagger.v3.oas.annotations.media.Schema;

/** Dados publicos do administrador. Nunca inclui a senha/hash. */
public record AdminResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "Administrador") String nome,
        @Schema(example = "admin@pratodossp.dev") String email) {

    public static AdminResponse de(UsuarioAdmin u) {
        return new AdminResponse(u.getId(), u.getNome(), u.getEmail());
    }
}
