package br.com.zynox.pratodossp.controller;

import br.com.zynox.pratodossp.Fixtures;
import br.com.zynox.pratodossp.bo.PromessaBO;
import br.com.zynox.pratodossp.dto.PromessaDetalheResponse;
import br.com.zynox.pratodossp.exception.ConflitoException;
import br.com.zynox.pratodossp.exception.RecursoNaoEncontradoException;
import br.com.zynox.pratodossp.exception.RegraNegocioException;
import br.com.zynox.pratodossp.model.EstadoPromessa;
import br.com.zynox.pratodossp.security.ErroSegurancaHandler;
import br.com.zynox.pratodossp.security.JwtService;
import br.com.zynox.pratodossp.security.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Camada CONTROLLER com a seguranca real (JWT): rotas do contrato, codigos HTTP e formato de erro.
 */
@WebMvcTest(PromessaController.class)
@Import({SecurityConfig.class, JwtService.class, ErroSegurancaHandler.class})
@ActiveProfiles("test")
class PromessaControllerTest {

    static final String JSON = "{\"texto\":\"Construir uma praça\",\"metaId\":null,\"subprefeituraIds\":[1]}";

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @MockitoBean PromessaBO promessaBO;

    String admin() {
        return "Bearer " + jwtService.gerarToken("admin@teste.dev");
    }

    PromessaDetalheResponse detalhe(Long id) {
        return new PromessaDetalheResponse(id, "Construir uma praça", EstadoPromessa.NAO_VIROU_META, null, List.of(1L));
    }

    @Test
    @DisplayName("GET /promessas e publico e segue o formato de exemplos/promessas.json")
    void listar() throws Exception {
        when(promessaBO.listar(null, null)).thenReturn(List.of(
                PromessaBO.paraResponse(Fixtures.promessa(1L, "Construir creches em todos os bairros", Fixtures.metaCreches())),
                PromessaBO.paraResponse(Fixtures.promessa(11L, "Fazer um grande parque linear", null, Fixtures.itaquera()))));

        mvc.perform(get("/promessas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("VIROU_META"))
                .andExpect(jsonPath("$[0].meta.eixo").value("Universo SP"))
                .andExpect(jsonPath("$[0].meta.status").value("EM_PROGRESSO"))
                .andExpect(jsonPath("$[0].meta.percentual").value(30.0))
                .andExpect(jsonPath("$[0].meta.regionalizacao").value("REGIONALIZADA"))
                .andExpect(jsonPath("$[0].meta.criadoPor").doesNotExist())
                .andExpect(jsonPath("$[1].estado").value("NAO_VIROU_META"))
                .andExpect(jsonPath("$[1].meta").isEmpty());
    }

    @Test
    @DisplayName("GET /promessas?semMeta=true&eixoId=1 repassa os filtros ao BO")
    void filtros() throws Exception {
        when(promessaBO.listar(true, 1L)).thenReturn(List.of());
        mvc.perform(get("/promessas").param("semMeta", "true").param("eixoId", "1")).andExpect(status().isOk());
        verify(promessaBO).listar(true, 1L);
    }

    @Test
    @DisplayName("GET /promessas/999 = 404 no formato padrao; /promessas/abc = 400")
    void erros() throws Exception {
        when(promessaBO.buscar(999L)).thenThrow(new RecursoNaoEncontradoException("Promessa não encontrada: 999"));
        mvc.perform(get("/promessas/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Promessa não encontrada: 999"))
                .andExpect(jsonPath("$.path").value("/promessas/999"))
                .andExpect(jsonPath("$.timestamp").exists());
        mvc.perform(get("/promessas/abc")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST sem token = 401; token invalido = 401; token sem papel ADMIN = 403")
    void seguranca() throws Exception {
        mvc.perform(post("/promessas").contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
        mvc.perform(post("/promessas").header("Authorization", "Bearer falso")
                .contentType(MediaType.APPLICATION_JSON).content(JSON)).andExpect(status().isUnauthorized());
        mvc.perform(post("/promessas").header("Authorization", "Bearer " + jwtService.gerarToken("x@x.dev", "VISITANTE"))
                .contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.error").value("FORBIDDEN"));
        verify(promessaBO, never()).cadastrar(any(), any());
    }

    @Test
    @DisplayName("POST com admin = 201 + Location; o e-mail do token vai para CRIADO_POR")
    void cadastrar() throws Exception {
        when(promessaBO.cadastrar(any(), eq("admin@teste.dev"))).thenReturn(detalhe(16L));
        mvc.perform(post("/promessas").header("Authorization", admin())
                        .contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/promessas/16"))
                .andExpect(jsonPath("$.subprefeituras[0]").value(1));
    }

    @Test
    @DisplayName("POST sem texto = 400 com erro por campo; JSON invalido = 400; regra violada = 400/409")
    void validacoes() throws Exception {
        mvc.perform(post("/promessas").header("Authorization", admin())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("texto"));
        mvc.perform(post("/promessas").header("Authorization", admin())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"x\",\"metaId\":\"abc\"}"))
                .andExpect(status().isBadRequest());

        when(promessaBO.cadastrar(any(), any())).thenThrow(new RegraNegocioException("A meta informada não existe: 999"));
        mvc.perform(post("/promessas").header("Authorization", admin())
                        .contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("A meta informada não existe: 999"));
    }

    @Test
    @DisplayName("PUT = 200; DELETE = 204; DELETE inexistente = 404")
    void editarExcluir() throws Exception {
        when(promessaBO.atualizar(eq(11L), any())).thenReturn(detalhe(11L));
        mvc.perform(put("/promessas/11").header("Authorization", admin())
                .contentType(MediaType.APPLICATION_JSON).content(JSON)).andExpect(status().isOk());

        mvc.perform(delete("/promessas/11").header("Authorization", admin())).andExpect(status().isNoContent());
        verify(promessaBO).excluir(11L);

        doThrow(new RecursoNaoEncontradoException("Promessa não encontrada: 999")).when(promessaBO).excluir(999L);
        mvc.perform(delete("/promessas/999").header("Authorization", admin())).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST/DELETE /promessas/{id}/subprefeituras/{subId}: 409 se a promessa tiver meta")
    void ligacoes() throws Exception {
        when(promessaBO.ligarSubprefeitura(11L, 3L)).thenReturn(detalhe(11L));
        mvc.perform(post("/promessas/11/subprefeituras/3").header("Authorization", admin())).andExpect(status().isOk());

        when(promessaBO.ligarSubprefeitura(1L, 3L)).thenThrow(new ConflitoException("A promessa virou meta"));
        mvc.perform(post("/promessas/1/subprefeituras/3").header("Authorization", admin()))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.error").value("CONFLICT"));

        when(promessaBO.desligarSubprefeitura(11L, 1L)).thenReturn(detalhe(11L));
        mvc.perform(delete("/promessas/11/subprefeituras/1").header("Authorization", admin())).andExpect(status().isOk());
        mvc.perform(delete("/promessas/11/subprefeituras/1")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("CORS liberado (preflight de outro endereco)")
    void cors() throws Exception {
        mvc.perform(options("/promessas").header("Origin", "http://localhost:5000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("Erro inesperado = 500 sem detalhes internos")
    void erroInesperado() throws Exception {
        when(promessaBO.buscar(1L)).thenThrow(new IllegalStateException("detalhe interno secreto"));
        mvc.perform(get("/promessas/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Ocorreu um erro inesperado. Tente novamente mais tarde."));
    }
}
