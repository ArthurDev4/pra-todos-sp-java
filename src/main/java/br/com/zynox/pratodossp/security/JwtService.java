package br.com.zynox.pratodossp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Gera e valida tokens JWT (assinatura HMAC-SHA256) do administrador.
 * O segredo vem da variavel de ambiente JWT_SECRET e nunca fica no codigo.
 */
@Service
public class JwtService {

    public static final String ROLE_ADMIN = "ADMIN";

    private final SecretKey chave;
    private final long expiracaoSegundos;

    public JwtService(@Value("${app.jwt.secret:}") String segredo,
                      @Value("${app.jwt.expiracao-minutos:120}") long expiracaoMinutos) {
        if (segredo == null || segredo.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "Defina a variável de ambiente JWT_SECRET com pelo menos 32 caracteres.");
        }
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.expiracaoSegundos = expiracaoMinutos * 60;
    }

    public String gerarToken(String email) {
        return gerarToken(email, ROLE_ADMIN);
    }

    public String gerarToken(String email, String papel) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(email)
                .claim("role", papel)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plusSeconds(expiracaoSegundos)))
                .signWith(chave)
                .compact();
    }

    /** Devolve os dados do token se a assinatura e a validade estiverem corretas. */
    public Optional<Claims> validar(String token) {
        try {
            return Optional.of(Jwts.parser().verifyWith(chave).build().parseSignedClaims(token).getPayload());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public long getExpiracaoSegundos() {
        return expiracaoSegundos;
    }
}
