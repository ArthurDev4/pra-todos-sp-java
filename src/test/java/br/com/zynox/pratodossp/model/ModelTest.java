package br.com.zynox.pratodossp.model;

import br.com.zynox.pratodossp.Fixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Testes unitarios das regras da camada MODEL. */
class ModelTest {

    @Test
    @DisplayName("Promessa sem meta = NAO_VIROU_META; com meta = VIROU_META")
    void estadoDaPromessa() {
        assertThat(Fixtures.promessa(11L, "Parque linear", null).getEstado()).isEqualTo(EstadoPromessa.NAO_VIROU_META);
        assertThat(Fixtures.promessa(1L, "Creches", Fixtures.metaCreches()).getEstado()).isEqualTo(EstadoPromessa.VIROU_META);
    }

    @Test
    @DisplayName("Meta e promessa sabem a quais subprefeituras estao ligadas")
    void ligacoes() {
        assertThat(Fixtures.metaCreches().atendeSubprefeitura(1L)).isTrue();
        assertThat(Fixtures.metaCreches().atendeSubprefeitura(3L)).isFalse();
        assertThat(Fixtures.promessa(11L, "Parque", null, Fixtures.itaquera()).atingeSubprefeitura(1L)).isTrue();
    }

    @Test
    @DisplayName("Somente REGIONALIZADA aceita subprefeituras")
    void regionalizacao() {
        assertThat(Regionalizacao.REGIONALIZADA.aceitaRegioes()).isTrue();
        assertThat(Regionalizacao.NAO_DIVULGADA.aceitaRegioes()).isFalse();
        assertThat(Regionalizacao.NAO_REGIONALIZAVEL.aceitaRegioes()).isFalse();
    }

    @Test
    @DisplayName("Subprefeituras com o mesmo ID sao iguais (sem repeticao em conjuntos)")
    void igualdadeSubprefeitura() {
        assertThat(Fixtures.itaquera()).isEqualTo(new Subprefeitura(1L, "Itaquera")).hasSameHashCodeAs(Fixtures.itaquera());
        assertThat(Fixtures.itaquera()).isNotEqualTo(Fixtures.butanta());
    }
}
