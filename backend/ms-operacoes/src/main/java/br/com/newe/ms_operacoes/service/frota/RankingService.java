package br.com.newe.ms_operacoes.service.frota;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.newe.ms_operacoes.dto.frota.RankingMensalMotoristaDTO;
import br.com.newe.ms_operacoes.service.indicadores.IndicadoresMotorista;
import br.com.newe.ms_operacoes.service.indicadores.IndicadoresService;
import br.com.newe.ms_operacoes.service.frota.ViagemMaisLongaService.ViagemMaisLonga;

@Service
public class RankingService {
    private static final List<String> COLUNAS = List.of("motorista", "tipoVeiculo", "viagens", "disponibilidade", "utilizacao", "valorFretes", "custos", "rentabilidade", "rentabilidadeMediaViagem");
    private final IndicadoresService indicadoresService;
    private final ViagemMaisLongaService viagemMaisLongaService;

    public RankingService(IndicadoresService indicadoresService, ViagemMaisLongaService viagemMaisLongaService) {
        this.indicadoresService = indicadoresService;
        this.viagemMaisLongaService = viagemMaisLongaService;
    }

    public static boolean colunaValida(String coluna) { return COLUNAS.contains(coluna); }
    public static boolean direcaoValida(String direcao) { return direcao.equalsIgnoreCase("asc") || direcao.equalsIgnoreCase("desc"); }

    public List<RankingMensalMotoristaDTO> rankingMensal(String mes, String ordenarPor, String direcao, boolean operador) {
        List<IndicadoresMotorista> indicadores = indicadoresService.calcular(mes).motoristas();
        Map<UUID, ViagemMaisLonga> maisLonga = viagemMaisLongaService.porMotorista(mes, indicadores.stream().map(IndicadoresMotorista::motoristaId).toList());
        String colunaOrdenacao = operador && List.of("valorFretes", "custos", "rentabilidade", "rentabilidadeMediaViagem").contains(ordenarPor) ? "viagens" : ordenarPor;
        return indicadores.stream().map(i -> new RankingMensalMotoristaDTO(i.motoristaId(), i.nome(), tipoVeiculo(maisLonga.get(i.motoristaId())), i.numeroViagens(), i.disponibilidade(), i.utilizacao(), operador ? null : i.valorFrete(), operador ? null : i.custoTotal(), operador ? null : i.rentabilidade(), operador ? null : i.rentabilidadeMediaViagem())).sorted(comparador(colunaOrdenacao, direcao)).toList();
    }

    private static String tipoVeiculo(ViagemMaisLonga viagem) {
        return viagem != null ? viagem.tipoVeiculo() : null;
    }

    // Nulo (ex.: media sem viagem apuravel) fica no fim nas duas direcoes: inverter
    // o comparador inteiro com reversed() o levaria para o topo no "desc".
    static java.util.Comparator<RankingMensalMotoristaDTO> comparador(String coluna, String direcao) {
        boolean desc = "desc".equalsIgnoreCase(direcao);
        return switch (coluna) {
            case "motorista" -> porCampo(RankingMensalMotoristaDTO::motorista, String.CASE_INSENSITIVE_ORDER, desc);
            case "tipoVeiculo" -> porCampo(RankingMensalMotoristaDTO::tipoVeiculo, String.CASE_INSENSITIVE_ORDER, desc);
            case "viagens" -> porCampo(RankingMensalMotoristaDTO::viagens, java.util.Comparator.<Long>naturalOrder(), desc);
            case "disponibilidade" -> porCampo(RankingMensalMotoristaDTO::disponibilidade, java.util.Comparator.<java.math.BigDecimal>naturalOrder(), desc);
            case "utilizacao" -> porCampo(RankingMensalMotoristaDTO::utilizacao, java.util.Comparator.<java.math.BigDecimal>naturalOrder(), desc);
            case "valorFretes" -> porCampo(RankingMensalMotoristaDTO::valorFretes, java.util.Comparator.<java.math.BigDecimal>naturalOrder(), desc);
            case "custos" -> porCampo(RankingMensalMotoristaDTO::custos, java.util.Comparator.<java.math.BigDecimal>naturalOrder(), desc);
            case "rentabilidade" -> porCampo(RankingMensalMotoristaDTO::rentabilidade, java.util.Comparator.<java.math.BigDecimal>naturalOrder(), desc);
            case "rentabilidadeMediaViagem" -> porCampo(RankingMensalMotoristaDTO::rentabilidadeMediaViagem, java.util.Comparator.<java.math.BigDecimal>naturalOrder(), desc);
            default -> throw new IllegalArgumentException("Coluna inválida");
        };
    }

    private static <T> java.util.Comparator<RankingMensalMotoristaDTO> porCampo(
            java.util.function.Function<RankingMensalMotoristaDTO, T> campo, java.util.Comparator<T> ordem, boolean desc) {
        return java.util.Comparator.comparing(campo, java.util.Comparator.nullsLast(desc ? ordem.reversed() : ordem));
    }
}
