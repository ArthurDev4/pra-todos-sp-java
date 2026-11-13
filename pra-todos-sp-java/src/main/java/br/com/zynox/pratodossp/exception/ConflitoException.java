package br.com.zynox.pratodossp.exception;

/** Operacao conflita com o estado atual dos dados (ex.: numero de meta repetido). Vira HTTP 409. */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
