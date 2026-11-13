package br.com.zynox.pratodossp.controller;

import br.com.zynox.pratodossp.bo.EixoBO;
import br.com.zynox.pratodossp.dto.EixoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Eixos (público)", description = "Os 4 eixos do Programa de Metas")
public class EixoController {

    private final EixoBO eixoBO;

    public EixoController(EixoBO eixoBO) {
        this.eixoBO = eixoBO;
    }

    @GetMapping("/eixos")
    @Operation(summary = "Lista os eixos")
    public List<EixoResponse> listar() {
        return eixoBO.listar();
    }
}
