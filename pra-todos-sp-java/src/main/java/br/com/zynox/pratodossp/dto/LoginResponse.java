package br.com.zynox.pratodossp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token JWT para usar no cabeçalho Authorization: Bearer <token>")
public record LoginResponse(
        @Schema(example = "eyJhbGciOiJIUzI1NiJ9...") String token,
        @Schema(example = "Bearer") String tipo,
        @Schema(example = "7200") long expiraEmSegundos,
        AdminResponse admin) {
}
