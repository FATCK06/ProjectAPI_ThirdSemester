package br.com.newe.ms_operacoes.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.newe.ms_operacoes.client.FrotaClient;
import br.com.newe.ms_operacoes.dto.MotoristaResumo;
import br.com.newe.ms_operacoes.dto.RankingMotoristaDTO;
import br.com.newe.ms_operacoes.repository.ViagemRepository;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final ViagemRepository repository;
    private final FrotaClient frotaClient;

    public DashboardController(ViagemRepository repository, FrotaClient frotaClient) {
        this.repository = repository;
        this.frotaClient = frotaClient;
    }

    @GetMapping("/ranking-motoristas") // "/api/dashboard/ranking-motoristas?mesReferencia=2026-09&limite=5"
    public ResponseEntity<List<RankingMotoristaDTO>> rankingMotoristas(
            @RequestParam("mesReferencia") String mesReferencia,
            @RequestParam(value = "limite", defaultValue = "5") int limite
    ) {
        if (!mesReferencia.matches("\\d{4}-\\d{2}") || limite < 1 || limite > 50) {
            return ResponseEntity.badRequest().build();
        }

        List<ViagemRepository.RankingMotoristaView> linhas =
                repository.rankingMotoristas(mesReferencia, PageRequest.of(0, limite));

        if (linhas.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<UUID> motoristaIds = linhas.stream().map(ViagemRepository.RankingMotoristaView::getMotoristaId).toList();

        // Viagem mais longa por motorista (kmChegada - kmSaida); em empate, fica a mais recente
        Map<UUID, ViagemRepository.ViagemKmView> maisLonga = new HashMap<>();
        Map<UUID, Integer> maiorDistancia = new HashMap<>();
        for (ViagemRepository.ViagemKmView v : repository.kmViagensPorMotoristas(mesReferencia, motoristaIds)) {
            Integer distancia = (v.getKmSaida() != null && v.getKmChegada() != null)
                    ? v.getKmChegada() - v.getKmSaida()
                    : null;

            if (!maisLonga.containsKey(v.getMotoristaId())) {
                maisLonga.put(v.getMotoristaId(), v);
                maiorDistancia.put(v.getMotoristaId(), distancia);
                continue;
            }

            Integer atual = maiorDistancia.get(v.getMotoristaId());
            if (distancia != null && (atual == null || distancia > atual)) {
                maisLonga.put(v.getMotoristaId(), v);
                maiorDistancia.put(v.getMotoristaId(), distancia);
            }
        }

        // Nome, CPF e placa pertencem ao ms-frota: uma chamada em lote para cada
        Map<UUID, MotoristaResumo> motoristas = frotaClient.buscarMotoristas(motoristaIds).stream()
                .collect(Collectors.toMap(MotoristaResumo::id, Function.identity()));
        List<UUID> veiculoIds = maisLonga.values().stream()
                .map(ViagemRepository.ViagemKmView::getVeiculoId)
                .distinct()
                .toList();
        Map<UUID, String> placas = frotaClient.buscarPlacas(veiculoIds);

        List<RankingMotoristaDTO> ranking = new ArrayList<>();
        for (int i = 0; i < linhas.size(); i++) {
            ViagemRepository.RankingMotoristaView linha = linhas.get(i);
            MotoristaResumo motorista = motoristas.get(linha.getMotoristaId());
            ViagemRepository.ViagemKmView viagem = maisLonga.get(linha.getMotoristaId());
            ranking.add(new RankingMotoristaDTO(
                    i + 1,
                    linha.getMotoristaId(),
                    motorista != null ? motorista.nome() : null,
                    motorista != null ? motorista.cpf() : null,
                    linha.getTotalViagens(),
                    viagem != null ? placas.get(viagem.getVeiculoId()) : null,
                    maiorDistancia.get(linha.getMotoristaId())
            ));
        }

        return ResponseEntity.ok(ranking);
    }
}
