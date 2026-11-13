package br.com.zynox.pratodossp.dto;

import br.com.zynox.pratodossp.model.Eixo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Eixo temático do Programa de Metas")
public record EixoResponse(
        @Schema(example = "2") Long id,
        @Schema(example = "Viver São Paulo") String nome) {

    public static EixoResponse de(Eixo e) {
        return e == null ? null : new EixoResponse(e.getId(), e.getNome());
    }
}
