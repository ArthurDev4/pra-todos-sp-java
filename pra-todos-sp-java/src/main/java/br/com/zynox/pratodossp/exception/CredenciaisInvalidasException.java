package br.com.zynox.pratodossp.exception;

/** E-mail ou senha do administrador incorretos. Vira HTTP 401. */
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("E-mail ou senha inválidos");
    }
}
