package br.com.ecomovement.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.sql.SQLException;
import java.util.Map;

@RestControllerAdvice
public class TratadorDeErros {
    private static final Logger log = LoggerFactory.getLogger(TratadorDeErros.class);

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<Map<String, Object>> regraNegocio(RegraNegocioException e) {
        return resposta(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(NaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> naoEncontrado(NaoEncontradoException e) {
        return resposta(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(NaoAutorizadoException.class)
    public ResponseEntity<Map<String, Object>> naoAutorizado(NaoAutorizadoException e) {
        return resposta(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<Map<String, Object>> conflito(ConflitoException e) {
        return resposta(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class})
    public ResponseEntity<Map<String, Object>> requisicaoInvalida(Exception e) {
        return resposta(HttpStatus.BAD_REQUEST, "Requisição inválida: verifique os dados enviados.");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> arquivoGrande(MaxUploadSizeExceededException e) {
        return resposta(HttpStatus.BAD_REQUEST, "O arquivo deve ter no máximo 50 MB.");
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Map<String, Object>> erroBanco(SQLException e) {
        // O detalhe técnico fica só no log do servidor; o cliente recebe uma mensagem genérica.
        log.error("Erro de banco de dados (SQLState={}, código={})", e.getSQLState(), e.getErrorCode(), e);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro ao acessar o banco de dados. Tente novamente em instantes.");
    }

    /**
     * Rede de segurança: qualquer exceção não prevista acima volta no mesmo formato {status, mensagem}.
     * Erros do próprio Spring (rota inexistente, método não permitido etc.) mantêm o status original.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> erroInesperado(Exception e) {
        if (e instanceof ErrorResponse erro) {
            HttpStatus status = HttpStatus.resolve(erro.getStatusCode().value());
            if (status != null && status.is4xxClientError()) {
                log.warn("Requisição rejeitada ({}): {}", status.value(), e.getMessage());
                return resposta(status, mensagemPadrao(status));
            }
        }
        log.error("Erro inesperado", e);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor. Tente novamente em instantes.");
    }

    private String mensagemPadrao(HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> "Recurso não encontrado.";
            case METHOD_NOT_ALLOWED -> "Método HTTP não permitido para este endereço.";
            case UNSUPPORTED_MEDIA_TYPE -> "Tipo de conteúdo não suportado.";
            default -> "Requisição inválida: verifique os dados enviados.";
        };
    }

    private ResponseEntity<Map<String, Object>> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(Map.of("status", status.value(), "mensagem", mensagem));
    }
}
