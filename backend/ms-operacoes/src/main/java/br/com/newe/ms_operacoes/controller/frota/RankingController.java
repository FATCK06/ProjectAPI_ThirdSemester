package br.com.newe.ms_operacoes.controller.frota;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.newe.ms_operacoes.controller.MesReferencia;
import br.com.newe.ms_operacoes.dto.frota.RankingMensalMotoristaDTO;
import br.com.newe.ms_operacoes.erro.DadoInvalidoException;
import br.com.newe.ms_operacoes.security.PerfilResolver;
import br.com.newe.ms_operacoes.security.Permissao;
import br.com.newe.ms_operacoes.service.exportacao.CsvExportacao;
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

    @GetMapping(value = "/mensal/csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportarMensalCsv(
            @RequestParam("mesReferencia") @MesReferencia String mesReferencia,
            @RequestParam(value = "ordenarPor", defaultValue = "rentabilidade") String ordenarPor,
            @RequestParam(value = "direcao", defaultValue = "desc")
            @Pattern(regexp = "(?i)asc|desc", message = "use asc ou desc") String direcao,
            HttpServletRequest request) throws IOException {
        if (!RankingService.colunaValida(ordenarPor)) {
            throw new DadoInvalidoException("ordenarPor", "coluna inválida");
        }
        boolean mostrarValoresFinanceiros = perfilResolver.podeVerDadosFinanceiros(request);
        List<RankingMensalMotoristaDTO> ranking = rankingService.rankingMensal(
                mesReferencia, ordenarPor, direcao, !mostrarValoresFinanceiros);
        List<String> cabecalho = new ArrayList<>(List.of(
                "Motorista", "Tipo de veículo", "Número de viagens", "Disponibilidade", "Utilização"));
        if (mostrarValoresFinanceiros) {
            cabecalho.addAll(List.of("Valor dos fretes", "Custos", "Rentabilidade", "Rentabilidade média"));
        }
        List<? extends List<?>> linhas = ranking.stream()
                .map(linha -> linhaCsv(linha, !mostrarValoresFinanceiros)).toList();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("ranking_" + mesReferencia.replace('-', '_') + ".csv").build());
        return ResponseEntity.ok().headers(headers).body(CsvExportacao.gerar(cabecalho, linhas));
    }

    private static List<?> linhaCsv(RankingMensalMotoristaDTO linha, boolean ocultarValores) {
        List<Object> valores = new ArrayList<>(List.of(
                linha.nome(), linha.tipoVeiculo(), linha.numeroViagens(), valorOuVazio(linha.disponibilidade()),
                valorOuVazio(linha.utilizacao())));
        if (!ocultarValores) {
            valores.add(valorOuVazio(linha.valorFrete()));
            valores.add(valorOuVazio(linha.custoTotal()));
            valores.add(valorOuVazio(linha.rentabilidade()));
            valores.add(valorOuVazio(linha.rentabilidadeMediaViagem()));
        }
        return valores;
    }

    private static Object valorOuVazio(Object valor) {
        return valor == null ? "" : valor;
    }
}
