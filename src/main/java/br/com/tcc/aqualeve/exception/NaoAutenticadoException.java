package br.com.tcc.aqualeve.exception;

// 401: login inválido
public class NaoAutenticadoException extends RuntimeException {

    public NaoAutenticadoException(String mensagem) {
        super(mensagem);
    }
}
