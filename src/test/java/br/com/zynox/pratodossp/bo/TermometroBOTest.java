package br.com.zynox.pratodossp.bo;

import br.com.zynox.pratodossp.Fixtures;
import br.com.zynox.pratodossp.dao.PromessaDAO;
import br.com.zynox.pratodossp.dao.SubprefeituraDAO;
import br.com.zynox.pratodossp.dto.PromessaResponse;
import br.com.zynox.pratodossp.dto.TermometroResponse;
import br.com.zynox.pratodossp.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/** Testes do termometro (regra de regiao da INTEGRACAO.md, secao 5). */
@ExtendWith(MockitoExtension.class)
class TermometroBOTest {

    @Mock PromessaDAO promessaDAO;
    @Mock SubprefeituraDAO subprefeituraDAO;

    TermometroBO termometroBO;

    @BeforeEach
    void setUp() {
        termometroBO = new TermometroBO(promessaDAO, subprefeituraDAO, new SubprefeituraBO(subprefeituraDAO));
        lenient().when(promessaDAO.findAllByOrderByIdAsc()).thenReturn(List.of(
                Fixtures.promessa(1L, "Construir creches em todos os bairros", Fixtures.metaCreches()), // meta: Itaquera e Capela
                Fixtures.promessa(4L, "Acabar com a fila de especialistas", Fixtures.metaNaoDivulgada()), // meta sem regiao
                Fixtures.promessa(11L, "Fazer um grande parque linear na zona leste", null, Fixtures.itaquera()),
                Fixtures.promessa(13L, "Criar o museu da cidade", null, Fixtures.butanta())));
    }

    @Test
    @DisplayName("Itaquera: promessa pela regiao da META + promessa sem meta pela PROMESSA_SUBPREFEITURA")
    void termometroItaquera() {
        when(subprefeituraDAO.findById(1L)).thenReturn(Optional.of(Fixtures.itaquera()));

        TermometroResponse t = termometroBO.daSubprefeitura(1L);

        assertThat(t.aviso()).isEqualTo("Dados fictícios");
        assertThat(t.subprefeitura().nome()).isEqualTo("Itaquera");
        assertThat(t.promessas()).extracting(PromessaResponse::id).containsExactly(1L);
        assertThat(t.naoViraramMeta()).extracting(PromessaResponse::id).containsExactly(11L);
        assertThat(t.naoViraramMeta().get(0).meta()).isNull();
    }

    @Test
    @DisplayName("Promessa cuja meta nao tem regiao nao aparece em nenhum bairro")
    void metaSemRegiaoNaoAparece() {
        when(subprefeituraDAO.findById(3L)).thenReturn(Optional.of(Fixtures.butanta()));
        TermometroResponse t = termometroBO.daSubprefeitura(3L);
        assertThat(t.promessas()).isEmpty();
        assertThat(t.naoViraramMeta()).extracting(PromessaResponse::id).containsExactly(13L);
    }

    @Test
    @DisplayName("Subprefeitura inexistente = 404")
    void inexistente() {
        when(subprefeituraDAO.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> termometroBO.daSubprefeitura(999L)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("GET /termometro: um bloco por subprefeitura")
    void todas() {
        when(subprefeituraDAO.findAllByOrderByIdAsc()).thenReturn(List.of(Fixtures.itaquera(), Fixtures.capela(), Fixtures.butanta()));
        assertThat(termometroBO.todas()).extracting(t -> t.subprefeitura().nome())
                .containsExactly("Itaquera", "Capela do Socorro", "Butantã");
    }
}
