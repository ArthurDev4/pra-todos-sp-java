package br.com.zynox.pratodossp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Corpo de POST/PUT /promessas.
 * metaId vazio = "não virou meta". subprefeituraIds so vale para promessa SEM meta
 * (a promessa com meta herda a regiao da meta).
 */
@Schema(description = "Cadastro/edição de promessa")
public record PromessaRequest(
        @NotBlank(message = "O texto da promessa é obrigatório")
        @Size(max = 300, message = "O texto da promessa deve ter no máximo 300 caracteres")
        @Schema(example = "Construir uma praça com academia ao ar livre") String texto,

        @Schema(description = "ID da meta. Vazio = não virou meta", example = "4")
        Long metaId,

        @Schema(description = "Subprefeituras da promessa sem meta", example = "[1]")
        List<Long> subprefeituraIds) {
}
