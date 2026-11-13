package br.com.zynox.pratodossp.dto;

import br.com.zynox.pratodossp.model.Regionalizacao;
import br.com.zynox.pratodossp.model.StatusMeta;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * Corpo de POST/PUT /metas. Limites iguais as colunas do 01_ddl.sql.
 * subprefeituraIds e opcional: so pode ser usado com REGIONALIZADA (e nesse caso e obrigatorio).
 */
@Schema(description = "Cadastro/edição de meta")
public record MetaRequest(
        @NotNull(message = "O número da meta é obrigatório")
        @Positive(message = "O número da meta deve ser maior que zero")
        @Max(value = 9999, message = "O número da meta deve ter no máximo 4 dígitos")
        @Schema(example = "13") Integer numero,

        @NotBlank(message = "O texto da meta é obrigatório")
        @Size(max = 300, message = "O texto da meta deve ter no máximo 300 caracteres")
        @Schema(example = "Construir 5 novos parques urbanos") String texto,

        @NotNull(message = "O eixo da meta é obrigatório")
        @Schema(example = "2") Long eixoId,

        @NotNull(message = "O status da meta é obrigatório")
        @Schema(example = "EM_PROGRESSO") StatusMeta status,

        @PositiveOrZero(message = "O valor atual não pode ser negativo")
        @Digits(integer = 10, fraction = 2, message = "Valor atual: até 10 dígitos e 2 casas decimais")
        @Schema(example = "2") BigDecimal valorAtual,

        @Positive(message = "O valor alvo deve ser maior que zero")
        @Digits(integer = 10, fraction = 2, message = "Valor alvo: até 10 dígitos e 2 casas decimais")
        @Schema(example = "5") BigDecimal valorAlvo,

        @NotNull(message = "A regionalização da meta é obrigatória")
        @Schema(example = "REGIONALIZADA") Regionalizacao regionalizacao,

        @Schema(description = "IDs das subprefeituras (somente para REGIONALIZADA)", example = "[1, 4]")
        List<Long> subprefeituraIds) {
}
