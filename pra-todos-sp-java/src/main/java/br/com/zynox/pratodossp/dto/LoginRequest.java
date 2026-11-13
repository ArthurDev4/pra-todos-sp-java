package br.com.zynox.pratodossp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Schema(example = "admin@pratodossp.dev") String email,

        @NotBlank(message = "A senha é obrigatória")
        @Schema(example = "sua-senha") String senha) {
}
