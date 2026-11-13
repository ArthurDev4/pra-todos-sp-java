package br.com.zynox.pratodossp.exception;

/** Requisicao viola uma regra de negocio (ex.: meta PENDENTE com regioes). Vira HTTP 400. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
