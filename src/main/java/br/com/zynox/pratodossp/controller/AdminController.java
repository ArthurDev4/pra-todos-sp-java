package br.com.zynox.pratodossp.controller;

import br.com.zynox.pratodossp.bo.AdminBO;
import br.com.zynox.pratodossp.dto.AdminRequest;
import br.com.zynox.pratodossp.dto.AdminResponse;
import br.com.zynox.pratodossp.dto.ErroResponse;
import br.com.zynox.pratodossp.dto.LoginRequest;
import br.com.zynox.pratodossp.dto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Administrador", description = "US08 - login e cadastro de administradores")
public class AdminController {

    private static final String ERRO = "application/json";

    private final AdminBO adminBO;

    public AdminController(AdminBO adminBO) {
        this.adminBO = adminBO;
    }

    @PostMapping("/login")
    @Operation(summary = "Login: confere a senha (BCrypt) e devolve um token JWT")
    @ApiResponse(responseCode = "200", description = "Login realizado")
    @ApiResponse(responseCode = "400", description = "E-mail ou senha ausentes",
            content = @Content(mediaType = ERRO, schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos",
            content = @Content(mediaType = ERRO, schema = @Schema(implementation = ErroResponse.class)))
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return adminBO.autenticar(req);
    }

    @PostMapping("/admins")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cria um administrador (exige login de admin); a senha vira hash BCrypt")
    @ApiResponse(responseCode = "201", description = "Admin criado (sem senha na resposta)")
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(mediaType = ERRO, schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "401", description = "Sem login",
            content = @Content(mediaType = ERRO, schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409", description = "E-mail já cadastrado",
            content = @Content(mediaType = ERRO, schema = @Schema(implementation = ErroResponse.class)))
    public AdminResponse criar(@Valid @RequestBody AdminRequest req) {
        return adminBO.criar(req);
    }
}
