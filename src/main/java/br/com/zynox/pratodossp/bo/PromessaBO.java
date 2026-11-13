package br.com.zynox.pratodossp.bo;

import br.com.zynox.pratodossp.dao.MetaDAO;
import br.com.zynox.pratodossp.dao.PromessaDAO;
import br.com.zynox.pratodossp.dto.PromessaDetalheResponse;
import br.com.zynox.pratodossp.dto.PromessaRequest;
import br.com.zynox.pratodossp.dto.PromessaResponse;
import br.com.zynox.pratodossp.exception.ConflitoException;
import br.com.zynox.pratodossp.exception.RecursoNaoEncontradoException;
import br.com.zynox.pratodossp.exception.RegraNegocioException;
import br.com.zynox.pratodossp.model.Meta;
import br.com.zynox.pratodossp.model.Promessa;
import br.com.zynox.pratodossp.model.Subprefeitura;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Regras de negocio das promessas (US02, US03, US09, US11).
 */
@Service
public class PromessaBO {

    private final PromessaDAO promessaDAO;
    private final MetaDAO metaDAO;
    private final SubprefeituraBO subprefeituraBO;
    private final AdminBO adminBO;

    public PromessaBO(PromessaDAO promessaDAO, MetaDAO metaDAO, SubprefeituraBO subprefeituraBO, AdminBO adminBO) {
        this.promessaDAO = promessaDAO;
        this.metaDAO = metaDAO;
        this.subprefeituraBO = subprefeituraBO;
        this.adminBO = adminBO;
    }

    // ---------------------------------------------------------------- consultas

    /**
     * GET /promessas (usada pelo projeto Python). Filtros opcionais:
     * semMeta=true (so as que nao viraram meta) / false (so as que viraram) e eixoId (eixo da meta).
     */
    @Transactional(readOnly = true)
    public List<PromessaResponse> listar(Boolean semMeta, Long eixoId) {
        return promessaDAO.findAllByOrderByIdAsc().stream()
                .filter(p -> semMeta == null || (p.getMeta() == null) == semMeta)
                .filter(p -> eixoId == null || (p.getMeta() != null && p.getMeta().getEixo().getId().equals(eixoId)))
                .map(PromessaBO::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PromessaDetalheResponse buscar(Long id) {
        Promessa p = buscarEntidade(id);
        List<Long> subs = p.getSubprefeituras().stream().map(Subprefeitura::getId).sorted().toList();
        return new PromessaDetalheResponse(p.getId(), p.getTexto(), p.getEstado(),
                p.getMeta() == null ? null : MetaBO.paraResumo(p.getMeta()), subs);
    }

    // ---------------------------------------------------------------- cadastro

    /** Cadastra a promessa gravando em CRIADO_POR o admin logado. */
    @Transactional
    public PromessaDetalheResponse cadastrar(PromessaRequest req, String emailAdmin) {
        Promessa promessa = new Promessa();
        aplicarDados(promessa, req);
        promessa.setCriadoPor(adminBO.buscarEntidadePorEmail(emailAdmin));
        return buscarSalva(promessaDAO.save(promessa));
    }

    @Transactional
    public PromessaDetalheResponse atualizar(Long id, PromessaRequest req) {
        Promessa promessa = buscarEntidade(id);
        aplicarDados(promessa, req);
        return buscarSalva(promessaDAO.save(promessa));
    }

    /** Apaga a promessa (as ligacoes com subprefeituras sao desfeitas antes). */
    @Transactional
    public void excluir(Long id) {
        Promessa promessa = buscarEntidade(id);
        promessa.getSubprefeituras().clear();
        promessaDAO.delete(promessa);
    }

    // ----------------------------------- ligacao com subprefeitura (so promessa sem meta)

    @Transactional
    public PromessaDetalheResponse ligarSubprefeitura(Long id, Long subprefeituraId) {
        Promessa promessa = buscarEntidade(id);
        Subprefeitura sub = subprefeituraBO.buscarEntidade(subprefeituraId);
        if (promessa.getMeta() != null) {
            throw new ConflitoException("A promessa virou meta: a região dela vem da meta "
                    + promessa.getMeta().getNumero() + ", não de PROMESSA_SUBPREFEITURA");
        }
        if (promessa.atingeSubprefeitura(subprefeituraId)) {
            throw new ConflitoException("A promessa já está ligada à subprefeitura " + sub.getNome());
        }
        promessa.getSubprefeituras().add(sub);
        return buscarSalva(promessaDAO.save(promessa));
    }

    @Transactional
    public PromessaDetalheResponse desligarSubprefeitura(Long id, Long subprefeituraId) {
        Promessa promessa = buscarEntidade(id);
        Subprefeitura sub = subprefeituraBO.buscarEntidade(subprefeituraId);
        if (!promessa.atingeSubprefeitura(subprefeituraId)) {
            throw new RecursoNaoEncontradoException("A promessa não está ligada à subprefeitura " + sub.getNome());
        }
        promessa.getSubprefeituras().remove(sub);
        return buscarSalva(promessaDAO.save(promessa));
    }

    // ---------------------------------------------------------------- apoio

    public static PromessaResponse paraResponse(Promessa p) {
        return new PromessaResponse(p.getId(), p.getTexto(), p.getEstado(),
                p.getMeta() == null ? null : MetaBO.paraResumo(p.getMeta()));
    }

    private Promessa buscarEntidade(Long id) {
        return promessaDAO.findWithDetalhesById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Promessa não encontrada: " + id));
    }

    private PromessaDetalheResponse buscarSalva(Promessa salva) {
        List<Long> subs = salva.getSubprefeituras().stream().map(Subprefeitura::getId).sorted().toList();
        return new PromessaDetalheResponse(salva.getId(), salva.getTexto(), salva.getEstado(),
                salva.getMeta() == null ? null : MetaBO.paraResumo(salva.getMeta()), subs);
    }

    private void aplicarDados(Promessa promessa, PromessaRequest req) {
        String texto = req.texto() == null ? "" : req.texto().trim();
        if (texto.isEmpty()) {
            throw new RegraNegocioException("O texto da promessa é obrigatório");
        }
        Set<Subprefeitura> regioes = subprefeituraBO.buscarReferencias(req.subprefeituraIds());
        Meta meta = null;
        if (req.metaId() != null) {
            meta = metaDAO.findById(req.metaId())
                    .orElseThrow(() -> new RegraNegocioException("A meta informada não existe: " + req.metaId()));
            if (!regioes.isEmpty()) {
                throw new ConflitoException("Promessa com meta herda a região da meta: não informe subprefeituras");
            }
        }
        promessa.setTexto(texto);
        promessa.setMeta(meta);
        // ao ligar a uma meta, as ligacoes com subprefeituras sao apagadas (regioes fica vazio)
        promessa.definirSubprefeituras(regioes);
    }
}
