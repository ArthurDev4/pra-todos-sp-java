package br.com.zynox.pratodossp.bo;

import br.com.zynox.pratodossp.dao.SubprefeituraDAO;
import br.com.zynox.pratodossp.dto.SubprefeituraResponse;
import br.com.zynox.pratodossp.exception.RecursoNaoEncontradoException;
import br.com.zynox.pratodossp.exception.RegraNegocioException;
import br.com.zynox.pratodossp.model.Subprefeitura;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Regras das subprefeituras (US01 - escolher o bairro). */
@Service
public class SubprefeituraBO {

    private final SubprefeituraDAO subprefeituraDAO;

    public SubprefeituraBO(SubprefeituraDAO subprefeituraDAO) {
        this.subprefeituraDAO = subprefeituraDAO;
    }

    @Transactional(readOnly = true)
    public List<SubprefeituraResponse> listar() {
        return subprefeituraDAO.findAllByOrderByNomeAsc().stream().map(SubprefeituraResponse::de).toList();
    }

    /** Subprefeitura do caminho da URL; inexistente = 404. */
    public Subprefeitura buscarEntidade(Long id) {
        return subprefeituraDAO.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Subprefeitura não encontrada: " + id));
    }

    /** Converte IDs do corpo da requisicao em subprefeituras; algum inexistente = 400. */
    public Set<Subprefeitura> buscarReferencias(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new LinkedHashSet<>();
        }
        if (ids.stream().anyMatch(Objects::isNull)) {
            throw new RegraNegocioException("A lista de subprefeituras não pode conter valores vazios");
        }
        Set<Long> unicos = new LinkedHashSet<>(ids);
        List<Subprefeitura> encontradas = subprefeituraDAO.findAllById(unicos);
        if (encontradas.size() != unicos.size()) {
            Set<Long> achados = encontradas.stream().map(Subprefeitura::getId).collect(Collectors.toSet());
            List<Long> faltando = unicos.stream().filter(id -> !achados.contains(id)).toList();
            throw new RegraNegocioException("Subprefeitura(s) inexistente(s): " + faltando);
        }
        return new LinkedHashSet<>(encontradas);
    }
}
