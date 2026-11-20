package br.com.newe.ms_operacoes.service.frota;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.newe.ms_operacoes.client.FrotaClient;
import br.com.newe.ms_operacoes.dto.ResultadoTratamentoVeiculosDTO;
import br.com.newe.ms_operacoes.dto.ResultadoTratamentoVeiculosDTO.ManifestoSemVeiculo;
import br.com.newe.ms_operacoes.dto.ResultadoTratamentoVeiculosDTO.TipoSemCategoria;
import br.com.newe.ms_operacoes.dto.VeiculoClassificacao;
import br.com.newe.ms_operacoes.repository.ViagemRepository;

/**
 * Cruza os veiculos das viagens com a classificacao do ms-frota.
 *
 * Nao grava nada: o tipo ja e gravado pela importacao de veiculos e a categoria
 * sai da tabela de-para (tipo_veiculos.id_categoria). Por isso pode ser
 * consultado a qualquer momento, depois de qualquer uma das duas importacoes.
 */
@Service
public class TratamentoVeiculosService {

    private final ViagemRepository viagemRepository;
    private final FrotaClient frotaClient;

    public TratamentoVeiculosService(ViagemRepository viagemRepository, FrotaClient frotaClient) {
        this.viagemRepository = viagemRepository;
        this.frotaClient = frotaClient;
    }

    public ResultadoTratamentoVeiculosDTO resultado() {
        List<UUID> veiculoIds = viagemRepository.buscarIdsVeiculos();
        if (veiculoIds.isEmpty()) {
            return new ResultadoTratamentoVeiculosDTO(0, 0, List.of(), List.of());
        }

        Map<UUID, VeiculoClassificacao> porId = new HashMap<>();
        frotaClient.classificarVeiculos(veiculoIds).forEach(v -> porId.put(v.idVeiculo(), v));

        int comCategoria = 0;
        Map<String, List<String>> placasPorTipo = new TreeMap<>();
        List<UUID> naoIdentificados = new ArrayList<>();
        for (UUID id : veiculoIds) {
            VeiculoClassificacao v = porId.get(id);
            if (v == null || v.tipo() == null) {
                // Sem tipo, ou nem existe mais no ms-frota: nao veio na planilha de veiculos.
                naoIdentificados.add(id);
            } else if (v.categoria() == null) {
                placasPorTipo.computeIfAbsent(v.tipo(), t -> new ArrayList<>()).add(v.placa());
            } else {
                comCategoria++;
            }
        }

        return new ResultadoTratamentoVeiculosDTO(
                veiculoIds.size(),
                comCategoria,
                tiposSemCategoria(placasPorTipo),
                manifestosSemVeiculo(naoIdentificados, porId));
    }

    private static List<TipoSemCategoria> tiposSemCategoria(Map<String, List<String>> placasPorTipo) {
        return placasPorTipo.entrySet().stream()
                .map(e -> new TipoSemCategoria(e.getKey(), e.getValue().size(),
                        e.getValue().stream().sorted().toList()))
                .sorted(Comparator.comparingInt(TipoSemCategoria::quantidade).reversed())
                .toList();
    }

    private List<ManifestoSemVeiculo> manifestosSemVeiculo(List<UUID> ids, Map<UUID, VeiculoClassificacao> porId) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return viagemRepository.buscarManifestosPorVeiculos(ids).stream()
                .map(m -> {
                    VeiculoClassificacao v = porId.get(m.getVeiculoId());
                    return new ManifestoSemVeiculo(m.getManifesto(), v != null ? v.placa() : null, m.getData());
                })
                .toList();
    }
}
