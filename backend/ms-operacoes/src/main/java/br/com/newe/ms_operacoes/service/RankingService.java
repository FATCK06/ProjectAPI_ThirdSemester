package br.com.newe.ms_operacoes.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import br.com.newe.ms_operacoes.client.FrotaClient;
import br.com.newe.ms_operacoes.dto.RankingMensalMotoristaDTO;
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

    public List<RankingMensalMotoristaDTO> rankingMensal(String mes, String ordenarPor, String direcao) {
        List<IndicadoresMotorista> indicadores = indicadoresService.calcular(mes).motoristas();
        Map<UUID, String> tipos = tiposVeiculoMaisLongo(mes, indicadores.stream().map(IndicadoresMotorista::motoristaId).toList());
        return indicadores.stream().map(i -> new RankingMensalMotoristaDTO(i.motoristaId(), i.nome(), tipos.get(i.motoristaId()), i.numeroViagens(), i.disponibilidade(), i.utilizacao(), i.valorFrete(), i.custoTotal(), i.rentabilidade(), i.rentabilidadeMediaViagem())).sorted(comparador(ordenarPor, direcao)).toList();
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

    private static java.util.Comparator<RankingMensalMotoristaDTO> comparador(String coluna, String direcao) {
        java.util.Comparator<RankingMensalMotoristaDTO> c = switch (coluna) {
            case "motorista" -> java.util.Comparator.comparing(RankingMensalMotoristaDTO::motorista, java.util.Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "tipoVeiculo" -> java.util.Comparator.comparing(RankingMensalMotoristaDTO::tipoVeiculo, java.util.Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "viagens" -> java.util.Comparator.comparingLong(RankingMensalMotoristaDTO::viagens);
            case "disponibilidade" -> java.util.Comparator.comparing(RankingMensalMotoristaDTO::disponibilidade);
            case "utilizacao" -> java.util.Comparator.comparing(RankingMensalMotoristaDTO::utilizacao);
            case "valorFretes" -> java.util.Comparator.comparing(RankingMensalMotoristaDTO::valorFretes);
            case "custos" -> java.util.Comparator.comparing(RankingMensalMotoristaDTO::custos);
            case "rentabilidade" -> java.util.Comparator.comparing(RankingMensalMotoristaDTO::rentabilidade);
            case "rentabilidadeMediaViagem" -> java.util.Comparator.comparing(RankingMensalMotoristaDTO::rentabilidadeMediaViagem);
            default -> throw new IllegalArgumentException("Coluna inválida");
        };
        return "desc".equalsIgnoreCase(direcao) ? c.reversed() : c;
    }
}
