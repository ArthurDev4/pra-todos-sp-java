package br.com.zynox.pratodossp.bo;

import br.com.zynox.pratodossp.dao.UsuarioAdminDAO;
import br.com.zynox.pratodossp.dto.AdminRequest;
import br.com.zynox.pratodossp.dto.AdminResponse;
import br.com.zynox.pratodossp.dto.LoginRequest;
import br.com.zynox.pratodossp.dto.LoginResponse;
import br.com.zynox.pratodossp.exception.ConflitoException;
import br.com.zynox.pratodossp.exception.CredenciaisInvalidasException;
import br.com.zynox.pratodossp.exception.RecursoNaoEncontradoException;
import br.com.zynox.pratodossp.model.UsuarioAdmin;
import br.com.zynox.pratodossp.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Regras do administrador (US08): login e criacao do administrador inicial.
 */
@Service
public class AdminBO {

    private static final Logger log = LoggerFactory.getLogger(AdminBO.class);

    private final UsuarioAdminDAO usuarioAdminDAO;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AdminBO(UsuarioAdminDAO usuarioAdminDAO, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioAdminDAO = usuarioAdminDAO;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /** Confere e-mail e senha (BCrypt) e devolve um token JWT. */
    @Transactional(readOnly = true)
    public LoginResponse autenticar(LoginRequest req) {
        UsuarioAdmin admin = usuarioAdminDAO.findByEmail(normalizar(req.email()))
                .orElseThrow(CredenciaisInvalidasException::new);
        if (!passwordEncoder.matches(req.senha(), admin.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }
        String token = jwtService.gerarToken(admin.getEmail());
        return new LoginResponse(token, "Bearer", jwtService.getExpiracaoSegundos(), AdminResponse.de(admin));
    }

    /** POST /admins: cria um admin (e-mail unico; senha gravada so como hash BCrypt). */
    @Transactional
    public AdminResponse criar(AdminRequest req) {
        String email = normalizar(req.email());
        if (usuarioAdminDAO.existsByEmail(email)) {
            throw new ConflitoException("Já existe um administrador com o e-mail " + email);
        }
        UsuarioAdmin novo = new UsuarioAdmin(req.nome().trim(), email, passwordEncoder.encode(req.senha()));
        return AdminResponse.de(usuarioAdminDAO.save(novo));
    }

    /** Usado para preencher CRIADO_POR com o administrador logado. */
    public UsuarioAdmin buscarEntidadePorEmail(String email) {
        return usuarioAdminDAO.findByEmail(normalizar(email))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Administrador não encontrado"));
    }

    /**
     * Cria o primeiro administrador a partir de variaveis de ambiente, se ainda nao existir.
     * Nao ha cadastro publico de administradores.
     */
    @Transactional
    public void criarAdminInicialSeNecessario(String nome, String email, String senha) {
        if (email == null || email.isBlank() || senha == null || senha.isBlank()) {
            log.warn("ADMIN_EMAIL/ADMIN_PASSWORD não definidos: nenhum administrador inicial foi criado.");
            return;
        }
        if (senha.length() < 8) {
            log.warn("ADMIN_PASSWORD precisa ter pelo menos 8 caracteres: administrador inicial NÃO criado.");
            return;
        }
        String emailNormalizado = normalizar(email);
        if (usuarioAdminDAO.existsByEmail(emailNormalizado)) {
            return;
        }
        String nomeFinal = (nome == null || nome.isBlank()) ? "Administrador" : nome.trim();
        usuarioAdminDAO.save(new UsuarioAdmin(nomeFinal, emailNormalizado, passwordEncoder.encode(senha)));
        log.info("Administrador inicial criado: {}", emailNormalizado);
    }

    private String normalizar(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
