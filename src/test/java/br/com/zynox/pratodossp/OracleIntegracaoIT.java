package br.com.zynox.pratodossp;

import br.com.zynox.pratodossp.security.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de INTEGRACAO com o Oracle REAL (aplicacao inteira + banco do grupo).
 * So executa com credenciais (variavel DB_USERNAME ou arquivo .env) e com "mvnw verify".
 * Cada teste roda em transacao desfeita no final (rollback): os dados do grupo nao mudam.
 * Obs.: sequences usadas em INSERTs desfeitos nao voltam (gera so "buracos" nos proximos ids).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@EnabledIf("oracleConfigurado")
class OracleIntegracaoIT {

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @Autowired ObjectMapper json;

    /** Admin criado na subida a partir de ADMIN_EMAIL (necessario para CRIADO_POR). */
    @Value("${app.admin.email}")
    String emailAdmin;

    static boolean oracleConfigurado() throws Exception {
        String env = System.getenv("DB_USERNAME");
        if (env != null && !env.isBlank()) {
            return true;
        }
        Path arquivo = Path.of(".env");
        return Files.exists(arquivo) && Files.readAllLines(arquivo).stream()
                .anyMatch(l -> l.startsWith("DB_USERNAME=") && l.length() > "DB_USERNAME=".length());
    }

    String admin() {
        return "Bearer " + jwtService.gerarToken(emailAdmin);
    }

    long id(ResultActions r) throws Exception {
        JsonNode n = json.readTree(r.andReturn().getResponse().getContentAsString());
        return n.get("id").asLong();
    }

    @Test
    @DisplayName("Oracle: termometro de uma subprefeitura tem o formato do contrato (nao depende dos dados cadastrados)")
    void termometroDeUmaSubprefeitura() throws Exception {
        mvc.perform(get("/subprefeituras/1/termometro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aviso").value("Dados fictícios"))
                .andExpect(jsonPath("$.subprefeitura.id").value(1))
                .andExpect(jsonPath("$.promessas").isArray())
                .andExpect(jsonPath("$.naoViraramMeta").isArray())
                // quem esta em "promessas" virou meta; quem esta em "naoViraramMeta" nao tem meta
                .andExpect(jsonPath("$.promessas[*].estado", everyItem(equalTo("VIROU_META"))))
                .andExpect(jsonPath("$.naoViraramMeta[*].estado", everyItem(equalTo("NAO_VIROU_META"))));
    }

    @Test
    @DisplayName("Oracle: listas de promessas e termometro respondem no formato do contrato (sem contar linhas fixas)")
    void listagens() throws Exception {
        mvc.perform(get("/promessas")).andExpect(status().isOk()).andExpect(jsonPath("$").isArray());
        mvc.perform(get("/termometro")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
        mvc.perform(get("/promessas").param("semMeta", "true")).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].estado", everyItem(equalTo("NAO_VIROU_META"))));
    }

    @Test
    @DisplayName("Oracle: fluxo completo de escrita (meta, promessa, ligacoes, regras 409) com rollback")
    void fluxoCompleto() throws Exception {
        long meta = id(mvc.perform(post("/metas").header("Authorization", admin()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numero":9901,"texto":"Meta de teste de integração","eixoId":4,"status":"EM_PROGRESSO",
                                 "valorAtual":3,"valorAlvo":4,"regionalizacao":"REGIONALIZADA",
                                 "fonte":"Programa de Metas (fictício)","subprefeituraIds":[1]}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.percentual").value(75.0)));

        long promessa = id(mvc.perform(post("/promessas").header("Authorization", admin()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"Promessa de teste de integração\",\"eixoId\":4,"
                                + "\"fonte\":\"Plano de governo (fictício)\",\"subprefeituraIds\":[4]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("NAO_VIROU_META")));

        mvc.perform(post("/promessas/" + promessa + "/subprefeituras/5").header("Authorization", admin()))
                .andExpect(jsonPath("$.subprefeituras", contains(4, 5)));

        // ligar a uma meta apaga as ligacoes com subprefeituras
        mvc.perform(put("/promessas/" + promessa).header("Authorization", admin()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"Promessa de teste de integração\",\"eixoId\":4,"
                                + "\"fonte\":\"Plano de governo (fictício)\",\"metaId\":" + meta + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("VIROU_META"))
                .andExpect(jsonPath("$.subprefeituras", hasSize(0)));
        mvc.perform(post("/promessas/" + promessa + "/subprefeituras/5").header("Authorization", admin()))
                .andExpect(status().isConflict());

        mvc.perform(post("/metas/" + meta + "/subprefeituras/2").header("Authorization", admin())).andExpect(status().isOk());
        mvc.perform(delete("/metas/" + meta).header("Authorization", admin())).andExpect(status().isConflict());

        mvc.perform(delete("/promessas/" + promessa).header("Authorization", admin())).andExpect(status().isNoContent());
        mvc.perform(delete("/metas/" + meta).header("Authorization", admin())).andExpect(status().isNoContent());
        mvc.perform(get("/metas/" + meta)).andExpect(status().isNotFound());

        mvc.perform(post("/admins").header("Authorization", admin()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Teste\",\"email\":\"admin1@example.com\",\"senha\":\"senha-forte-123\"}"))
                .andExpect(status().isConflict());
    }
}