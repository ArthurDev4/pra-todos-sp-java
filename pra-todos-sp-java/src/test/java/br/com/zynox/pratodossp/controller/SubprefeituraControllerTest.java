package br.com.zynox.pratodossp.controller;

import br.com.zynox.pratodossp.Fixtures;
import br.com.zynox.pratodossp.bo.PromessaBO;
import br.com.zynox.pratodossp.bo.SubprefeituraBO;
import br.com.zynox.pratodossp.bo.TermometroBO;
import br.com.zynox.pratodossp.dto.SubprefeituraResponse;
import br.com.zynox.pratodossp.dto.TermometroResponse;
import br.com.zynox.pratodossp.exception.RecursoNaoEncontradoException;
import br.com.zynox.pratodossp.security.ErroSegurancaHandler;
import br.com.zynox.pratodossp.security.JwtService;
import br.com.zynox.pratodossp.security.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubprefeituraController.class)
@Import({SecurityConfig.class, JwtService.class, ErroSegurancaHandler.class})
@ActiveProfiles("test")
class SubprefeituraControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean SubprefeituraBO subprefeituraBO;
    @MockitoBean TermometroBO termometroBO;

    TermometroResponse itaquera() {
        return new TermometroResponse("Dados fictícios", new SubprefeituraResponse(1L, "Itaquera"),
                List.of(PromessaBO.paraResponse(Fixtures.promessa(1L, "Construir creches em todos os bairros", Fixtures.metaCreches()))),
                List.of(PromessaBO.paraResponse(Fixtures.promessa(11L, "Fazer um grande parque linear na zona leste", null))));
    }

    @Test
    @DisplayName("GET /subprefeituras e publico")
    void listar() throws Exception {
        when(subprefeituraBO.listar()).thenReturn(List.of(new SubprefeituraResponse(1L, "Itaquera")));
        mvc.perform(get("/subprefeituras")).andExpect(status().isOk()).andExpect(jsonPath("$[0].nome").value("Itaquera"));
    }

    @Test
    @DisplayName("GET /subprefeituras/1/termometro no formato de exemplos/termometro_itaquera.json")
    void termometro() throws Exception {
        when(termometroBO.daSubprefeitura(1L)).thenReturn(itaquera());
        mvc.perform(get("/subprefeituras/1/termometro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aviso").value("Dados fictícios"))
                .andExpect(jsonPath("$.subprefeitura.id").value(1))
                .andExpect(jsonPath("$.promessas[0].estado").value("VIROU_META"))
                .andExpect(jsonPath("$.promessas[0].meta.numero").value(1))
                .andExpect(jsonPath("$.naoViraramMeta[0].estado").value("NAO_VIROU_META"))
                .andExpect(jsonPath("$.naoViraramMeta[0].meta").isEmpty());
    }

    @Test
    @DisplayName("Termometro de subprefeitura inexistente = 404; GET /termometro = lista")
    void termometroErroETodas() throws Exception {
        when(termometroBO.daSubprefeitura(999L)).thenThrow(new RecursoNaoEncontradoException("Subprefeitura não encontrada: 999"));
        mvc.perform(get("/subprefeituras/999/termometro")).andExpect(status().isNotFound());

        when(termometroBO.todas()).thenReturn(List.of(itaquera()));
        mvc.perform(get("/termometro")).andExpect(status().isOk()).andExpect(jsonPath("$[0].subprefeitura.nome").value("Itaquera"));
    }
}
