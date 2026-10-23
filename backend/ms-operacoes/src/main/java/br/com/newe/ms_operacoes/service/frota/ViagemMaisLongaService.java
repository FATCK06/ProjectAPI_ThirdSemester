package br.com.newe.ms_operacoes.service.frota;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.newe.ms_operacoes.client.FrotaClient;
import br.com.newe.ms_operacoes.repository.ViagemRepository;
import br.com.newe.ms_operacoes.repository.ViagemRepository.ViagemKmView;

/**
 * Tipo de veiculo de cada motorista no mes: o da viagem mais longa. Regra unica
 * para o ranking mensal e para o top N do dashboard, que antes calculavam a
 * distancia de jeitos diferentes.
 *
 * No manifesto, "Km saida" ja e a distancia da rota, nao o hodometro: bate com o
 * km escrito nas observacoes em 96% dos casos, e "Km chegada" vem sempre 0. Zero
 * ali significa nao informado. Em empate, fica a viagem mais recente; sem nenhuma
 * distancia informada, tambem.
 */
@Service
public class ViagemMaisLongaService {

    static final String TIPO_NAO_INFORMADO = "Não informado";

    public record ViagemMaisLonga(String tipoVeiculo, Integer distanciaKm) {
    }

    private final ViagemRepository repository;
    private final FrotaClient frotaClient;

    public ViagemMaisLongaService(ViagemRepository repository, FrotaClient frotaClient) {
        this.repository = repository;
        this.frotaClient = frotaClient;
    }

    /** Motorista sem viagem no mes fica fora do mapa. */
    public Map<UUID, ViagemMaisLonga> porMotorista(String mesReferencia, Collection<UUID> motoristaIds) {
        if (motoristaIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, ViagemKmView> maisLonga = maisLongaPorMotorista(
                repository.kmViagensPorMotoristas(mesReferencia, motoristaIds));

        // Modelo pertence ao ms-frota: uma chamada em lote para o mes inteiro.
        List<UUID> veiculoIds = maisLonga.values().stream().map(ViagemKmView::getVeiculoId).distinct().toList();
        Map<UUID, String> tipos = frotaClient.buscarTiposVeiculo(veiculoIds);

        Map<UUID, ViagemMaisLonga> resultado = new HashMap<>();
        maisLonga.forEach((motoristaId, viagem) -> resultado.put(motoristaId, new ViagemMaisLonga(
                tipos.getOrDefault(viagem.getVeiculoId(), TIPO_NAO_INFORMADO),
                distancia(viagem))));
        return resultado;
    }

    /** @param viagens da mais recente para a mais antiga, como a consulta devolve */
    static Map<UUID, ViagemKmView> maisLongaPorMotorista(List<ViagemKmView> viagens) {
        Map<UUID, ViagemKmView> maisLonga = new HashMap<>();
        for (ViagemKmView viagem : viagens) {
            ViagemKmView atual = maisLonga.get(viagem.getMotoristaId());
            Integer distancia = distancia(viagem);
            if (atual == null || (distancia != null && (distancia(atual) == null || distancia > distancia(atual)))) {
                maisLonga.put(viagem.getMotoristaId(), viagem);
            }
        }
        return maisLonga;
    }

    static Integer distancia(ViagemKmView viagem) {
        Integer km = viagem.getKmSaida();
        return km != null && km > 0 ? km : null;
    }
}
