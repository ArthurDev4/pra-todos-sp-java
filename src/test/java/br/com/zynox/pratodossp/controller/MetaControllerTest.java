package br.com.zynox.pratodossp.controller;

import br.com.zynox.pratodossp.Fixtures;
import br.com.zynox.pratodossp.bo.MetaBO;
import br.com.zynox.pratodossp.exception.ConflitoException;
import br.com.zynox.pratodossp.exception.RegraNegocioException;
import br.com.zynox.pratodossp.model.StatusMeta;
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

import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MetaController.class)
@Import({SecurityConfig.class, JwtService.class, ErroSegurancaHandler.class})
@ActiveProfiles("test")
class MetaControllerTest {

    static final String JSON = """
            {"numero":13,"texto":"Construir 5 novos parques","eixoId":2,"status":"EM_PROGRESSO",
             "valorAtual":2,"valorAlvo":5,"regionalizacao":"REGIONALIZADA","subprefeituraIds":[1]}""";

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @MockitoBean MetaBO metaBO;

    String admin() {
        return "Bearer " + jwtService.gerarToken("admin@teste.dev");
    }

    @Test
    @DisplayName("GET /metas e publico, aceita os filtros e segue exemplos/metas.json")
    void listar() throws Exception {
        when(metaBO.listar(1L, StatusMeta.EM_PROGRESSO, 1L)).thenReturn(List.of(MetaBO.paraResponse(Fixtures.metaCreches())));
        mvc.perform(get("/metas").param("eixoId", "1").param("status", "EM_PROGRESSO").param("subprefeituraId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eixo").value("Universo SP"))
                .andExpect(jsonPath("$[0].percentual").value(30.0))
                .andExpect(jsonPath("$[0].subprefeituras[0]").value(1))
                .andExpect(jsonPath("$[0].subprefeituras[1]").value(2));
    }

    @Test
    @DisplayName("GET /metas/{id}: meta sem valor alvo traz percentual null")
    void buscar() throws Exception {
        when(metaBO.buscar(3L)).thenReturn(MetaBO.paraResponse(Fixtures.metaNaoDivulgada()));
        mvc.perform(get("/metas/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.percentual").isEmpty())
                .andExpect(jsonPath("$.regionalizacao").value("NAO_DIVULGADA"));
    }

    @Test
    @DisplayName("GET /metas/export devolve CSV")
    void exportar() throws Exception {
        when(metaBO.exportarCsv()).thenReturn("numero,eixo\n1,Universo SP\n");
        mvc.perform(get("/metas/export"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string(startsWith("numero,eixo")));
    }

    @Test
    @DisplayName("POST /metas: 401 sem token; 201 com admin; 409 numero repetido")
    void cadastrar() throws Exception {
        mvc.perform(post("/metas").contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isUnauthorized());

        when(metaBO.cadastrar(any(), eq("admin@teste.dev"))).thenReturn(MetaBO.paraResponse(Fixtures.metaCreches()));
        mvc.perform(post("/metas").header("Authorization", admin()).contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isCreated());

        when(metaBO.cadastrar(any(), any())).thenThrow(new ConflitoException("Já existe uma meta com o número 13"));
        mvc.perform(post("/metas").header("Authorization", admin()).contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /metas com valor negativo e alvo zero = 400 com mensagens por campo")
    void validacao() throws Exception {
        String ruim = JSON.replace("\"valorAtual\":2", "\"valorAtual\":-5").replace("\"valorAlvo\":5", "\"valorAlvo\":0");
        mvc.perform(post("/metas").header("Authorization", admin()).contentType(MediaType.APPLICATION_JSON).content(ruim))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[?(@.campo=='valorAtual')].mensagem").value("O valor atual não pode ser negativo"))
                .andExpect(jsonPath("$.campos[?(@.campo=='valorAlvo')].mensagem").value("O valor alvo deve ser maior que zero"));
    }

    @Test
    @DisplayName("PUT = 200; DELETE = 204; DELETE com promessas = 409")
    void editarExcluir() throws Exception {
        when(metaBO.atualizar(eq(1L), any())).thenReturn(MetaBO.paraResponse(Fixtures.metaCreches()));
        mvc.perform(put("/metas/1").header("Authorization", admin()).contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isOk());

        mvc.perform(delete("/metas/11").header("Authorization", admin())).andExpect(status().isNoContent());
        verify(metaBO).excluir(11L);

        doThrow(new ConflitoException("A meta 1 tem 2 promessa(s) ligada(s)")).when(metaBO).excluir(1L);
        mvc.perform(delete("/metas/1").header("Authorization", admin())).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST/DELETE /metas/{id}/subprefeituras/{subId}")
    void ligacoes() throws Exception {
        when(metaBO.ligarSubprefeitura(1L, 3L)).thenReturn(MetaBO.paraResponse(Fixtures.metaCreches()));
        mvc.perform(post("/metas/1/subprefeituras/3").header("Authorization", admin())).andExpect(status().isOk());

        when(metaBO.ligarSubprefeitura(3L, 1L)).thenThrow(new RegraNegocioException("Só metas REGIONALIZADAS..."));
        mvc.perform(post("/metas/3/subprefeituras/1").header("Authorization", admin())).andExpect(status().isBadRequest());

        when(metaBO.desligarSubprefeitura(1L, 2L)).thenReturn(MetaBO.paraResponse(Fixtures.metaCreches()));
        mvc.perform(delete("/metas/1/subprefeituras/2").header("Authorization", admin())).andExpect(status().isOk());
        mvc.perform(post("/metas/1/subprefeituras/3")).andExpect(status().isUnauthorized());
    }
}
