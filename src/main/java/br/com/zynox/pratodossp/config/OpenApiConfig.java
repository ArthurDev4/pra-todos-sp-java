package br.com.zynox.pratodossp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentacao Swagger/OpenAPI, disponivel em /swagger-ui.html.
 * Para testar rotas de administrador: faca POST /login, copie o token
 * e clique em "Authorize".
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Pra Todos SP - API")
                        .version("1.0.0")
                        .description("""
                                API da CitizenTech Pra Todos SP (Equipe Zynox - Global Solution FIAP 2026.2).
                                O cidadão escolhe sua região e vê as promessas de campanha, se viraram meta oficial,
                                o status e o resultado. **Todos os dados são fictícios** (demonstração).

                                Consultas (GET) são públicas. Cadastro, edição e exclusão exigem login de administrador:
                                use `POST /login` e informe o token em **Authorize**.
                                Tela principal: `GET /subprefeituras/{id}/termometro`."""))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}
