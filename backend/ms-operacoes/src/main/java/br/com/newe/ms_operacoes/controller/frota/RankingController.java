package br.com.newe.ms_operacoes.controller.frota;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.newe.ms_operacoes.controller.MesReferencia;
import br.com.newe.ms_operacoes.dto.frota.RankingMensalMotoristaDTO;
import br.com.newe.ms_operacoes.erro.DadoInvalidoException;
import br.com.newe.ms_operacoes.security.PerfilResolver;
<<<<<<< HEAD
import br.com.newe.ms_operacoes.security.Permissao;
=======
>>>>>>> origin/develop
import br.com.newe.ms_operacoes.service.frota.RankingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Pattern;

@RestController
@RequestMapping("/api/ranking")
public class RankingController {
    private final RankingService rankingService;
    private final PerfilResolver perfilResolver;

    public RankingController(RankingService rankingService, PerfilResolver perfilResolver) {
        this.rankingService = rankingService;
        this.perfilResolver = perfilResolver;
    }

    @GetMapping("/mensal")
    public ResponseEntity<List<RankingMensalMotoristaDTO>> mensal(
            @RequestParam("mesReferencia") @MesReferencia String mesReferencia,
            @RequestParam(value = "ordenarPor", defaultValue = "rentabilidade") String ordenarPor,
            @RequestParam(value = "direcao", defaultValue = "desc")
            @Pattern(regexp = "(?i)asc|desc", message = "use asc ou desc") String direcao,
            HttpServletRequest request) {
        if (!RankingService.colunaValida(ordenarPor)) {
            throw new DadoInvalidoException("ordenarPor", "coluna inválida");
        }
        boolean ocultarValores = !perfilResolver.possui(request, Permissao.VER_DADOS_FINANCEIROS);
        return ResponseEntity.ok(rankingService.rankingMensal(mesReferencia, ordenarPor, direcao, ocultarValores));
    }
}
