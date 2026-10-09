package br.com.newe.ms_operacoes.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.newe.ms_operacoes.dto.RankingMensalMotoristaDTO;
import br.com.newe.ms_operacoes.service.RankingService;

@RestController
@RequestMapping("/api/ranking")
public class RankingController {
    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping("/mensal")
    public ResponseEntity<List<RankingMensalMotoristaDTO>> mensal(
            @RequestParam("mesReferencia") String mesReferencia,
            @RequestParam(value = "ordenarPor", defaultValue = "rentabilidade") String ordenarPor,
            @RequestParam(value = "direcao", defaultValue = "desc") String direcao) {
        if (!mesReferencia.matches("\\d{4}-(0[1-9]|1[0-2])")
                || !RankingService.colunaValida(ordenarPor) || !RankingService.direcaoValida(direcao)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(rankingService.rankingMensal(mesReferencia, ordenarPor, direcao));
    }
}
