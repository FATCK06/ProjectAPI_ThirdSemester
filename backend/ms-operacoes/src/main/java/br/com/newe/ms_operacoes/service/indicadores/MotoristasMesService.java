package br.com.newe.ms_operacoes.service.indicadores;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.newe.ms_operacoes.client.FrotaClient;
import br.com.newe.ms_operacoes.dto.MotoristaMesDTO;
import br.com.newe.ms_operacoes.dto.MotoristaResumo;
import br.com.newe.ms_operacoes.repository.ViagemRepository;
import br.com.newe.ms_operacoes.repository.ViagemRepository.MotoristaMesView;

@Service
public class MotoristasMesService {

    /** Mantém a resposta utilizável quando o cadastro referenciado não foi encontrado. */
    private static final String NAO_INFORMADO = "Não informado";

    private final ViagemRepository viagemRepository;
    private final FrotaClient frotaClient;

    public MotoristasMesService(ViagemRepository viagemRepository, FrotaClient frotaClient) {
        this.viagemRepository = viagemRepository;
        this.frotaClient = frotaClient;
    }

    /**
     * Converte a página agregada de viagens em resposta de dashboard, incluindo
     * os dados de frota em lote para não criar dependência direta entre bancos.
     */
    public Page<MotoristaMesDTO> listar(String mesReferencia, Pageable pageable) {
        Page<MotoristaMesView> pagina = viagemRepository.motoristasPorMes(mesReferencia, pageable);
        if (pagina.isEmpty()) {
            // Preserva os metadados da página sem fazer chamadas HTTP desnecessárias.
            return new PageImpl<>(List.of(), pagina.getPageable(), pagina.getTotalElements());
        }

        // O enriquecimento considera apenas a página atual e consulta a frota em lote,
        // evitando uma chamada HTTP por motorista ou por veículo.
        List<UUID> motoristaIds = pagina.getContent().stream()
                .map(linha -> linha.getMotoristaId())
                .distinct()
                .toList();
        List<UUID> veiculoIds = pagina.getContent().stream()
                .map(linha -> linha.getVeiculoId())
                .distinct()
                .toList();

        Map<UUID, MotoristaResumo> motoristas = frotaClient.buscarMotoristas(motoristaIds).stream()
                .collect(Collectors.toMap(motorista -> motorista.id(), motorista -> motorista));
        Map<UUID, String> placas = frotaClient.buscarPlacas(veiculoIds);
        Map<UUID, String> tipos = frotaClient.buscarTiposVeiculo(veiculoIds);
        YearMonth mes = YearMonth.parse(mesReferencia);
        long diasNoMes = mes.lengthOfMonth();
        // Regra provisória compartilhada com os indicadores: todos os dias do mês
        // contam como disponíveis enquanto não houver registro de afastamentos.
        long diasDisponiveis = IndicadoresService.diasDisponiveis(mes);

        return pagina.map(linha -> {
            MotoristaResumo motorista = motoristas.get(linha.getMotoristaId());
            return new MotoristaMesDTO(
                    linha.getMotoristaId(),
                    linha.getVeiculoId(),
                    motorista != null && motorista.nome() != null ? motorista.nome() : NAO_INFORMADO,
                    placas.getOrDefault(linha.getVeiculoId(), NAO_INFORMADO),
                    tipos.getOrDefault(linha.getVeiculoId(), NAO_INFORMADO),
                    linha.getViagensNoMes(),
                    linha.getDiasEmOperacao(),
                    diasDisponiveis,
                    CalculoIndicadores.disponibilidade(diasDisponiveis, diasNoMes),
                    // Utilização mede os dias distintos em operação sobre o mês inteiro.
                    CalculoIndicadores.utilizacao(linha.getDiasEmOperacao(), diasNoMes),
                    // A consulta fornece a média mensal; AVG ignora valores nulos.
                    linha.getAvaliacaoMedia());
        });
    }
}
