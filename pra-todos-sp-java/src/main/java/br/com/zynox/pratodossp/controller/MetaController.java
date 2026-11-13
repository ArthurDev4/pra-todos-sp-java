package br.com.zynox.pratodossp.controller;

import br.com.zynox.pratodossp.bo.MetaBO;
import br.com.zynox.pratodossp.dto.ErroResponse;
import br.com.zynox.pratodossp.dto.MetaRequest;
import br.com.zynox.pratodossp.dto.MetaResponse;
import br.com.zynox.pratodossp.model.StatusMeta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
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
 * Metas: GET publico; POST, PUT e DELETE exigem administrador (token JWT).
 */
@RestController
@RequestMapping("/metas")
@Tag(name = "Metas", description = "US04/US06 (consulta) e US10/UC11 (administração)")
public class MetaController {

    private static final String ERRO = "application/json";

    private final MetaBO metaBO;

    public MetaController(MetaBO metaBO) {
        this.metaBO = metaBO;
    }

    @GetMapping
    @Operation(summary = "Lista metas com percentual (filtros opcionais)")
    public List<MetaResponse> listar(
            @Parameter(description = "ID do eixo", example = "1") @RequestParam(required = false) Long eixoId,
            @Parameter(description = "EM_PLANEJAMENTO, EM_PROGRESSO ou ATINGIDA") @RequestParam(required = false) StatusMeta status,
            @Parameter(description = "ID da subprefeitura", example = "1") @RequestParam(required = false) Long subprefeituraId) {
        return metaBO.listar(eixoId, status, subprefeituraId);
    }

    @GetMapping(value = "/export", produces = "text/csv")
    @Operation(summary = "Exporta as metas em CSV (Data Science)")
    public ResponseEntity<String> exportar() {
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=metas_export.csv")
                .contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
                .body(metaBO.exportarCsv());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Uma meta com percentual e subprefeituras")
    @ApiResponse(responseCode = "200", description = "Meta encontrada")
    @ApiResponse(responseCode = "404", description = "Meta não encontrada",
            content = @Content(mediaType = ERRO, schema = @Schema(implementation = ErroResponse.class)))
    public MetaResponse buscar(@PathVariable Long id) {
        return metaBO.buscar(id);
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cadastra meta (admin)",
            description = "Valores juntos ou nenhum; alvo > 0; REGIONALIZADA exige subprefeituraIds; "
                    + "NAO_REGIONALIZAVEL e NAO_DIVULGADA não aceitam subprefeituras.")
    @ApiResponse(responseCode = "201", description = "Meta criada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(mediaType = ERRO, schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "401", description = "Sem login",
            content = @Content(mediaType = ERRO, schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409", description = "Número de meta já existe",
            content = @Content(mediaType = ERRO, schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<MetaResponse> cadastrar(@Valid @RequestBody MetaRequest req,
                                                  @Parameter(hidden = true) Authentication admin) {
        MetaResponse criada = metaBO.cadastrar(req, admin.getName());
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criada.id()).toUri()).body(criada);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Edita meta (admin)")
    @ApiResponse(responseCode = "200", description = "Meta atualizada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(mediaType = ERRO, schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "Meta não encontrada",
            content = @Content(mediaType = ERRO, schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409", description = "Número de meta já existe",
            content = @Content(mediaType = ERRO, schema = @Schema(implementation = ErroResponse.class)))
    public MetaResponse atualizar(@PathVariable Long id, @Valid @RequestBody MetaRequest req) {
        return metaBO.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Apaga meta (admin); 409 se houver promessas ligadas")
    @ApiResponse(responseCode = "204", description = "Meta apagada")
    @ApiResponse(responseCode = "404", description = "Meta não encontrada",
            content = @Content(mediaType = ERRO, schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409", description = "Meta tem promessas ligadas",
            content = @Content(mediaType = ERRO, schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        metaBO.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/subprefeituras/{subId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Liga a meta (REGIONALIZADA) a uma subprefeitura (admin)")
    @ApiResponse(responseCode = "200", description = "Ligação criada")
    @ApiResponse(responseCode = "400", description = "Meta não é REGIONALIZADA",
            content = @Content(mediaType = ERRO, schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "Meta ou subprefeitura não encontrada",
            content = @Content(mediaType = ERRO, schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409", description = "Ligação já existe",
            content = @Content(mediaType = ERRO, schema = @Schema(implementation = ErroResponse.class)))
    public MetaResponse ligar(@PathVariable Long id, @PathVariable Long subId) {
        return metaBO.ligarSubprefeitura(id, subId);
    }

    @DeleteMapping("/{id}/subprefeituras/{subId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Desliga a meta de uma subprefeitura (admin)")
    @ApiResponse(responseCode = "200", description = "Ligação removida")
    @ApiResponse(responseCode = "404", description = "Meta, subprefeitura ou ligação não encontrada",
            content = @Content(mediaType = ERRO, schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409", description = "Seria a última subprefeitura de uma meta REGIONALIZADA",
            content = @Content(mediaType = ERRO, schema = @Schema(implementation = ErroResponse.class)))
    public MetaResponse desligar(@PathVariable Long id, @PathVariable Long subId) {
        return metaBO.desligarSubprefeitura(id, subId);
    }
}
