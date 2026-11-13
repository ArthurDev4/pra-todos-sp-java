package br.com.zynox.pratodossp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Termometro de uma subprefeitura (formato de exemplos/termometro_itaquera.json).
 */
@Schema(description = "Termômetro de promessas de uma subprefeitura")
public record TermometroResponse(
        @Schema(example = "Dados fictícios") String aviso,
        SubprefeituraResponse subprefeitura,
        @Schema(description = "Promessas que viraram meta e cuja meta atende a subprefeitura")
        List<PromessaResponse> promessas,
        @Schema(description = "Promessas sem meta ligadas à subprefeitura (PROMESSA_SUBPREFEITURA)")
        List<PromessaResponse> naoViraramMeta) {

    public static final String AVISO = "Dados fictícios";
}
