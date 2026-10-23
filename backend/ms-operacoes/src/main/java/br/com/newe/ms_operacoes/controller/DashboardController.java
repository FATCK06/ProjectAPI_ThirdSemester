package br.com.newe.ms_operacoes.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.newe.ms_operacoes.security.PerfilResolver;
import br.com.newe.ms_operacoes.service.indicadores.IndicadoresMes;
import br.com.newe.ms_operacoes.service.indicadores.IndicadoresService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final IndicadoresService indicadoresService;
    private final PerfilResolver perfilResolver;

    public DashboardController(IndicadoresService indicadoresService, PerfilResolver perfilResolver) {
        this.indicadoresService = indicadoresService;
        this.perfilResolver = perfilResolver;
    }

    @GetMapping("/indicadores") // "/api/dashboard/indicadores?mesReferencia=2026-04"
    public ResponseEntity<IndicadoresMes> indicadores(
            @RequestParam("mesReferencia") @MesReferencia String mesReferencia,
            @RequestParam(value = "ranking", defaultValue = "false") boolean ranking,
            HttpServletRequest request) {
        IndicadoresMes indicadores = indicadoresService.calcular(mesReferencia);
        boolean ocultarValores = !perfilResolver.podeVerDadosFinanceiros(request);
        return ResponseEntity.ok(ocultarValores ? indicadores.semValoresFinanceiros() : indicadores);
    }
}
