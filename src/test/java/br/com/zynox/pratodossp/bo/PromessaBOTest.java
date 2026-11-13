package br.com.zynox.pratodossp.bo;

import br.com.zynox.pratodossp.Fixtures;
import br.com.zynox.pratodossp.dao.MetaDAO;
import br.com.zynox.pratodossp.dao.PromessaDAO;
import br.com.zynox.pratodossp.dao.SubprefeituraDAO;
import br.com.zynox.pratodossp.dto.PromessaDetalheResponse;
import br.com.zynox.pratodossp.dto.PromessaRequest;
import br.com.zynox.pratodossp.dto.PromessaResponse;
import br.com.zynox.pratodossp.exception.ConflitoException;
import br.com.zynox.pratodossp.exception.RecursoNaoEncontradoException;
import br.com.zynox.pratodossp.exception.RegraNegocioException;
import br.com.zynox.pratodossp.model.EstadoPromessa;
import br.com.zynox.pratodossp.model.Promessa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Testes unitarios das regras de negocio de PROMESSA. */
@ExtendWith(MockitoExtension.class)
class PromessaBOTest {

    @Mock PromessaDAO promessaDAO;
    @Mock MetaDAO metaDAO;
    @Mock SubprefeituraDAO subprefeituraDAO;
    @Mock AdminBO adminBO;

    PromessaBO promessaBO;

    @BeforeEach
    void setUp() {
        promessaBO = new PromessaBO(promessaDAO, metaDAO, new SubprefeituraBO(subprefeituraDAO), adminBO);
        lenient().when(promessaDAO.save(any(Promessa.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(adminBO.buscarEntidadePorEmail("admin@teste.dev")).thenReturn(Fixtures.admin());
        lenient().when(subprefeituraDAO.findById(1L)).thenReturn(Optional.of(Fixtures.itaquera()));
        lenient().when(subprefeituraDAO.findById(3L)).thenReturn(Optional.of(Fixtures.butanta()));
        lenient().when(metaDAO.findById(1L)).thenReturn(Optional.of(Fixtures.metaCreches()));
    }

    private List<Promessa> amostra() {
        return List.of(
                Fixtures.promessa(1L, "Construir creches em todos os bairros", Fixtures.metaCreches()),
                Fixtures.promessa(4L, "Acabar com a fila de especialistas", Fixtures.metaNaoDivulgada()),
                Fixtures.promessa(11L, "Fazer um grande parque linear na zona leste", null, Fixtures.itaquera()));
    }

    @Test
    @DisplayName("GET /promessas no formato do Python: estado + meta (ou null)")
    void listarTodas() {
        when(promessaDAO.findAllByOrderByIdAsc()).thenReturn(amostra());
        List<PromessaResponse> r = promessaBO.listar(null, null);

        assertThat(r).extracting(PromessaResponse::id).containsExactly(1L, 4L, 11L);
        assertThat(r.get(0).estado()).isEqualTo(EstadoPromessa.VIROU_META);
        assertThat(r.get(0).meta().eixo()).isEqualTo("Universo SP");
        assertThat(r.get(0).meta().percentual()).isEqualByComparingTo("30.0");
        assertThat(r.get(1).meta().percentual()).isNull();
        assertThat(r.get(2).estado()).isEqualTo(EstadoPromessa.NAO_VIROU_META);
        assertThat(r.get(2).meta()).isNull();
    }

    @Test
    @DisplayName("Filtros semMeta e eixoId")
    void filtros() {
        when(promessaDAO.findAllByOrderByIdAsc()).thenReturn(amostra());
        assertThat(promessaBO.listar(true, null)).extracting(PromessaResponse::id).containsExactly(11L);
        assertThat(promessaBO.listar(false, null)).extracting(PromessaResponse::id).containsExactly(1L, 4L);
        assertThat(promessaBO.listar(null, 1L)).extracting(PromessaResponse::id).containsExactly(1L, 4L);
        assertThat(promessaBO.listar(null, 4L)).isEmpty();
    }

    @Test
    @DisplayName("Consultar promessa inexistente = 404")
    void buscarInexistente() {
        when(promessaDAO.findWithDetalhesById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> promessaBO.buscar(999L)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Cadastrar promessa sem meta com subprefeitura; CRIADO_POR = admin logado")
    void cadastrarSemMeta() {
        when(subprefeituraDAO.findAllById(anyIterable())).thenReturn(List.of(Fixtures.itaquera()));
        PromessaDetalheResponse r = promessaBO.cadastrar(new PromessaRequest("Praça nova", null, List.of(1L)), "admin@teste.dev");

        assertThat(r.estado()).isEqualTo(EstadoPromessa.NAO_VIROU_META);
        assertThat(r.subprefeituras()).containsExactly(1L);
        ArgumentCaptor<Promessa> salva = ArgumentCaptor.forClass(Promessa.class);
        verify(promessaDAO).save(salva.capture());
        assertThat(salva.getValue().getCriadoPor().getEmail()).isEqualTo("admin@teste.dev");
    }

    @Test
    @DisplayName("Promessa com meta e subprefeituras ao mesmo tempo = 409; meta inexistente = 400")
    void cadastrarRegras() {
        when(subprefeituraDAO.findAllById(anyIterable())).thenReturn(List.of(Fixtures.itaquera()));
        assertThatThrownBy(() -> promessaBO.cadastrar(new PromessaRequest("Creches", 1L, List.of(1L)), "admin@teste.dev"))
                .isInstanceOf(ConflitoException.class);
        when(metaDAO.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> promessaBO.cadastrar(new PromessaRequest("Creches", 999L, null), "admin@teste.dev"))
                .isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> promessaBO.cadastrar(new PromessaRequest("   ", null, null), "admin@teste.dev"))
                .isInstanceOf(RegraNegocioException.class);
        verify(promessaDAO, never()).save(any());
    }

    @Test
    @DisplayName("Editar ligando a uma meta apaga as ligacoes com subprefeituras (US11)")
    void ligarAMetaApagaLigacoes() {
        Promessa semMeta = Fixtures.promessa(11L, "Parque linear", null, Fixtures.itaquera());
        when(promessaDAO.findWithDetalhesById(11L)).thenReturn(Optional.of(semMeta));

        PromessaDetalheResponse r = promessaBO.atualizar(11L, new PromessaRequest("Parque linear", 1L, null));

        assertThat(r.estado()).isEqualTo(EstadoPromessa.VIROU_META);
        assertThat(r.subprefeituras()).isEmpty();
    }

    @Test
    @DisplayName("Editar desligando a meta: volta a NAO_VIROU_META")
    void desligarMeta() {
        when(promessaDAO.findWithDetalhesById(1L)).thenReturn(Optional.of(
                Fixtures.promessa(1L, "Creches", Fixtures.metaCreches())));
        assertThat(promessaBO.atualizar(1L, new PromessaRequest("Creches", null, null)).estado())
                .isEqualTo(EstadoPromessa.NAO_VIROU_META);
    }

    @Test
    @DisplayName("Apagar promessa (e inexistente = 404)")
    void excluir() {
        Promessa p = Fixtures.promessa(11L, "Parque", null, Fixtures.itaquera());
        when(promessaDAO.findWithDetalhesById(11L)).thenReturn(Optional.of(p));
        promessaBO.excluir(11L);
        verify(promessaDAO).delete(p);

        when(promessaDAO.findWithDetalhesById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> promessaBO.excluir(999L)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Ligar subprefeitura: so para promessa sem meta (409 se tiver meta ou ja ligada)")
    void ligarSubprefeitura() {
        when(promessaDAO.findWithDetalhesById(11L)).thenReturn(Optional.of(
                Fixtures.promessa(11L, "Parque", null, Fixtures.itaquera())));
        assertThat(promessaBO.ligarSubprefeitura(11L, 3L).subprefeituras()).containsExactly(1L, 3L);

        when(promessaDAO.findWithDetalhesById(11L)).thenReturn(Optional.of(
                Fixtures.promessa(11L, "Parque", null, Fixtures.itaquera())));
        assertThatThrownBy(() -> promessaBO.ligarSubprefeitura(11L, 1L)).isInstanceOf(ConflitoException.class);

        when(promessaDAO.findWithDetalhesById(1L)).thenReturn(Optional.of(
                Fixtures.promessa(1L, "Creches", Fixtures.metaCreches())));
        assertThatThrownBy(() -> promessaBO.ligarSubprefeitura(1L, 3L))
                .isInstanceOf(ConflitoException.class).hasMessageContaining("virou meta");
    }

    @Test
    @DisplayName("Desligar subprefeitura nao ligada = 404")
    void desligarSubprefeitura() {
        when(promessaDAO.findWithDetalhesById(11L)).thenReturn(Optional.of(
                Fixtures.promessa(11L, "Parque", null, Fixtures.itaquera())));
        assertThatThrownBy(() -> promessaBO.desligarSubprefeitura(11L, 3L)).isInstanceOf(RecursoNaoEncontradoException.class);
        assertThat(promessaBO.desligarSubprefeitura(11L, 1L).subprefeituras()).isEmpty();
    }
}
