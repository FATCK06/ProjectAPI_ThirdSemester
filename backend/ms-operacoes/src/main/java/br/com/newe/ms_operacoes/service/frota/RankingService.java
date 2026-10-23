package br.com.newe.ms_operacoes.service.frota;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import br.com.newe.ms_operacoes.dto.frota.RankingMensalMotoristaDTO;
import br.com.newe.ms_operacoes.service.indicadores.IndicadoresMotorista;
import br.com.newe.ms_operacoes.service.indicadores.IndicadoresService;
import br.com.newe.ms_operacoes.service.frota.ViagemMaisLongaService.ViagemMaisLonga;

@Service
public class RankingService {
    private static final List<String> COLUNAS_FINANCEIRAS = List.of("valorFrete", "custoTotal", "rentabilidade", "rentabilidadeMediaViagem");
    private static final List<String> COLUNAS = List.of("nome", "tipoVeiculo", "numeroViagens", "disponibilidade", "utilizacao", "valorFrete", "custoTotal", "rentabilidade", "rentabilidadeMediaViagem");
    private final IndicadoresService indicadoresService;
    private final ViagemMaisLongaService viagemMaisLongaService;

    public RankingService(IndicadoresService indicadoresService, ViagemMaisLongaService viagemMaisLongaService) {
        this.indicadoresService = indicadoresService;
        this.viagemMaisLongaService = viagemMaisLongaService;
    }

    public static boolean colunaValida(String coluna) { return COLUNAS.contains(coluna); }

    public List<RankingMensalMotoristaDTO> rankingMensal(String mes, String ordenarPor, String direcao, boolean operador) {
        List<IndicadoresMotorista> indicadores = indicadoresService.calcular(mes).motoristas();
        Map<UUID, ViagemMaisLonga> maisLonga = viagemMaisLongaService.porMotorista(mes, indicadores.stream().map(IndicadoresMotorista::motoristaId).toList());
        // Operador nao recebe os valores, entao ordenar por eles vazaria a ordem: cai em viagens.
        String colunaOrdenacao = operador && COLUNAS_FINANCEIRAS.contains(ordenarPor) ? "numeroViagens" : ordenarPor;
        return indicadores.stream()
                .map(i -> RankingMensalMotoristaDTO.de(operador ? i.semValoresFinanceiros() : i, tipoVeiculo(maisLonga.get(i.motoristaId()))))
                .sorted(comparador(colunaOrdenacao, direcao))
                .toList();
    }

    private static String tipoVeiculo(ViagemMaisLonga viagem) {
        return viagem != null ? viagem.tipoVeiculo() : null;
    }

    // Nulo (ex.: media sem viagem apuravel) fica no fim nas duas direcoes: inverter
    // o comparador inteiro com reversed() o levaria para o topo no "desc".
    static Comparator<RankingMensalMotoristaDTO> comparador(String coluna, String direcao) {
        boolean desc = "desc".equalsIgnoreCase(direcao);
        return switch (coluna) {
            case "nome" -> porCampo(RankingMensalMotoristaDTO::nome, String.CASE_INSENSITIVE_ORDER, desc);
            case "tipoVeiculo" -> porCampo(RankingMensalMotoristaDTO::tipoVeiculo, String.CASE_INSENSITIVE_ORDER, desc);
            case "numeroViagens" -> porCampo(RankingMensalMotoristaDTO::numeroViagens, Comparator.<Long>naturalOrder(), desc);
            case "disponibilidade" -> porValor(RankingMensalMotoristaDTO::disponibilidade, desc);
            case "utilizacao" -> porValor(RankingMensalMotoristaDTO::utilizacao, desc);
            case "valorFrete" -> porValor(RankingMensalMotoristaDTO::valorFrete, desc);
            case "custoTotal" -> porValor(RankingMensalMotoristaDTO::custoTotal, desc);
            case "rentabilidade" -> porValor(RankingMensalMotoristaDTO::rentabilidade, desc);
            case "rentabilidadeMediaViagem" -> porValor(RankingMensalMotoristaDTO::rentabilidadeMediaViagem, desc);
            default -> throw new IllegalArgumentException("Coluna inválida");
        };
    }

    private static Comparator<RankingMensalMotoristaDTO> porValor(Function<RankingMensalMotoristaDTO, BigDecimal> campo, boolean desc) {
        return porCampo(campo, Comparator.<BigDecimal>naturalOrder(), desc);
    }

    private static <T> Comparator<RankingMensalMotoristaDTO> porCampo(Function<RankingMensalMotoristaDTO, T> campo, Comparator<T> ordem, boolean desc) {
        return Comparator.comparing(campo, Comparator.nullsLast(desc ? ordem.reversed() : ordem));
    }
}
