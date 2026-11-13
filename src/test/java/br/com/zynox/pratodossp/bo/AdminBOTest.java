package br.com.zynox.pratodossp.bo;

import br.com.zynox.pratodossp.dao.UsuarioAdminDAO;
import br.com.zynox.pratodossp.dto.AdminRequest;
import br.com.zynox.pratodossp.dto.LoginRequest;
import br.com.zynox.pratodossp.dto.LoginResponse;
import br.com.zynox.pratodossp.exception.ConflitoException;
import br.com.zynox.pratodossp.exception.CredenciaisInvalidasException;
import br.com.zynox.pratodossp.model.UsuarioAdmin;
import br.com.zynox.pratodossp.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Testes unitarios do login administrativo (US08). */
@ExtendWith(MockitoExtension.class)
class AdminBOTest {

    @Mock
    UsuarioAdminDAO usuarioAdminDAO;

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    JwtService jwtService = new JwtService("segredo-de-teste-com-mais-de-32-caracteres-zynox", 5);
    AdminBO adminBO;

    @BeforeEach
    void setUp() {
        adminBO = new AdminBO(usuarioAdminDAO, encoder, jwtService);
    }

    private UsuarioAdmin admin() {
        UsuarioAdmin u = new UsuarioAdmin("Admin Teste", "admin@teste.dev", encoder.encode("senha-forte-123"));
        u.setId(1L);
        return u;
    }

    @Test
    @DisplayName("Login correto devolve token JWT valido e nao expoe a senha")
    void loginOk() {
        when(usuarioAdminDAO.findByEmail("admin@teste.dev")).thenReturn(Optional.of(admin()));

        LoginResponse r = adminBO.autenticar(new LoginRequest(" Admin@Teste.dev ", "senha-forte-123"));

        assertThat(r.tipo()).isEqualTo("Bearer");
        assertThat(jwtService.validar(r.token())).isPresent();
        assertThat(r.admin().email()).isEqualTo("admin@teste.dev");
    }

    @Test
    @DisplayName("Senha errada = credenciais invalidas")
    void senhaErrada() {
        when(usuarioAdminDAO.findByEmail("admin@teste.dev")).thenReturn(Optional.of(admin()));
        assertThatThrownBy(() -> adminBO.autenticar(new LoginRequest("admin@teste.dev", "errada")))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    @DisplayName("E-mail inexistente = credenciais invalidas (mesma mensagem)")
    void emailInexistente() {
        when(usuarioAdminDAO.findByEmail("x@teste.dev")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> adminBO.autenticar(new LoginRequest("x@teste.dev", "qualquer")))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    @DisplayName("Administrador inicial e criado com senha em hash BCrypt")
    void criaAdminInicial() {
        when(usuarioAdminDAO.existsByEmail("novo@teste.dev")).thenReturn(false);

        adminBO.criarAdminInicialSeNecessario("Novo", "novo@teste.dev", "senha-forte-123");

        ArgumentCaptor<UsuarioAdmin> captor = ArgumentCaptor.forClass(UsuarioAdmin.class);
        verify(usuarioAdminDAO).save(captor.capture());
        assertThat(captor.getValue().getSenhaHash()).isNotEqualTo("senha-forte-123");
        assertThat(encoder.matches("senha-forte-123", captor.getValue().getSenhaHash())).isTrue();
    }

    @Test
    @DisplayName("POST /admins: cria com hash BCrypt e e-mail normalizado; e-mail repetido = 409")
    void criarAdmin() {
        when(usuarioAdminDAO.existsByEmail("novo@teste.dev")).thenReturn(false);
        when(usuarioAdminDAO.save(any(UsuarioAdmin.class))).thenAnswer(inv -> inv.getArgument(0));

        var r = adminBO.criar(new AdminRequest("Novo Admin", " Novo@Teste.dev ", "senha-forte-123"));

        assertThat(r.email()).isEqualTo("novo@teste.dev");
        ArgumentCaptor<UsuarioAdmin> captor = ArgumentCaptor.forClass(UsuarioAdmin.class);
        verify(usuarioAdminDAO).save(captor.capture());
        assertThat(encoder.matches("senha-forte-123", captor.getValue().getSenhaHash())).isTrue();

        when(usuarioAdminDAO.existsByEmail("admin1@example.com")).thenReturn(true);
        assertThatThrownBy(() -> adminBO.criar(new AdminRequest("X", "admin1@example.com", "senha-forte-123")))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    @DisplayName("Sem variaveis de ambiente ou com senha curta, nenhum admin e criado")
    void naoCriaAdminSemConfig() {
        adminBO.criarAdminInicialSeNecessario("X", "", "");
        adminBO.criarAdminInicialSeNecessario("X", "a@b.dev", "curta");
        verify(usuarioAdminDAO, never()).save(any());
    }

    @Test
    @DisplayName("Token assinado com outro segredo ou com conteudo alterado e rejeitado")
    void tokenAdulterado() {
        JwtService outro = new JwtService("outro-segredo-qualquer-com-mais-de-32-caracteres", 5);
        assertThat(jwtService.validar(outro.gerarToken("admin@teste.dev"))).isEmpty();

        String[] partes = jwtService.gerarToken("admin@teste.dev").split("\\.");
        String payloadFalso = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"hacker@x.dev\",\"role\":\"ADMIN\"}".getBytes());
        assertThat(jwtService.validar(partes[0] + "." + payloadFalso + "." + partes[2])).isEmpty();
    }
}
