package br.com.newe.ms_operacoes.erro;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;

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
import org.springframework.web.client.HttpServerErrorException;
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

    /**
     * O codigo vai na resposta e na primeira linha do log, junto com a rota e a
     * causa raiz: com o codigo que o front mostrar, um grep no log acha o erro.
     */
    private static final Pattern CODIGO_NA_RESPOSTA = Pattern.compile("\"codigo\"\\s*:\\s*\"([^\"]+)\"");

    /**
     * O ms-frota respondeu 5xx. Vira 502 (falha de um servico do qual este
     * depende) e reaproveita o codigo que o ms-frota gravou no log dele: o mesmo
     * codigo acha o erro nos dois servicos.
     */
    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<RespostaErro> falhaNoFrota(HttpServerErrorException e, HttpServletRequest requisicao) {
        Matcher m = CODIGO_NA_RESPOSTA.matcher(e.getResponseBodyAsString());
        String codigo = m.find() ? m.group(1) : novoCodigo();
        log.error("ms-frota falhou [codigo={}] {} {} -> ms-frota respondeu {}: {}",
                codigo, requisicao.getMethod(), rota(requisicao), e.getStatusCode().value(),
                e.getResponseBodyAsString());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(RespostaErro.falha(codigo));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespostaErro> erroInesperado(Exception e, HttpServletRequest requisicao) {
        String codigo = novoCodigo();
        log.error("Erro nao tratado [codigo={}] {} {} -> {}",
                codigo, requisicao.getMethod(), rota(requisicao), causaRaiz(e), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RespostaErro.falha(codigo));
    }

    /** 8 caracteres bastam para achar a linha no log de um dia. */
    static String novoCodigo() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    static String rota(HttpServletRequest requisicao) {
        String query = requisicao.getQueryString();
        return query == null ? requisicao.getRequestURI() : requisicao.getRequestURI() + "?" + query;
    }

    /** A excecao mais interna, que e a que explica (ex.: "relation ... does not exist"). */
    static String causaRaiz(Throwable e) {
        Throwable raiz = e;
        while (raiz.getCause() != null && raiz.getCause() != raiz) {
            raiz = raiz.getCause();
        }
        return raiz.getClass().getSimpleName() + ": " + raiz.getMessage();
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
