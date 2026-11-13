package br.com.zynox.pratodossp.controller;

import br.com.zynox.pratodossp.bo.AdminBO;
import br.com.zynox.pratodossp.dto.AdminResponse;
import br.com.zynox.pratodossp.dto.LoginResponse;
import br.com.zynox.pratodossp.exception.ConflitoException;
import br.com.zynox.pratodossp.exception.CredenciaisInvalidasException;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@Import({SecurityConfig.class, JwtService.class, ErroSegurancaHandler.class})
@ActiveProfiles("test")
class AdminControllerTest {

    static final String NOVO = "{\"nome\":\"Novo\",\"email\":\"novo@teste.dev\",\"senha\":\"senha-forte-123\"}";

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @MockitoBean AdminBO adminBO;

    @Test
    @DisplayName("POST /login correto = 200 com token e sem senha na resposta")
    void loginOk() throws Exception {
        when(adminBO.autenticar(any())).thenReturn(new LoginResponse("abc.def.ghi", "Bearer", 7200,
                new AdminResponse(6L, "Admin", "admin@teste.dev")));
        mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@teste.dev\",\"senha\":\"senha-forte-123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("abc.def.ghi"))
                .andExpect(jsonPath("$.admin.senhaHash").doesNotExist());
    }

    @Test
    @DisplayName("POST /login errado = 401; sem campos = 400")
    void loginErrado() throws Exception {
        when(adminBO.autenticar(any())).thenThrow(new CredenciaisInvalidasException());
        mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@teste.dev\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("E-mail ou senha inválidos"));
        mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /admins: 401 sem login; 201 com admin; 409 e-mail repetido; 400 senha curta")
    void criarAdmin() throws Exception {
        mvc.perform(post("/admins").contentType(MediaType.APPLICATION_JSON).content(NOVO))
                .andExpect(status().isUnauthorized());

        String token = "Bearer " + jwtService.gerarToken("admin@teste.dev");
        when(adminBO.criar(any())).thenReturn(new AdminResponse(7L, "Novo", "novo@teste.dev"));
        mvc.perform(post("/admins").header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(NOVO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("novo@teste.dev"))
                .andExpect(jsonPath("$.senha").doesNotExist());

        when(adminBO.criar(any())).thenThrow(new ConflitoException("Já existe um administrador com o e-mail novo@teste.dev"));
        mvc.perform(post("/admins").header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(NOVO))
                .andExpect(status().isConflict());

        mvc.perform(post("/admins").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content(NOVO.replace("senha-forte-123", "123")))
                .andExpect(status().isBadRequest());
    }
}
