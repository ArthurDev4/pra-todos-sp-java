package br.com.zynox.pratodossp.dto;

import br.com.zynox.pratodossp.model.Subprefeitura;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Subprefeitura (o bairro que o cidadão escolhe)")
public record SubprefeituraResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "Itaquera") String nome) {

    public static SubprefeituraResponse de(Subprefeitura s) {
        return new SubprefeituraResponse(s.getId(), s.getNome());
    }
}
