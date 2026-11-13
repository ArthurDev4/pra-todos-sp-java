package br.com.zynox.pratodossp.bo;

import br.com.zynox.pratodossp.Fixtures;
import br.com.zynox.pratodossp.dao.EixoDAO;
import br.com.zynox.pratodossp.dao.MetaDAO;
import br.com.zynox.pratodossp.dao.PromessaDAO;
import br.com.zynox.pratodossp.dao.SubprefeituraDAO;
import br.com.zynox.pratodossp.dto.MetaRequest;
import br.com.zynox.pratodossp.dto.MetaResponse;
import br.com.zynox.pratodossp.exception.ConflitoException;
import br.com.zynox.pratodossp.exception.RecursoNaoEncontradoException;
import br.com.zynox.pratodossp.exception.RegraNegocioException;
import br.com.zynox.pratodossp.model.Meta;
import br.com.zynox.pratodossp.model.Regionalizacao;
import br.com.zynox.pratodossp.model.StatusMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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

/** Testes unitarios das regras de negocio de META (DAOs simulados com Mockito). */
@ExtendWith(MockitoExtension.class)
class MetaBOTest {

    @Mock MetaDAO metaDAO;
    @Mock PromessaDAO promessaDAO;
    @Mock SubprefeituraDAO subprefeituraDAO;
    @Mock EixoDAO eixoDAO;
    @Mock AdminBO adminBO;

    MetaBO metaBO;

    @BeforeEach
    void setUp() {
        metaBO = new MetaBO(metaDAO, promessaDAO, new SubprefeituraBO(subprefeituraDAO), new EixoBO(eixoDAO), adminBO);
        lenient().when(metaDAO.save(any(Meta.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(subprefeituraDAO.findAllById(anyIterable())).thenReturn(List.of(Fixtures.itaquera()));
        lenient().when(subprefeituraDAO.findById(1L)).thenReturn(Optional.of(Fixtures.itaquera()));
        lenient().when(subprefeituraDAO.findById(2L)).thenReturn(Optional.of(Fixtures.capela()));
        lenient().when(subprefeituraDAO.findById(3L)).thenReturn(Optional.of(Fixtures.butanta()));
        lenient().when(eixoDAO.findById(1L)).thenReturn(Optional.of(Fixtures.universoSp()));
        lenient().when(adminBO.buscarEntidadePorEmail("admin@teste.dev")).thenReturn(Fixtures.admin());
    }

    private MetaRequest req(BigDecimal atual, BigDecimal alvo, Regionalizacao reg, List<Long> subs) {
        return new MetaRequest(13, "Construir 5 novos parques", 1L, StatusMeta.EM_PROGRESSO, atual, alvo, reg, subs);
    }

    // ------------------------------------------------------------ percentual

    @Test
    @DisplayName("Percentual = atual / alvo x 100 com 1 casa: 12/40 = 30.0 e 8/30 = 26.7 (exemplos do grupo)")
    void percentual() {
        assertThat(MetaBO.calcularPercentual(new BigDecimal("12"), new BigDecimal("40"))).isEqualByComparingTo("30.0");
        assertThat(MetaBO.calcularPercentual(new BigDecimal("8"), new BigDecimal("30"))).isEqualByComparingTo("26.7");
        assertThat(MetaBO.calcularPercentual(new BigDecimal("8"), new BigDecimal("30")).scale()).isEqualTo(1);
        assertThat(MetaBO.calcularPercentual(BigDecimal.ZERO, new BigDecimal("15"))).isEqualByComparingTo("0.0");
    }

    @Test
    @DisplayName("Percentual e null sem valor alvo (ou com alvo invalido)")
    void percentualNulo() {
        assertThat(MetaBO.calcularPercentual(null, null)).isNull();
        assertThat(MetaBO.calcularPercentual(BigDecimal.ONE, BigDecimal.ZERO)).isNull();
    }

    // ------------------------------------------------------------ consultas

    @Test
    @DisplayName("GET /metas com filtros de eixo, status e subprefeitura")
    void listarComFiltros() {
        when(metaDAO.findAllByOrderByNumeroAsc()).thenReturn(List.of(Fixtures.metaCreches(), Fixtures.metaNaoDivulgada()));

        assertThat(metaBO.listar(null, null, null)).hasSize(2);
        assertThat(metaBO.listar(null, StatusMeta.EM_PLANEJAMENTO, null)).extracting(MetaResponse::numero).containsExactly(3);
        assertThat(metaBO.listar(null, null, 1L)).extracting(MetaResponse::numero).containsExactly(1);
        assertThat(metaBO.listar(2L, null, null)).isEmpty();
    }

    @Test
    @DisplayName("Resposta da meta: eixo, status e regionalizacao como texto; subprefeituras como IDs")
    void formatoResposta() {
        MetaResponse r = MetaBO.paraResponse(Fixtures.metaCreches());
        assertThat(r.eixo()).isEqualTo("Universo SP");
        assertThat(r.status()).isEqualTo("EM_PROGRESSO");
        assertThat(r.regionalizacao()).isEqualTo("REGIONALIZADA");
        assertThat(r.subprefeituras()).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("Consultar meta inexistente = 404")
    void buscarInexistente() {
        when(metaDAO.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> metaBO.buscar(999L)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Export CSV tem o cabecalho e as colunas de exemplos/metas_export.csv")
    void exportCsv() {
        when(metaDAO.findAllByOrderByNumeroAsc()).thenReturn(List.of(Fixtures.metaCreches(), Fixtures.metaNaoDivulgada()));
        when(promessaDAO.countByMetaId(1L)).thenReturn(2L);
        when(promessaDAO.countByMetaId(3L)).thenReturn(1L);

        assertThat(metaBO.exportarCsv().split("\n")).containsExactly(
                "numero,eixo,status,valor_atual,valor_alvo,percentual,regionalizacao,qtd_subprefeituras,qtd_promessas",
                "1,Universo SP,EM_PROGRESSO,12,40,30.0,REGIONALIZADA,2,2",
                "3,Universo SP,EM_PLANEJAMENTO,,,,NAO_DIVULGADA,0,1");
    }

    // ------------------------------------------------------------ cadastro e regras

    @Test
    @DisplayName("Cadastrar meta grava CRIADO_POR com o admin logado")
    void cadastrar() {
        MetaResponse r = metaBO.cadastrar(req(new BigDecimal("2"), new BigDecimal("5"),
                Regionalizacao.REGIONALIZADA, List.of(1L)), "admin@teste.dev");

        assertThat(r.percentual()).isEqualByComparingTo("40.0");
        ArgumentCaptor<Meta> salva = ArgumentCaptor.forClass(Meta.class);
        verify(metaDAO).save(salva.capture());
        assertThat(salva.getValue().getCriadoPor().getEmail()).isEqualTo("admin@teste.dev");
    }

    @Test
    @DisplayName("Numero de meta repetido = 409")
    void numeroDuplicado() {
        when(metaDAO.existsByNumero(13)).thenReturn(true);
        assertThatThrownBy(() -> metaBO.cadastrar(req(null, null, Regionalizacao.NAO_DIVULGADA, null), "admin@teste.dev"))
                .isInstanceOf(ConflitoException.class);
        verify(metaDAO, never()).save(any());
    }

    @Test
    @DisplayName("Valores: juntos ou nenhum, atual >= 0, alvo > 0")
    void valores() {
        assertThatThrownBy(() -> metaBO.cadastrar(req(BigDecimal.ONE, null, Regionalizacao.NAO_DIVULGADA, null), "admin@teste.dev"))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("juntos");
        assertThatThrownBy(() -> metaBO.cadastrar(req(new BigDecimal("-1"), BigDecimal.TEN, Regionalizacao.NAO_DIVULGADA, null), "admin@teste.dev"))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("negativo");
        assertThatThrownBy(() -> metaBO.cadastrar(req(BigDecimal.ONE, BigDecimal.ZERO, Regionalizacao.NAO_DIVULGADA, null), "admin@teste.dev"))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("maior que zero");
    }

    @Test
    @DisplayName("REGIONALIZADA exige subprefeitura; NAO_DIVULGADA e NAO_REGIONALIZAVEL nao aceitam")
    void regionalizacao() {
        assertThatThrownBy(() -> metaBO.cadastrar(req(null, null, Regionalizacao.REGIONALIZADA, List.of()), "admin@teste.dev"))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("pelo menos uma");
        assertThatThrownBy(() -> metaBO.cadastrar(req(null, null, Regionalizacao.NAO_DIVULGADA, List.of(1L)), "admin@teste.dev"))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("NAO_DIVULGADA");
        assertThatThrownBy(() -> metaBO.cadastrar(req(null, null, Regionalizacao.NAO_REGIONALIZAVEL, List.of(1L)), "admin@teste.dev"))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("NAO_REGIONALIZAVEL");
    }

    @Test
    @DisplayName("Eixo ou subprefeitura inexistente no corpo = 400")
    void referenciasInexistentes() {
        when(eixoDAO.findById(99L)).thenReturn(Optional.empty());
        MetaRequest eixoRuim = new MetaRequest(13, "Texto", 99L, StatusMeta.EM_PROGRESSO, null, null,
                Regionalizacao.NAO_DIVULGADA, null);
        assertThatThrownBy(() -> metaBO.cadastrar(eixoRuim, "admin@teste.dev")).isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> metaBO.cadastrar(req(null, null, Regionalizacao.REGIONALIZADA, List.of(1L, 999L)), "admin@teste.dev"))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("999");
    }

    @Test
    @DisplayName("Editar meta altera status e valores")
    void editar() {
        when(metaDAO.findById(1L)).thenReturn(Optional.of(Fixtures.metaCreches()));
        MetaRequest r = new MetaRequest(1, "Creches", 1L, StatusMeta.ATINGIDA, new BigDecimal("40"),
                new BigDecimal("40"), Regionalizacao.REGIONALIZADA, List.of(1L));
        MetaResponse resp = metaBO.atualizar(1L, r);
        assertThat(resp.status()).isEqualTo("ATINGIDA");
        assertThat(resp.percentual()).isEqualByComparingTo("100.0");
    }

    @Test
    @DisplayName("Apagar meta com promessas = 409; sem promessas apaga e desfaz as ligacoes")
    void excluir() {
        Meta meta = Fixtures.metaCreches();
        when(metaDAO.findById(1L)).thenReturn(Optional.of(meta));
        when(promessaDAO.countByMetaId(1L)).thenReturn(2L);
        assertThatThrownBy(() -> metaBO.excluir(1L)).isInstanceOf(ConflitoException.class);
        verify(metaDAO, never()).delete(any());

        when(promessaDAO.countByMetaId(1L)).thenReturn(0L);
        metaBO.excluir(1L);
        assertThat(meta.getSubprefeituras()).isEmpty();
        verify(metaDAO).delete(meta);
    }

    // ------------------------------------------------------------ ligacoes

    @Test
    @DisplayName("Ligar subprefeitura: ok na REGIONALIZADA; 409 se ja ligada; 400 se nao for REGIONALIZADA")
    void ligar() {
        when(metaDAO.findById(1L)).thenReturn(Optional.of(Fixtures.metaCreches()));
        assertThat(metaBO.ligarSubprefeitura(1L, 3L).subprefeituras()).containsExactly(1L, 2L, 3L);

        when(metaDAO.findById(1L)).thenReturn(Optional.of(Fixtures.metaCreches()));
        assertThatThrownBy(() -> metaBO.ligarSubprefeitura(1L, 1L)).isInstanceOf(ConflitoException.class);

        when(metaDAO.findById(3L)).thenReturn(Optional.of(Fixtures.metaNaoDivulgada()));
        assertThatThrownBy(() -> metaBO.ligarSubprefeitura(3L, 1L)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("Desligar: 404 se nao ligada; 409 se for a ultima de uma REGIONALIZADA")
    void desligar() {
        when(metaDAO.findById(1L)).thenReturn(Optional.of(Fixtures.metaCreches()));
        assertThat(metaBO.desligarSubprefeitura(1L, 2L).subprefeituras()).containsExactly(1L);

        Meta umaSo = Fixtures.metaCreches();
        umaSo.getSubprefeituras().removeIf(s -> s.getId() == 2L);
        when(metaDAO.findById(1L)).thenReturn(Optional.of(umaSo));
        assertThatThrownBy(() -> metaBO.desligarSubprefeitura(1L, 1L)).isInstanceOf(ConflitoException.class);
        assertThatThrownBy(() -> metaBO.desligarSubprefeitura(1L, 3L)).isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
