package br.com.tcc.aqualeve.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

// Qualquer exceção que os controllers não tratarem vira uma tela de erro amigável (erro.html)
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ModelAndView tratarRecursoNaoEncontrado(RecursoNaoEncontradoException exception) {
        return montarTela(HttpStatus.NOT_FOUND, "Página ou registro não encontrado", exception.getMessage());
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ModelAndView tratarAcessoNegado(AcessoNegadoException exception) {
        return montarTela(HttpStatus.FORBIDDEN, "Acesso negado", exception.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ModelAndView tratarRegraNegocio(RegraNegocioException exception) {
        return montarTela(HttpStatus.BAD_REQUEST, "Não foi possível concluir", exception.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ModelAndView tratarIntegridade(DataIntegrityViolationException exception) {
        return montarTela(HttpStatus.CONFLICT, "Conflito de dados",
                "A operação viola uma regra de integridade (registro duplicado ou com vínculos).");
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ModelAndView tratarUpload(Exception e) {
        return montarTela(HttpStatus.PAYLOAD_TOO_LARGE, "Imagem muito grande", "Envie uma foto de até 5 MB. Volte ao formulário para tentar novamente.");
    }

    private ModelAndView montarTela(HttpStatus status, String titulo, String mensagem) {
        ModelAndView tela = new ModelAndView("erro");
        tela.setStatus(status);
        tela.addObject("codigo", status.value());
        tela.addObject("titulo", titulo);
        tela.addObject("mensagem", mensagem);
        return tela;
    }
}
