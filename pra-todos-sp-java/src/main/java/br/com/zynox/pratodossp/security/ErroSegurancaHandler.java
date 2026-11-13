package br.com.zynox.pratodossp.security;

import br.com.zynox.pratodossp.dto.ErroResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Responde 401 (sem login) e 403 (sem permissao) no mesmo formato JSON do
 * tratamento global de excecoes. Esses erros acontecem nos filtros de seguranca,
 * antes de chegar ao Controller, por isso nao passam pelo @RestControllerAdvice.
 */
@Component
public class ErroSegurancaHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public ErroSegurancaHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        escrever(request, response, HttpStatus.UNAUTHORIZED,
                "Faça login como administrador para acessar este recurso");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        escrever(request, response, HttpStatus.FORBIDDEN, "Você não tem permissão para acessar este recurso");
    }

    private void escrever(HttpServletRequest request, HttpServletResponse response,
                          HttpStatus status, String mensagem) throws IOException {
        ErroResponse erro = new ErroResponse(LocalDateTime.now(), status.value(), status.name(),
                mensagem, request.getRequestURI(), null);
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getOutputStream(), erro);
    }
}
