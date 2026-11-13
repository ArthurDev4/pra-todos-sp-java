package br.com.zynox.pratodossp.controller;

import br.com.zynox.pratodossp.bo.SubprefeituraBO;
import br.com.zynox.pratodossp.bo.TermometroBO;
import br.com.zynox.pratodossp.dto.ErroResponse;
import br.com.zynox.pratodossp.dto.SubprefeituraResponse;
import br.com.zynox.pratodossp.dto.TermometroResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Consultas publicas do cidadao: bairros e termometro (sem login).
 * O Controller so recebe a requisicao e chama o BO: nenhuma regra de negocio aqui.
 */
@RestController
@Tag(name = "Termômetro (público)", description = "US01 escolher bairro, US02/US03 promessas do bairro")
public class SubprefeituraController {

    private final SubprefeituraBO subprefeituraBO;
    private final TermometroBO termometroBO;

    public SubprefeituraController(SubprefeituraBO subprefeituraBO, TermometroBO termometroBO) {
        this.subprefeituraBO = subprefeituraBO;
        this.termometroBO = termometroBO;
    }

    @GetMapping("/subprefeituras")
    @Operation(summary = "Lista os bairros (subprefeituras) para o cidadão escolher")
    public List<SubprefeituraResponse> listar() {
        return subprefeituraBO.listar();
    }

    @GetMapping("/subprefeituras/{id}/termometro")
    @Operation(summary = "Termômetro do bairro: promessas que viraram meta e as que não viraram")
    @ApiResponse(responseCode = "200", description = "Termômetro da subprefeitura")
    @ApiResponse(responseCode = "404", description = "Subprefeitura não encontrada",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public TermometroResponse termometro(@PathVariable Long id) {
        return termometroBO.daSubprefeitura(id);
    }

    @GetMapping("/termometro")
    @Operation(summary = "Termômetro de todas as subprefeituras")
    public List<TermometroResponse> termometroTodas() {
        return termometroBO.todas();
    }
}
