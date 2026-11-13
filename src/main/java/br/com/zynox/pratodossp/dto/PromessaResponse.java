package br.com.zynox.pratodossp.dto;

import br.com.zynox.pratodossp.model.EstadoPromessa;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Promessa no formato de exemplos/promessas.json (consumido pelo projeto Python).
 * Promessa sem meta: estado NAO_VIROU_META e meta null.
 */
@Schema(description = "Promessa com estado e meta (ou null)")
public record PromessaResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "Construir creches em todos os bairros") String texto,
        @Schema(description = "Área da promessa (eixo)", example = "Universo SP") String area,
        @Schema(example = "Plano de governo (fictício)") String fonte,
        @Schema(example = "VIROU_META") EstadoPromessa estado,
        @Schema(description = "Meta ligada; null quando não virou meta") MetaResumoResponse meta) {
}
