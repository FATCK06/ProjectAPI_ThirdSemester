package br.com.newe.ms_operacoes.client;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import br.com.newe.ms_operacoes.dto.AgregadoLoteItem;
import br.com.newe.ms_operacoes.dto.MotoristaLoteItem;
import br.com.newe.ms_operacoes.dto.VeiculoLoteItem;

/**
 * Acesso ao ms-frota, dono de motoristas, veiculos e agregados.
 *
 * Sempre em lote: um manifesto tem centenas de linhas, e uma chamada por registro
 * seria inviavel. Depois da deduplicacao no ManifestoBatchWriter, o arquivo de
 * abr-jun vira 3 requisicoes - 150 motoristas, 193 agregados e as placas distintas.
 */
@Component
public class FrotaClient {

    private static final ParameterizedTypeReference<Map<String, UUID>> MAPA_CHAVE_ID =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    // Builder estatico em vez de injetar RestClient.Builder: esse bean depende de
    // auto-configuracao que nem sempre esta presente, e aqui nao precisamos de
    // nenhuma customizacao global.
    public FrotaClient(@Value("${frota.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
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
}
