package br.com.newe.ms_frota.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.newe.ms_frota.dto.VeiculoLoteItem;
import br.com.newe.ms_frota.models.Veiculo;
import br.com.newe.ms_frota.repository.VeiculoRepository;
import br.com.newe.ms_frota.repository.VeiculoRepository.VeiculoPlacaId;

@Service
public class VeiculoService {

    private final VeiculoRepository repository;

    public VeiculoService(VeiculoRepository repository) {
        this.repository = repository;
    }

    public Optional<Veiculo> buscarPorPlaca(String placa) {
        return repository.findByPlaca(normalizar(placa));
    }

    /** @return id -> placa, para os ids encontrados. */
    public Map<UUID, String> buscarPlacas(List<UUID> ids) {
        Map<UUID, String> placas = new HashMap<>();
        if (ids.isEmpty()) {
            return placas;
        }
        for (Veiculo veiculo : repository.findAllById(ids)) {
            placas.put(veiculo.getIdVeiculo(), veiculo.getPlaca());
        }
        return placas;
    }

    /**
     * Cria o veiculo minimo quando a placa aparece pela primeira vez: viagens.id_veiculo
     * e NOT NULL e o CSV de manifestos so traz a placa. Os demais dados entram
     * depois, pelo CRUD de frota.
     */
    @Transactional
    public Map<String, UUID> upsertLote(List<VeiculoLoteItem> itens) {
        Map<String, VeiculoLoteItem> porPlaca = new LinkedHashMap<>();
        for (VeiculoLoteItem item : itens) {
            String placa = normalizar(item.placa());
            if (placa != null) {
                porPlaca.putIfAbsent(placa, item);
            }
        }
        if (porPlaca.isEmpty()) {
            return Map.of();
        }

        Map<String, UUID> placaParaId = new HashMap<>();
        for (VeiculoPlacaId existente : repository.buscarIdsPorPlaca(porPlaca.keySet())) {
            placaParaId.put(existente.getPlaca(), existente.getId());
        }

        List<Veiculo> novos = new ArrayList<>();
        for (Map.Entry<String, VeiculoLoteItem> entrada : porPlaca.entrySet()) {
            if (!placaParaId.containsKey(entrada.getKey())) {
                novos.add(montar(entrada.getKey(), entrada.getValue()));
            }
        }

        for (Veiculo salvo : repository.saveAll(novos)) {
            placaParaId.put(salvo.getPlaca(), salvo.getIdVeiculo());
        }

        return placaParaId;
    }

    /** Placa e chave unica: sem normalizar, "abc1d23" viraria um veiculo diferente de "ABC1D23". */
    private String normalizar(String placa) {
        if (placa == null) {
            return null;
        }
        String texto = placa.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        return texto.isEmpty() ? null : texto;
    }

    private Veiculo montar(String placa, VeiculoLoteItem item) {
        Veiculo veiculo = new Veiculo();
        veiculo.setPlaca(placa);
        veiculo.setCapacidade(item.capacidade());
        veiculo.setNomeAgregado(item.nomeAgregado());
        veiculo.setStatusVeiculo(Veiculo.STATUS_ATIVO);
        return veiculo;
    }
}
