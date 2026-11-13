package br.com.zynox.pratodossp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

        @NotNull(message = "A área (eixo) da promessa é obrigatória")
        @Schema(description = "ID do eixo (área). Se tiver meta, deve ser o mesmo eixo da meta", example = "2")
        Long eixoId,

        @Schema(description = "ID da meta. Vazio = não virou meta", example = "4")
        Long metaId,

        @NotBlank(message = "A fonte da promessa é obrigatória")
        @Size(max = 200, message = "A fonte deve ter no máximo 200 caracteres")
        @Schema(example = "Plano de governo (fictício)") String fonte,

        @Schema(description = "Subprefeituras da promessa sem meta", example = "[1]")
        List<Long> subprefeituraIds) {
}
