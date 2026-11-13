package br.com.zynox.pratodossp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Formato padrao de erro devolvido por toda a API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Resposta padronizada de erro")
public record ErroResponse(
        @Schema(example = "2026-11-13T10:00:00") LocalDateTime timestamp,
        @Schema(example = "404") int status,
        @Schema(example = "NOT_FOUND") String error,
        @Schema(example = "Promessa não encontrada") String message,
        @Schema(example = "/promessas/999") String path,
        @Schema(description = "Erros por campo (somente em validações)") List<CampoErro> campos) {

    public record CampoErro(String campo, String mensagem) {
    }
}
