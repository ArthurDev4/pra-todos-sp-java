package br.com.zynox.pratodossp.config;

import br.com.zynox.pratodossp.bo.AdminBO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

/**
 * Tarefas ao subir a aplicacao:
 * 1) Com DB_INICIALIZAR=true e sem a tabela PROMESSA: roda os scripts oficiais do grupo
 *    (db/01_ddl.sql e db/02_dml.sql). Se as tabelas ja existem, nao mexe em nada.
 * 2) Cria o primeiro administrador a partir de ADMIN_EMAIL / ADMIN_PASSWORD (os admins de
 *    exemplo do 02_dml tem senha ficticia e nao conseguem logar).
 */
@Configuration
@Profile("!test")
public class InicializacaoConfig {

    private static final Logger log = LoggerFactory.getLogger(InicializacaoConfig.class);

    @Bean
    @Order(1)
    public CommandLineRunner prepararBanco(DataSource dataSource, JdbcTemplate jdbc,
                                           @Value("${app.db.inicializar:false}") boolean inicializar) {
        return args -> {
            if (!inicializar) {
                return;
            }
            Integer existe = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME = 'PROMESSA'", Integer.class);
            if (existe != null && existe > 0) {
                log.info("Tabelas do Pra Todos SP já existem no Oracle: nada a criar.");
                return;
            }
            log.info("Banco vazio: rodando 01_ddl.sql e 02_dml.sql do grupo...");
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                    new ClassPathResource("db/01_ddl.sql"), new ClassPathResource("db/02_dml.sql"));
            populator.setSqlScriptEncoding(StandardCharsets.UTF_8.name());
            populator.execute(dataSource);
            log.info("Banco criado com sucesso.");
        };
    }

    @Bean
    @Order(2)
    public CommandLineRunner criarAdminInicial(AdminBO adminBO,
                                               @Value("${app.admin.nome:Administrador}") String nome,
                                               @Value("${app.admin.email:}") String email,
                                               @Value("${app.admin.senha:}") String senha) {
        return args -> adminBO.criarAdminInicialSeNecessario(nome, email, senha);
    }
}
