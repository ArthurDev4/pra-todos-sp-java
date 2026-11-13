package br.com.zynox.pratodossp.dto;

import br.com.zynox.pratodossp.model.EstadoPromessa;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Uma promessa (GET /promessas/{id}): mesmo formato da lista + subprefeituras proprias
 * (PROMESSA_SUBPREFEITURA, preenchida so quando a promessa nao virou meta).
 */
@Schema(description = "Promessa com estado, meta e subprefeituras próprias")
public record PromessaDetalheResponse(
        @Schema(example = "11") Long id,
        @Schema(example = "Fazer um grande parque linear na zona leste") String texto,
        @Schema(example = "NAO_VIROU_META") EstadoPromessa estado,
        MetaResumoResponse meta,
        @Schema(description = "IDs das subprefeituras ligadas à promessa", example = "[1, 5]")
        List<Long> subprefeituras) {
}
