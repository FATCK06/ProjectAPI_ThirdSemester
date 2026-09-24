package br.com.newe.ms_operacoes.client;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import br.com.newe.ms_operacoes.dto.AgregadoLoteItem;
import br.com.newe.ms_operacoes.dto.MotoristaLoteItem;
import br.com.newe.ms_operacoes.dto.MotoristaResumo;
import br.com.newe.ms_operacoes.dto.VeiculoLoteItem;

/**
 * Acesso ao ms-frota, dono de motoristas, veiculos e agregados.
 *
 * Sempre em lote: um manifesto tem centenas de linhas, e uma chamada por
 * registro
 * seria inviavel. Depois da deduplicacao no ManifestoBatchWriter, o arquivo de
 * abr-jun vira 3 requisicoes - 150 motoristas, 193 agregados e as placas
 * distintas.
 */
@Component
public class FrotaClient {

    private static final ParameterizedTypeReference<Map<String, UUID>> MAPA_CHAVE_ID = new ParameterizedTypeReference<>() {
    };

    private static final ParameterizedTypeReference<List<MotoristaResumo>> LISTA_MOTORISTAS = new ParameterizedTypeReference<>() {
    };

    private static final ParameterizedTypeReference<Map<UUID, String>> MAPA_ID_PLACA = new ParameterizedTypeReference<>() {
    };

    private final RestClient restClient;

    // Builder estatico em vez de injetar RestClient.Builder: esse bean depende de
    // auto-configuracao que nem sempre esta presente, e aqui nao precisamos de
    // nenhuma customizacao global.
    // O interceptor abaixo repassa o token da requisicao atual ao ms-frota
    // (sem ele a chamada sai sem Authorization).
    // A fabrica simples (HttpURLConnection) evita o travamento do cliente HTTP
    // padrao e permite timeout: sem ele, a importacao ficava esperando para
    // sempre se o ms-frota nao respondesse.
    public FrotaClient(@Value("${frota.base-url}") String baseUrl) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(10));
        fabrica.setReadTimeout(Duration.ofSeconds(120));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(fabrica)
                .requestInterceptor((request, body, execution) -> {
                    String token = tokenAtual();
                    if (token != null) {
                        request.getHeaders().set(HttpHeaders.AUTHORIZATION, token);
                    }
                    return execution.execute(request, body);
                })
                .build();
    }

    /** @return cpf -> id, para todos os CPFs enviados. */
    public Map<String, UUID> upsertMotoristas(List<MotoristaLoteItem> itens) {
        return postLote("/api/motoristas/lote", itens);
    }

    /** @return documento -> id. */
    public Map<String, UUID> upsertAgregados(List<AgregadoLoteItem> itens) {
        return postLote("/api/agregados/lote", itens);
    }

    /** @return placa (normalizada pelo ms-frota) -> id. */
    public Map<String, UUID> upsertVeiculos(List<VeiculoLoteItem> itens) {
        return postLote("/api/veiculos/lote", itens);
    }

    /** Nome e CPF dos motoristas informados (os nao encontrados ficam de fora). */
    public List<MotoristaResumo> buscarMotoristas(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }

        List<MotoristaResumo> resposta = restClient.post()
                .uri("/api/motoristas/resumos")
                .body(ids)
                .retrieve()
                .body(LISTA_MOTORISTAS);

        return resposta != null ? resposta : List.of();
    }

    /** @return id -> placa, para os veiculos encontrados. */
    public Map<UUID, String> buscarPlacas(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }

        Map<UUID, String> resposta = restClient.post()
                .uri("/api/veiculos/placas")
                .body(ids)
                .retrieve()
                .body(MAPA_ID_PLACA);

        return resposta != null ? resposta : Map.of();
    }

    private Map<String, UUID> postLote(String uri, List<?> itens) {
        if (itens.isEmpty()) {
            return Map.of();
        }

        Map<String, UUID> resposta = restClient.post()
                .uri(uri)
                .body(itens)
                .retrieve()
                .body(MAPA_CHAVE_ID);

        return resposta != null ? resposta : Map.of();
    }

    /**
     * Le o header Authorization da requisicao HTTP que disparou a importacao.
     * Funciona porque o /executar roda na mesma thread da requisicao.
     */
    private static String tokenAtual() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servlet) {
            return servlet.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
        }
        return null;
    }
}