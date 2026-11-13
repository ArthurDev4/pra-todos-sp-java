package br.com.zynox.pratodossp.controller;

import br.com.zynox.pratodossp.bo.PromessaBO;
import br.com.zynox.pratodossp.dto.ErroResponse;
import br.com.zynox.pratodossp.dto.PromessaDetalheResponse;
import br.com.zynox.pratodossp.dto.PromessaRequest;
import br.com.zynox.pratodossp.dto.PromessaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/**
 * Promessas: GET publico (inclusive para o projeto Python); POST, PUT e DELETE exigem administrador.
 */
@RestController
@RequestMapping("/promessas")
@Tag(name = "Promessas", description = "US03 (consulta) e US09/US11 (administração)")
public class PromessaController {

    private static final String ERRO = "application/json";

    private final PromessaBO promessaBO;

    public PromessaController(PromessaBO promessaBO) {
        this.promessaBO = promessaBO;
    }

    @GetMapping
    @Operation(summary = "Lista promessas com estado e meta (usada pelo Python)")
    public List<PromessaResponse> listar(
            @Parameter(description = "true = só as que não viraram meta; false = só as que viraram")
            @RequestParam(required = false) Boolean semMeta,
            @Parameter(description = "ID do eixo da meta", example = "1")
            @RequestParam(required = false) Long eixoId) {
        return promessaBO.listar(semMeta, eixoId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Uma promessa com estado, meta e subprefeituras próprias")
    @ApiResponse(responseCode = "200", description = "Promessa encontrada")
    @ApiResponse(responseCode = "404", description = "Promessa não encontrada",
            content = @Content(mediaType = ERRO, schema = @Schema(implementation = ErroResponse.class)))
    public PromessaDetalheResponse buscar(@PathVariable Long id) {
        return promessaBO.buscar(id);
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cadastra promessa (admin)",
            description = "metaId vazio = não virou meta. subprefeituraIds só para promessa sem meta.")
    @ApiResponse(responseCode = "201", description = "Promessa criada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos, meta ou subprefeitura inexistente",
            content = @Content(mediaType = ERRO, schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "401", description = "Sem login",
            content = @Content(mediaType = ERRO, schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409", description = "Promessa com meta e subprefeituras ao mesmo tempo",
            content = @Content(mediaType = ERRO, schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<PromessaDetalheResponse> cadastrar(@Valid @RequestBody PromessaRequest req,
                                                             @Parameter(hidden = true) Authentication admin) {
        PromessaDetalheResponse criada = promessaBO.cadastrar(req, admin.getName());
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criada.id()).toUri()).body(criada);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Edita promessa (admin); ligar a uma meta apaga as ligações com subprefeituras")
    @ApiResponse(responseCode = "200", description = "Promessa atualizada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(mediaType = ERRO, schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "Promessa não encontrada",
            content = @Content(mediaType = ERRO, schema = @Schema(implementation = ErroResponse.class)))
    public PromessaDetalheResponse atualizar(@PathVariable Long id, @Valid @RequestBody PromessaRequest req) {
        return promessaBO.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Apaga promessa (admin)")
    @ApiResponse(responseCode = "204", description = "Promessa apagada")
    @ApiResponse(responseCode = "404", description = "Promessa não encontrada",
            content = @Content(mediaType = ERRO, schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        promessaBO.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/subprefeituras/{subId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Liga uma promessa SEM meta a uma subprefeitura (admin)")
    @ApiResponse(responseCode = "200", description = "Ligação criada")
    @ApiResponse(responseCode = "404", description = "Promessa ou subprefeitura não encontrada",
            content = @Content(mediaType = ERRO, schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409", description = "A promessa tem meta, ou a ligação já existe",
            content = @Content(mediaType = ERRO, schema = @Schema(implementation = ErroResponse.class)))
    public PromessaDetalheResponse ligar(@PathVariable Long id, @PathVariable Long subId) {
        return promessaBO.ligarSubprefeitura(id, subId);
    }

    @DeleteMapping("/{id}/subprefeituras/{subId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Desliga uma promessa de uma subprefeitura (admin)")
    @ApiResponse(responseCode = "200", description = "Ligação removida")
    @ApiResponse(responseCode = "404", description = "Promessa, subprefeitura ou ligação não encontrada",
            content = @Content(mediaType = ERRO, schema = @Schema(implementation = ErroResponse.class)))
    public PromessaDetalheResponse desligar(@PathVariable Long id, @PathVariable Long subId) {
        return promessaBO.desligarSubprefeitura(id, subId);
    }
}
