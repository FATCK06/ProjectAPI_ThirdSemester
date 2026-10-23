package br.com.newe.ms_operacoes.controller;

<<<<<<< HEAD
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

=======
>>>>>>> origin/develop
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

<<<<<<< HEAD
import br.com.newe.ms_operacoes.dto.MesesReferenciaDTO;
=======
>>>>>>> origin/develop
import br.com.newe.ms_operacoes.security.PerfilResolver;
import br.com.newe.ms_operacoes.security.Permissao;
import br.com.newe.ms_operacoes.service.indicadores.HistoricoMes;
import br.com.newe.ms_operacoes.service.indicadores.IndicadoresMes;
import br.com.newe.ms_operacoes.service.indicadores.IndicadoresService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

<<<<<<< HEAD
    /** A operacao e no Brasil: o mes vira a meia-noite de Brasilia, nao em UTC. */
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

=======
>>>>>>> origin/develop
    private final IndicadoresService indicadoresService;
    private final PerfilResolver perfilResolver;

    public DashboardController(IndicadoresService indicadoresService, PerfilResolver perfilResolver) {
        this.indicadoresService = indicadoresService;
        this.perfilResolver = perfilResolver;
    }

    /**
     * Mes atual e anterior pelo relogio do servidor, para o filtro do dashboard
     * nao depender da data da maquina de quem acessa.
     */
    @GetMapping("/meses-referencia")
    public MesesReferenciaDTO mesesReferencia() {
        YearMonth atual = YearMonth.now(FUSO);
        return new MesesReferenciaDTO(atual.toString(), atual.minusMonths(1).toString());
    }

    @GetMapping("/indicadores") // "/api/dashboard/indicadores?mesReferencia=2026-04"
    public ResponseEntity<IndicadoresMes> indicadores(
            @RequestParam("mesReferencia") @MesReferencia String mesReferencia,
            @RequestParam(value = "ranking", defaultValue = "false") boolean ranking,
            HttpServletRequest request) {
        IndicadoresMes indicadores = indicadoresService.calcular(mesReferencia);
        boolean ocultarValores = !perfilResolver.possui(request, Permissao.VER_DADOS_FINANCEIROS);
        return ResponseEntity.ok(ocultarValores ? indicadores.semValoresFinanceiros() : indicadores);
    }
<<<<<<< HEAD

    /**
     * Totais dos ultimos meses ate mesReferencia, para o comparativo com o mes
     * anterior e a linha de tendencia dos cards.
     */
    @GetMapping("/indicadores/historico") // "/api/dashboard/indicadores/historico?mesReferencia=2026-06&meses=6"
    public ResponseEntity<List<HistoricoMes>> historico(
            @RequestParam("mesReferencia") @MesReferencia String mesReferencia,
            @RequestParam(value = "meses", defaultValue = "6")
            @Min(value = 2, message = "de 2 a 12") @Max(value = 12, message = "de 2 a 12") int meses,
            HttpServletRequest request) {
        List<HistoricoMes> historico = indicadoresService.historico(mesReferencia, meses);
        if (!perfilResolver.possui(request, Permissao.VER_DADOS_FINANCEIROS)) {
            historico = historico.stream().map(HistoricoMes::semValoresFinanceiros).toList();
        }
        return ResponseEntity.ok(historico);
    }
=======
>>>>>>> origin/develop
}
