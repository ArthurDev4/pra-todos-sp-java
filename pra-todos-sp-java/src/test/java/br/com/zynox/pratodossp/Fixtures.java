package br.com.zynox.pratodossp;

import br.com.zynox.pratodossp.model.Eixo;
import br.com.zynox.pratodossp.model.Meta;
import br.com.zynox.pratodossp.model.Promessa;
import br.com.zynox.pratodossp.model.Regionalizacao;
import br.com.zynox.pratodossp.model.StatusMeta;
import br.com.zynox.pratodossp.model.Subprefeitura;
import br.com.zynox.pratodossp.model.UsuarioAdmin;

import java.math.BigDecimal;
import java.util.Set;

/** Objetos de exemplo iguais aos do 02_dml.sql do grupo (dados ficticios). */
public final class Fixtures {

    private Fixtures() {
    }

    public static Subprefeitura itaquera() { return new Subprefeitura(1L, "Itaquera"); }
    public static Subprefeitura capela() { return new Subprefeitura(2L, "Capela do Socorro"); }
    public static Subprefeitura butanta() { return new Subprefeitura(3L, "Butantã"); }
    public static Eixo universoSp() { return new Eixo(1L, "Universo SP"); }

    public static UsuarioAdmin admin() {
        UsuarioAdmin u = new UsuarioAdmin("Admin Teste", "admin@teste.dev", "hash");
        u.setId(1L);
        return u;
    }

    /** Meta 1 do DML: creches, 12 de 40 (30.0%), REGIONALIZADA em Itaquera e Capela do Socorro. */
    public static Meta metaCreches() {
        Meta m = new Meta();
        m.setId(1L);
        m.setNumero(1);
        m.setTexto("Ampliar a rede de creches para atender mais crianças pequenas");
        m.setEixo(universoSp());
        m.setStatus(StatusMeta.EM_PROGRESSO);
        m.setValorAtual(new BigDecimal("12"));
        m.setValorAlvo(new BigDecimal("40"));
        m.setRegionalizacao(Regionalizacao.REGIONALIZADA);
        m.setFonte("Programa de Metas (fictício)");
        m.setCriadoPor(admin());
        m.definirSubprefeituras(Set.of(itaquera(), capela()));
        return m;
    }

    /** Meta 3 do DML: fila de especialistas, sem valores, NAO_DIVULGADA. */
    public static Meta metaNaoDivulgada() {
        Meta m = metaCreches();
        m.setId(3L);
        m.setNumero(3);
        m.setTexto("Reduzir a fila de consultas com especialistas");
        m.setStatus(StatusMeta.EM_PLANEJAMENTO);
        m.setValorAtual(null);
        m.setValorAlvo(null);
        m.setRegionalizacao(Regionalizacao.NAO_DIVULGADA);
        m.definirSubprefeituras(Set.of());
        return m;
    }

    public static Promessa promessa(Long id, String texto, Meta meta, Subprefeitura... regioes) {
        Promessa p = new Promessa();
        p.setId(id);
        p.setTexto(texto);
        p.setEixo(meta != null ? meta.getEixo() : universoSp());
        p.setFonte("Plano de governo (fictício)");
        p.setMeta(meta);
        p.setCriadoPor(admin());
        p.definirSubprefeituras(Set.of(regioes));
        return p;
    }
}
