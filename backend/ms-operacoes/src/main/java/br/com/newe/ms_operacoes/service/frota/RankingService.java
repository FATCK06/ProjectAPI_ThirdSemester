package br.com.newe.ms_operacoes.service.frota;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import br.com.newe.ms_operacoes.client.FrotaClient;
import br.com.newe.ms_operacoes.dto.frota.RankingMensalMotoristaDTO;
import br.com.newe.ms_operacoes.repository.ViagemRepository;
import br.com.newe.ms_operacoes.service.indicadores.IndicadoresMotorista;
import br.com.newe.ms_operacoes.service.indicadores.IndicadoresService;

@Service
public class RankingService {
    private static final List<String> COLUNAS = List.of("motorista", "tipoVeiculo", "viagens", "disponibilidade", "utilizacao", "valorFretes", "custos", "rentabilidade", "rentabilidadeMediaViagem");
    private final ViagemRepository repository;
    private final FrotaClient frotaClient;
    private final IndicadoresService indicadoresService;

    public RankingService(ViagemRepository repository, FrotaClient frotaClient, IndicadoresService indicadoresService) {
        this.repository = repository;
        this.frotaClient = frotaClient;
        this.indicadoresService = indicadoresService;
    }

    public static boolean colunaValida(String coluna) { return COLUNAS.contains(coluna); }
    public static boolean direcaoValida(String direcao) { return direcao.equalsIgnoreCase("asc") || direcao.equalsIgnoreCase("desc"); }

    public List<RankingMensalMotoristaDTO> rankingMensal(String mes, String ordenarPor, String direcao, boolean operador) {
        List<IndicadoresMotorista> indicadores = indicadoresService.calcular(mes).motoristas();
        Map<UUID, String> tipos = tiposVeiculoMaisLongo(mes, indicadores.stream().map(IndicadoresMotorista::motoristaId).toList());
        String colunaOrdenacao = operador && List.of("valorFretes", "custos", "rentabilidade", "rentabilidadeMediaViagem").contains(ordenarPor) ? "viagens" : ordenarPor;
        return indicadores.stream().map(i -> new RankingMensalMotoristaDTO(i.motoristaId(), i.nome(), tipos.get(i.motoristaId()), i.numeroViagens(), i.disponibilidade(), i.utilizacao(), operador ? null : i.valorFrete(), operador ? null : i.custoTotal(), operador ? null : i.rentabilidade(), operador ? null : i.rentabilidadeMediaViagem())).sorted(comparador(colunaOrdenacao, direcao)).toList();
    }

    private Map<UUID, String> tiposVeiculoMaisLongo(String mes, List<UUID> motoristaIds) {
        if (motoristaIds.isEmpty()) return Map.of();
        Map<UUID, ViagemRepository.ViagemKmView> maisLonga = new HashMap<>();
        Map<UUID, Integer> distancias = new HashMap<>();
        for (ViagemRepository.ViagemKmView viagem : repository.kmViagensPorMotoristas(mes, motoristaIds)) {
            Integer distancia = viagem.getKmSaida() != null && viagem.getKmChegada() != null ? viagem.getKmChegada() - viagem.getKmSaida() : null;
            Integer atual = distancias.get(viagem.getMotoristaId());
            if (!maisLonga.containsKey(viagem.getMotoristaId()) || (distancia != null && (atual == null || distancia > atual))) {
                maisLonga.put(viagem.getMotoristaId(), viagem);
                distancias.put(viagem.getMotoristaId(), distancia);
            }
        }
        List<UUID> veiculoIds = maisLonga.values().stream().map(ViagemRepository.ViagemKmView::getVeiculoId).distinct().toList();
        Map<UUID, String> tipos = frotaClient.buscarTiposVeiculo(veiculoIds);
        return maisLonga.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> tipos.getOrDefault(e.getValue().getVeiculoId(), "Não informado")));
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
