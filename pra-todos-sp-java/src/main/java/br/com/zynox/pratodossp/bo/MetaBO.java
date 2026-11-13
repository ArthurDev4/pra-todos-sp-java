package br.com.zynox.pratodossp.bo;

import br.com.zynox.pratodossp.dao.MetaDAO;
import br.com.zynox.pratodossp.dao.PromessaDAO;
import br.com.zynox.pratodossp.dto.MetaRequest;
import br.com.zynox.pratodossp.dto.MetaResponse;
import br.com.zynox.pratodossp.dto.MetaResumoResponse;
import br.com.zynox.pratodossp.exception.ConflitoException;
import br.com.zynox.pratodossp.exception.RecursoNaoEncontradoException;
import br.com.zynox.pratodossp.exception.RegraNegocioException;
import br.com.zynox.pratodossp.model.Meta;
import br.com.zynox.pratodossp.model.Regionalizacao;
import br.com.zynox.pratodossp.model.StatusMeta;
import br.com.zynox.pratodossp.model.Subprefeitura;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

/**
 * Regras de negocio das metas (US04, US06, US10, UC11).
 */
@Service
public class MetaBO {

    private final MetaDAO metaDAO;
    private final PromessaDAO promessaDAO;
    private final SubprefeituraBO subprefeituraBO;
    private final EixoBO eixoBO;
    private final AdminBO adminBO;

    public MetaBO(MetaDAO metaDAO, PromessaDAO promessaDAO, SubprefeituraBO subprefeituraBO,
                  EixoBO eixoBO, AdminBO adminBO) {
        this.metaDAO = metaDAO;
        this.promessaDAO = promessaDAO;
        this.subprefeituraBO = subprefeituraBO;
        this.eixoBO = eixoBO;
        this.adminBO = adminBO;
    }

    // ---------------------------------------------------------------- consultas

    /** GET /metas com filtros opcionais (eixo, status e subprefeitura). */
    @Transactional(readOnly = true)
    public List<MetaResponse> listar(Long eixoId, StatusMeta status, Long subprefeituraId) {
        return metaDAO.findAllByOrderByNumeroAsc().stream()
                .filter(m -> eixoId == null || m.getEixo().getId().equals(eixoId))
                .filter(m -> status == null || m.getStatus() == status)
                .filter(m -> subprefeituraId == null || m.atendeSubprefeitura(subprefeituraId))
                .map(MetaBO::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MetaResponse buscar(Long id) {
        return paraResponse(buscarEntidade(id));
    }

    /** GET /metas/export: CSV no formato de exemplos/metas_export.csv. */
    @Transactional(readOnly = true)
    public String exportarCsv() {
        StringBuilder csv = new StringBuilder(
                "numero,eixo,status,valor_atual,valor_alvo,percentual,regionalizacao,qtd_subprefeituras,qtd_promessas\n");
        for (Meta m : metaDAO.findAllByOrderByNumeroAsc()) {
            csv.append(m.getNumero()).append(',')
                    .append(m.getEixo().getNome()).append(',')
                    .append(m.getStatus()).append(',')
                    .append(texto(m.getValorAtual())).append(',')
                    .append(texto(m.getValorAlvo())).append(',')
                    .append(percentualTexto(calcularPercentual(m.getValorAtual(), m.getValorAlvo()))).append(',')
                    .append(m.getRegionalizacao()).append(',')
                    .append(m.getSubprefeituras().size()).append(',')
                    .append(promessaDAO.countByMetaId(m.getId())).append('\n');
        }
        return csv.toString();
    }

    // ---------------------------------------------------------------- cadastro

    /** Cadastra a meta gravando em CRIADO_POR o admin logado. */
    @Transactional
    public MetaResponse cadastrar(MetaRequest req, String emailAdmin) {
        if (metaDAO.existsByNumero(req.numero())) {
            throw new ConflitoException("Já existe uma meta com o número " + req.numero());
        }
        Meta meta = new Meta();
        aplicarDados(meta, req);
        meta.setCriadoPor(adminBO.buscarEntidadePorEmail(emailAdmin));
        return paraResponse(metaDAO.save(meta));
    }

    @Transactional
    public MetaResponse atualizar(Long id, MetaRequest req) {
        Meta meta = buscarEntidade(id);
        if (metaDAO.existsByNumeroAndIdNot(req.numero(), id)) {
            throw new ConflitoException("Já existe outra meta com o número " + req.numero());
        }
        aplicarDados(meta, req);
        return paraResponse(metaDAO.save(meta));
    }

    /**
     * Apaga a meta. Com promessas ligadas devolve 409 (desligue as promessas antes).
     * As ligacoes com subprefeituras sao desfeitas antes de apagar.
     */
    @Transactional
    public void excluir(Long id) {
        Meta meta = buscarEntidade(id);
        long promessas = promessaDAO.countByMetaId(id);
        if (promessas > 0) {
            throw new ConflitoException("A meta " + meta.getNumero() + " tem " + promessas
                    + " promessa(s) ligada(s). Desligue ou apague essas promessas antes.");
        }
        meta.getSubprefeituras().clear();
        metaDAO.delete(meta);
    }

    // ------------------------------------------- ligacao com subprefeitura (UC11)

    /** POST /metas/{id}/subprefeituras/{subId}: so para meta REGIONALIZADA. */
    @Transactional
    public MetaResponse ligarSubprefeitura(Long id, Long subprefeituraId) {
        Meta meta = buscarEntidade(id);
        Subprefeitura sub = subprefeituraBO.buscarEntidade(subprefeituraId);
        if (!meta.getRegionalizacao().aceitaRegioes()) {
            throw new RegraNegocioException("Só metas REGIONALIZADAS podem ser ligadas a subprefeituras (esta é "
                    + meta.getRegionalizacao() + ")");
        }
        if (meta.atendeSubprefeitura(subprefeituraId)) {
            throw new ConflitoException("A meta já está ligada à subprefeitura " + sub.getNome());
        }
        meta.getSubprefeituras().add(sub);
        return paraResponse(metaDAO.save(meta));
    }

    /** DELETE /metas/{id}/subprefeituras/{subId}: a REGIONALIZADA nao pode ficar sem nenhuma. */
    @Transactional
    public MetaResponse desligarSubprefeitura(Long id, Long subprefeituraId) {
        Meta meta = buscarEntidade(id);
        Subprefeitura sub = subprefeituraBO.buscarEntidade(subprefeituraId);
        if (!meta.atendeSubprefeitura(subprefeituraId)) {
            throw new RecursoNaoEncontradoException("A meta não está ligada à subprefeitura " + sub.getNome());
        }
        if (meta.getRegionalizacao().aceitaRegioes() && meta.getSubprefeituras().size() == 1) {
            throw new ConflitoException("Uma meta REGIONALIZADA precisa de pelo menos uma subprefeitura. "
                    + "Ligue outra antes ou mude a regionalização.");
        }
        meta.getSubprefeituras().remove(sub);
        return paraResponse(metaDAO.save(meta));
    }

    /** Meta do caminho da URL; inexistente = 404. */
    public Meta buscarEntidade(Long id) {
        return metaDAO.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Meta não encontrada: " + id));
    }

    // ---------------------------------------------------------------- regras

    /**
     * Percentual = valorAtual / valorAlvo x 100, com 1 casa decimal (ex.: 8 de 30 = 26.7).
     * Devolve null quando nao ha valor alvo (ou ele nao e valido).
     */
    public static BigDecimal calcularPercentual(BigDecimal valorAtual, BigDecimal valorAlvo) {
        if (valorAtual == null || valorAlvo == null || valorAlvo.signum() <= 0) {
            return null;
        }
        return valorAtual.multiply(BigDecimal.valueOf(100)).divide(valorAlvo, 1, RoundingMode.HALF_UP);
    }

    private void aplicarDados(Meta meta, MetaRequest req) {
        validarValores(req.valorAtual(), req.valorAlvo());
        Set<Subprefeitura> regioes = subprefeituraBO.buscarReferencias(req.subprefeituraIds());
        validarRegionalizacao(req.regionalizacao(), regioes);

        meta.setNumero(req.numero());
        meta.setTexto(req.texto().trim());
        meta.setEixo(eixoBO.buscarReferencia(req.eixoId()));
        meta.setStatus(req.status());
        meta.setValorAtual(req.valorAtual());
        meta.setValorAlvo(req.valorAlvo());
        meta.setRegionalizacao(req.regionalizacao());
        meta.setFonte(fonteObrigatoria(req.fonte(), "da meta"));
        meta.definirSubprefeituras(regioes);
    }

    /** A fonte e obrigatoria (texto ou link) e nao pode ser so espacos. */
    static String fonteObrigatoria(String fonte, String de) {
        String f = fonte == null ? "" : fonte.trim();
        if (f.isEmpty()) {
            throw new RegraNegocioException("A fonte " + de + " é obrigatória");
        }
        return f;
    }

    /** valor_atual e valor_alvo juntos ou nenhum; atual nao negativo; alvo maior que 0 (= CK_META_VALORES). */
    void validarValores(BigDecimal valorAtual, BigDecimal valorAlvo) {
        if ((valorAtual == null) != (valorAlvo == null)) {
            throw new RegraNegocioException("Informe o valor atual e o valor alvo juntos, ou deixe os dois vazios");
        }
        if (valorAtual != null && valorAtual.signum() < 0) {
            throw new RegraNegocioException("O valor atual não pode ser negativo");
        }
        if (valorAlvo != null && valorAlvo.signum() <= 0) {
            throw new RegraNegocioException("O valor alvo deve ser maior que zero");
        }
    }

    /** REGIONALIZADA precisa de pelo menos uma subprefeitura; as outras nao podem ter. */
    void validarRegionalizacao(Regionalizacao regionalizacao, Set<Subprefeitura> regioes) {
        if (regionalizacao.aceitaRegioes() && regioes.isEmpty()) {
            throw new RegraNegocioException("Uma meta REGIONALIZADA precisa de pelo menos uma subprefeitura");
        }
        if (!regionalizacao.aceitaRegioes() && !regioes.isEmpty()) {
            throw new RegraNegocioException("Uma meta " + regionalizacao + " não pode ter subprefeituras ligadas");
        }
    }

    // ---------------------------------------------------------------- conversao

    public static MetaResumoResponse paraResumo(Meta m) {
        return new MetaResumoResponse(m.getId(), m.getNumero(), m.getTexto(), m.getEixo().getNome(),
                m.getStatus().name(), m.getValorAtual(), m.getValorAlvo(),
                calcularPercentual(m.getValorAtual(), m.getValorAlvo()), m.getRegionalizacao().name(), m.getFonte());
    }

    public static MetaResponse paraResponse(Meta m) {
        List<Long> subs = m.getSubprefeituras().stream().map(Subprefeitura::getId).sorted().toList();
        return new MetaResponse(m.getId(), m.getNumero(), m.getTexto(), m.getEixo().getNome(),
                m.getStatus().name(), m.getValorAtual(), m.getValorAlvo(),
                calcularPercentual(m.getValorAtual(), m.getValorAlvo()), m.getRegionalizacao().name(), m.getFonte(), subs);
    }

    /** Valores sem zeros desnecessarios (12, nao 12.00). */
    private static String texto(BigDecimal v) {
        return v == null ? "" : v.stripTrailingZeros().toPlainString();
    }

    /** Percentual sempre com 1 casa (30.0), como no exemplo do grupo. */
    private static String percentualTexto(BigDecimal p) {
        return p == null ? "" : p.toPlainString();
    }
}
