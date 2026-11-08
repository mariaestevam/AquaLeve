package br.com.tcc.aqualeve.exception;

// 403: usuário logado, mas sem permissão sobre aquele recurso
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
