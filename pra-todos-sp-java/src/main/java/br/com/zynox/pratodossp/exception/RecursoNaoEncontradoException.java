package br.com.zynox.pratodossp.exception;

/** Recurso do caminho da URL nao existe. Vira HTTP 404. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
