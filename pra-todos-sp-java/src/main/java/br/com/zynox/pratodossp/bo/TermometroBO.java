package br.com.zynox.pratodossp.bo;

import br.com.zynox.pratodossp.dao.PromessaDAO;
import br.com.zynox.pratodossp.dao.SubprefeituraDAO;
import br.com.zynox.pratodossp.dto.PromessaResponse;
import br.com.zynox.pratodossp.dto.SubprefeituraResponse;
import br.com.zynox.pratodossp.dto.TermometroResponse;
import br.com.zynox.pratodossp.model.Promessa;
import br.com.zynox.pratodossp.model.Subprefeitura;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * O termometro de promessas por bairro (funcionalidade principal, US02/US03).
 * Regra de regiao (INTEGRACAO.md, secao 5):
 * <ul>
 *   <li>"promessas": as que viraram meta e cuja META esta ligada a subprefeitura (META_SUBPREFEITURA);</li>
 *   <li>"naoViraramMeta": as sem meta ligadas a subprefeitura (PROMESSA_SUBPREFEITURA).</li>
 * </ul>
 */
@Service
public class TermometroBO {

    private final PromessaDAO promessaDAO;
    private final SubprefeituraDAO subprefeituraDAO;
    private final SubprefeituraBO subprefeituraBO;

    public TermometroBO(PromessaDAO promessaDAO, SubprefeituraDAO subprefeituraDAO, SubprefeituraBO subprefeituraBO) {
        this.promessaDAO = promessaDAO;
        this.subprefeituraDAO = subprefeituraDAO;
        this.subprefeituraBO = subprefeituraBO;
    }

    /** GET /subprefeituras/{id}/termometro */
    @Transactional(readOnly = true)
    public TermometroResponse daSubprefeitura(Long subprefeituraId) {
        Subprefeitura sub = subprefeituraBO.buscarEntidade(subprefeituraId);
        return montar(sub, promessaDAO.findAllByOrderByIdAsc());
    }

    /** GET /termometro: um bloco por subprefeitura (mesma ordem de id dos exemplos). */
    @Transactional(readOnly = true)
    public List<TermometroResponse> todas() {
        List<Promessa> promessas = promessaDAO.findAllByOrderByIdAsc();
        return subprefeituraDAO.findAllByOrderByIdAsc().stream().map(s -> montar(s, promessas)).toList();
    }

    private TermometroResponse montar(Subprefeitura sub, List<Promessa> todas) {
        List<PromessaResponse> viraramMeta = todas.stream()
                .filter(p -> p.getMeta() != null && p.getMeta().atendeSubprefeitura(sub.getId()))
                .map(PromessaBO::paraResponse)
                .toList();
        List<PromessaResponse> naoViraram = todas.stream()
                .filter(p -> p.getMeta() == null && p.atingeSubprefeitura(sub.getId()))
                .map(PromessaBO::paraResponse)
                .toList();
        return new TermometroResponse(TermometroResponse.AVISO, SubprefeituraResponse.de(sub), viraramMeta, naoViraram);
    }
}
