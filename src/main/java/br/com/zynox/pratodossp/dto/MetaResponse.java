package br.com.zynox.pratodossp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

/**
 * Meta completa (formato de exemplos/metas.json): resumo + ids das subprefeituras ligadas.
 */
@Schema(description = "Meta com percentual e subprefeituras")
public record MetaResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "1") Integer numero,
        @Schema(example = "Ampliar a rede de creches para atender mais crianças pequenas") String texto,
        @Schema(example = "Universo SP") String eixo,
        @Schema(example = "EM_PROGRESSO") String status,
        @Schema(example = "12") BigDecimal valorAtual,
        @Schema(example = "40") BigDecimal valorAlvo,
        @Schema(example = "30.0") BigDecimal percentual,
        @Schema(example = "REGIONALIZADA") String regionalizacao,
        @Schema(description = "IDs das subprefeituras (META_SUBPREFEITURA)", example = "[1, 2, 5]")
        List<Long> subprefeituras) {
}
