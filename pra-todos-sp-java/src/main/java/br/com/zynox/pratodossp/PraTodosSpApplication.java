package br.com.zynox.pratodossp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * Ponto de entrada da API do Pra Todos SP (Equipe Zynox - Global Solution FIAP 2026.2).
 * O usuario padrao do Spring Security e desligado: o login e feito pelo AdminBO + JWT.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class PraTodosSpApplication {

    public static void main(String[] args) {
        SpringApplication.run(PraTodosSpApplication.class, args);
    }
}
