package br.com.zynox.pratodossp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Meta dentro de uma promessa (formato de exemplos/promessas.json e do termometro).
 * eixo, status e regionalizacao sao textos; percentual e null quando nao ha valor alvo.
 */
@Schema(description = "Meta resumida (dentro da promessa)")
public record MetaResumoResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "1") Integer numero,
        @Schema(example = "Ampliar a rede de creches para atender mais crianças pequenas") String texto,
        @Schema(example = "Universo SP") String eixo,
        @Schema(example = "EM_PROGRESSO") String status,
        @Schema(example = "12") BigDecimal valorAtual,
        @Schema(example = "40") BigDecimal valorAlvo,
        @Schema(example = "30.0") BigDecimal percentual,
        @Schema(example = "REGIONALIZADA") String regionalizacao,
        @Schema(example = "Programa de Metas (fictício)") String fonte) {
}
