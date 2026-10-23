package br.com.newe.ms_operacoes.erro;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Padroniza toda resposta de erro no formato de {@link RespostaErro}.
 *
 * Os erros do proprio Spring (parametro faltando, tipo errado, JSON quebrado,
 * arquivo grande, rota inexistente) passam pela classe base, que ja escolhe o
 * status certo; aqui so trocamos o corpo. O que sobra vira 500 com mensagem
 * generica, e o detalhe fica apenas no log.
 *
 * Mensagem de regra de negocio (ex.: "ja existe uma importacao sendo gravada")
 * continua sendo devolvida pelo proprio controller: so detalhe tecnico e escondido.
 */
@RestControllerAdvice
public class ErroHandlerGlobal extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ErroHandlerGlobal.class);

    private final String mensagemArquivoGrande;

    public ErroHandlerGlobal(@Value("${spring.servlet.multipart.max-file-size:10MB}") DataSize limiteArquivo) {
        this.mensagemArquivoGrande = "O arquivo excede o tamanho máximo de " + limiteArquivo.toMegabytes() + "MB.";
    }

    @ExceptionHandler(DadoInvalidoException.class)
    public ResponseEntity<RespostaErro> dadoInvalido(DadoInvalidoException e) {
        return ResponseEntity.badRequest()
                .body(new RespostaErro(RespostaErro.DADOS_INVALIDOS, Map.of(e.getCampo(), e.getMotivo())));
    }

    /** Outro servico fora do ar (ex.: o ms-frota, chamado pelo ms-operacoes): 503, nao 500. */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<RespostaErro> servicoIndisponivel(ResourceAccessException e) {
        log.warn("Servico externo indisponivel", e);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new RespostaErro(RespostaErro.FALHA_GENERICA));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespostaErro> erroInesperado(Exception e) {
        log.error("Erro nao tratado", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RespostaErro(RespostaErro.FALHA_GENERICA));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
            HttpStatusCode statusCode, WebRequest request) {
        ResponseEntity<Object> padrao = super.handleExceptionInternal(ex, body, headers, statusCode, request);
        if (padrao == null) {
            // Resposta ja enviada: nao ha mais como trocar o corpo.
            return null;
        }
        return ResponseEntity.status(padrao.getStatusCode())
                .headers(padrao.getHeaders())
                .body(corpo(ex, padrao.getStatusCode()));
    }

    private RespostaErro corpo(Exception ex, HttpStatusCode status) {
        if (status.value() == HttpStatus.CONTENT_TOO_LARGE.value()) {
            return new RespostaErro(mensagemArquivoGrande);
        }
        if (status.value() == HttpStatus.BAD_REQUEST.value()) {
            return new RespostaErro(RespostaErro.DADOS_INVALIDOS, campos(ex));
        }
        return new RespostaErro(RespostaErro.FALHA_GENERICA);
    }

    /** Campo e motivo, para o front destacar o que corrigir. Vazio quando nao da para saber. */
    private Map<String, String> campos(Exception ex) {
        Map<String, String> campos = new LinkedHashMap<>();

        if (ex instanceof MethodArgumentNotValidException e) {
            e.getBindingResult().getFieldErrors()
                    .forEach(f -> campos.putIfAbsent(f.getField(), f.getDefaultMessage()));

        } else if (ex instanceof HandlerMethodValidationException e) {
            e.getParameterValidationResults().forEach(r -> r.getResolvableErrors()
                    .forEach(erro -> campos.putIfAbsent(
                            r.getMethodParameter().getParameterName(), erro.getDefaultMessage())));

        } else if (ex instanceof MissingServletRequestParameterException e) {
            campos.put(e.getParameterName(), "obrigatório");

        } else if (ex instanceof MissingServletRequestPartException e) {
            campos.put(e.getRequestPartName(), "obrigatório");

        } else if (ex instanceof TypeMismatchException e && e.getPropertyName() != null) {
            campos.put(e.getPropertyName(), "formato inválido");
        }

        return campos;
    }
}
