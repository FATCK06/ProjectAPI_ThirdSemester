package br.com.newe.ms_operacoes.controller.frota;

import java.util.List;
import java.util.Locale;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.newe.ms_operacoes.dto.frota.SituacaoMotoristaDTO;
import br.com.newe.ms_operacoes.models.enums.frota.SituacaoMotorista;
import br.com.newe.ms_operacoes.service.frota.SituacaoMotoristaService;

/**
 * Situacao dos motoristas no mes, classificada sobre os dias gravados na
 * importacao (controle_disponibilidade).
 *
 * Escopo: so motoristas com viagem no mes - quem nao rodou nenhum dia nao tem
 * linha em controle_disponibilidade.
 */
@RestController
@RequestMapping("/api/dashboard/motoristas")
public class MotoristaSituacaoController {

    private final SituacaoMotoristaService situacaoMotoristaService;

    public MotoristaSituacaoController(SituacaoMotoristaService situacaoMotoristaService) {
        this.situacaoMotoristaService = situacaoMotoristaService;
    }

    @GetMapping("/situacao") // "/api/dashboard/motoristas/situacao?mesReferencia=2026-04&situacao=DISPONIVEL"
    public ResponseEntity<List<SituacaoMotoristaDTO>> situacao(
            @RequestParam("mesReferencia") String mesReferencia,
            @RequestParam(value = "situacao", required = false) String situacao) {
        if (!mesReferencia.matches("\\d{4}-(0[1-9]|1[0-2])")) {
            return ResponseEntity.badRequest().build();
        }

        SituacaoMotorista filtro = null;
        if (situacao != null && !situacao.isBlank()) {
            try {
                // Sem diferenciar caixa: "disponivel" vale o mesmo que "DISPONIVEL".
                filtro = SituacaoMotorista.valueOf(situacao.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }

        return ResponseEntity.ok(situacaoMotoristaService.listar(mesReferencia, filtro));
    }
}
