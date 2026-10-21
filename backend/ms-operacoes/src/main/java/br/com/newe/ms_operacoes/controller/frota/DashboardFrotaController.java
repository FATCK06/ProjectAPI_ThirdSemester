package br.com.newe.ms_operacoes.controller.frota;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.newe.ms_operacoes.client.FrotaClient;
import br.com.newe.ms_operacoes.dto.MotoristaResumo;
import br.com.newe.ms_operacoes.dto.frota.MotoristaMesDTO;
import br.com.newe.ms_operacoes.dto.frota.RankingMotoristaDTO;
import br.com.newe.ms_operacoes.repository.ViagemRepository;
import br.com.newe.ms_operacoes.security.PerfilResolver;
import br.com.newe.ms_operacoes.service.frota.MotoristasMesService;
import br.com.newe.ms_operacoes.service.frota.ViagemMaisLongaService;
import br.com.newe.ms_operacoes.service.frota.ViagemMaisLongaService.ViagemMaisLonga;
import br.com.newe.ms_operacoes.service.indicadores.IndicadoresPorModelo;
import br.com.newe.ms_operacoes.service.indicadores.IndicadoresService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardFrotaController {

    private final ViagemRepository repository;
    private final FrotaClient frotaClient;
    private final IndicadoresService indicadoresService;
    private final MotoristasMesService motoristasMesService;
    private final ViagemMaisLongaService viagemMaisLongaService;
    private final PerfilResolver perfilResolver;

    public DashboardFrotaController(ViagemRepository repository, FrotaClient frotaClient,
            IndicadoresService indicadoresService, MotoristasMesService motoristasMesService,
            ViagemMaisLongaService viagemMaisLongaService, PerfilResolver perfilResolver) {
        this.repository = repository;
        this.frotaClient = frotaClient;
        this.indicadoresService = indicadoresService;
        this.motoristasMesService = motoristasMesService;
        this.viagemMaisLongaService = viagemMaisLongaService;
        this.perfilResolver = perfilResolver;
    }

    /**
     * Lista os indicadores mensais dos motoristas. Pageable permite que o Spring
     * converta page, size e sort da query string para paginação no repositório.
     */
    @GetMapping("/motoristas")
    public ResponseEntity<Page<MotoristaMesDTO>> motoristasMes(
            @RequestParam("mesReferencia") String mesReferencia,
            Pageable pageable) {
        // O mês é obrigatório e a página/ordenação são limitadas para evitar
        // consultas inválidas ou páginas excessivamente grandes.
        if (!mesReferencia.matches("\\d{4}-(0[1-9]|1[0-2])")
                || pageable.getPageSize() > 100
                || !ordenacaoMensalValida(pageable.getSort())) {
            return ResponseEntity.badRequest().<Page<MotoristaMesDTO>>build();
        }

        // Uma ordenação determinística evita mudanças arbitrárias entre páginas.
        Pageable pagina = pageable.getSort().isUnsorted()
                ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("motoristaId"))
                : pageable;
        return ResponseEntity.ok(motoristasMesService.listar(mesReferencia, pagina));
    }

    @GetMapping("/ranking-motoristas") // "/api/dashboard/ranking-motoristas?mesReferencia=2026-09&limite=5"
    public ResponseEntity<List<RankingMotoristaDTO>> rankingMotoristas(
            @RequestParam("mesReferencia") String mesReferencia,
            @RequestParam(value = "limite", defaultValue = "5") int limite) {
        if (!mesReferencia.matches("\\d{4}-\\d{2}") || limite < 1 || limite > 50) {
            return ResponseEntity.badRequest().build();
        }

        List<ViagemRepository.RankingMotoristaView> linhas = repository.rankingMotoristas(mesReferencia,
                PageRequest.of(0, limite));

        if (linhas.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<UUID> motoristaIds = linhas.stream().map(ViagemRepository.RankingMotoristaView::getMotoristaId).toList();

        // Mesma regra do ranking mensal: ver ViagemMaisLongaService.
        Map<UUID, ViagemMaisLonga> maisLonga = viagemMaisLongaService.porMotorista(mesReferencia, motoristaIds);

        // Nome e CPF pertencem ao ms-frota: uma chamada em lote.
        Map<UUID, MotoristaResumo> motoristas = frotaClient.buscarMotoristas(motoristaIds).stream()
                .collect(Collectors.toMap(MotoristaResumo::id, Function.identity()));

        List<RankingMotoristaDTO> ranking = new ArrayList<>();
        for (int i = 0; i < linhas.size(); i++) {
            ViagemRepository.RankingMotoristaView linha = linhas.get(i);
            MotoristaResumo motorista = motoristas.get(linha.getMotoristaId());
            ViagemMaisLonga viagem = maisLonga.get(linha.getMotoristaId());
            ranking.add(new RankingMotoristaDTO(
                    i + 1,
                    linha.getMotoristaId(),
                    motorista != null ? motorista.nome() : null,
                    motorista != null ? motorista.cpf() : null,
                    linha.getTotalViagens(),
                    viagem != null ? viagem.tipoVeiculo() : null,
                    viagem != null ? viagem.distanciaKm() : null));
        }

        return ResponseEntity.ok(ranking);
    }

    @GetMapping("/indicadores-por-modelo")
    public ResponseEntity<List<IndicadoresPorModelo>> indicadoresPorModelo(
            @RequestParam("mesReferencia") String mesReferencia,
            HttpServletRequest request) {
        if (!mesReferencia.matches("\\d{4}-(0[1-9]|1[0-2])")) {
            return ResponseEntity.badRequest().build();
        }
        List<IndicadoresPorModelo> porModelo = indicadoresService.calcularPorModelo(mesReferencia);
        if (!perfilResolver.podeVerDadosFinanceiros(request)) {
            porModelo = porModelo.stream().map(IndicadoresPorModelo::semValoresFinanceiros).toList();
        }
        return ResponseEntity.ok(porModelo);
    }

    /** Restringe sort aos campos da projeção agregada aceitos pela consulta. */
    private static boolean ordenacaoMensalValida(Sort sort) {
        return sort.stream().allMatch(order -> switch (order.getProperty()) {
            case "motoristaId", "veiculoId", "viagensNoMes", "diasEmOperacao" -> true;
            default -> false;
        });
    }
}
