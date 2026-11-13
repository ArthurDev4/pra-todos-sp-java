package br.com.zynox.pratodossp.bo;

import br.com.zynox.pratodossp.dao.EixoDAO;
import br.com.zynox.pratodossp.dto.EixoResponse;
import br.com.zynox.pratodossp.exception.RegraNegocioException;
import br.com.zynox.pratodossp.model.Eixo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Eixos tematicos das metas (tabela de referencia do modelo da equipe). */
@Service
public class EixoBO {

    private final EixoDAO eixoDAO;

    public EixoBO(EixoDAO eixoDAO) {
        this.eixoDAO = eixoDAO;
    }

    @Transactional(readOnly = true)
    public List<EixoResponse> listar() {
        return eixoDAO.findAllByOrderByIdAsc().stream().map(EixoResponse::de).toList();
    }

    /** Eixo citado no corpo da requisicao; inexistente = 400. */
    public Eixo buscarReferencia(Long id) {
        return eixoDAO.findById(id)
                .orElseThrow(() -> new RegraNegocioException("O eixo informado não existe: " + id));
    }
}
