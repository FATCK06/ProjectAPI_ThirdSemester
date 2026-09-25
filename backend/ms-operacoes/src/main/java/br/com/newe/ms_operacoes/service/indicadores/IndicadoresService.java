package br.com.newe.ms_operacoes.service.indicadores;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import br.com.newe.ms_operacoes.client.FrotaClient;
import br.com.newe.ms_operacoes.dto.MotoristaResumo;
import br.com.newe.ms_operacoes.repository.ViagemRepository;
import br.com.newe.ms_operacoes.repository.ViagemRepository.SomaMotoristaView;

/**
 * Ranking gerencial do mes. Calculado na hora, sem tabela propria: assim o
 * numero nunca fica defasado depois de uma importacao nova.
 */
@Service
public class IndicadoresService {

    private final ViagemRepository viagemRepository;
    private final FrotaClient frotaClient;

    public IndicadoresService(ViagemRepository viagemRepository, FrotaClient frotaClient) {
        this.viagemRepository = viagemRepository;
        this.frotaClient = frotaClient;
    }

    /** @param mesReferencia "yyyy-MM" */
    public IndicadoresMes calcular(String mesReferencia) {
        YearMonth mes = YearMonth.parse(mesReferencia);
        long diasNoMes = mes.lengthOfMonth();
        long diasDisponiveis = diasDisponiveis(mes);

        List<SomaMotoristaView> somas = viagemRepository.somasPorMotorista(mesReferencia);

        // Nome pertence ao ms-frota: uma chamada em lote para o mes inteiro.
        List<UUID> ids = somas.stream().map(SomaMotoristaView::getMotoristaId).toList();
        Map<UUID, MotoristaResumo> cadastro = frotaClient.buscarMotoristas(ids).stream()
                .collect(Collectors.toMap(MotoristaResumo::id, Function.identity()));

        List<IndicadoresMotorista> motoristas = new ArrayList<>(somas.size());
        for (SomaMotoristaView soma : somas) {
            MotoristaResumo resumo = cadastro.get(soma.getMotoristaId());
            motoristas.add(indicadoresDe(soma, resumo != null ? resumo.nome() : null, diasDisponiveis, diasNoMes));
        }
        motoristas.sort(Comparator.comparing(IndicadoresMotorista::rentabilidade).reversed());

        return new IndicadoresMes(mesReferencia, diasNoMes, diasDisponiveis, totais(motoristas, diasDisponiveis), motoristas);
    }

    /**
     * PENDENTE (cliente): nada no sistema registra ferias, afastamento ou
     * manutencao, entao por ora todo dia do mes conta como disponivel - a
     * disponibilidade sai 100% para todos. Dias uteis foi descartado: quem roda
     * no sabado passaria de 100% de utilizacao.
     */
    static long diasDisponiveis(YearMonth mes) {
        return mes.lengthOfMonth();
    }

    static IndicadoresMotorista indicadoresDe(SomaMotoristaView soma, String nome, long diasDisponiveis, long diasNoMes) {
        long viagens = soma.getNumeroViagens();
        long diasOperacao = soma.getDiasOperacao();
        BigDecimal frete = CalculoIndicadores.zeroSeNulo(soma.getValorFrete());
        BigDecimal custo = CalculoIndicadores.zeroSeNulo(soma.getCustoTotal());
        BigDecimal rentabilidade = CalculoIndicadores.rentabilidade(frete, custo);

        return new IndicadoresMotorista(
                soma.getMotoristaId(),
                nome,
                viagens,
                diasOperacao,
                diasDisponiveis,
                CalculoIndicadores.utilizacao(diasOperacao, diasDisponiveis),
                CalculoIndicadores.disponibilidade(diasDisponiveis, diasNoMes),
                frete,
                custo,
                rentabilidade,
                CalculoIndicadores.rentabilidadeMediaPorViagem(rentabilidade, viagens),
                CalculoIndicadores.margem(rentabilidade, frete));
    }

    static IndicadoresMes.Totais totais(List<IndicadoresMotorista> motoristas, long diasDisponiveis) {
        long viagens = 0;
        long diasOperacao = 0;
        BigDecimal frete = BigDecimal.ZERO;
        BigDecimal custo = BigDecimal.ZERO;

        for (IndicadoresMotorista m : motoristas) {
            viagens += m.numeroViagens();
            diasOperacao += m.diasOperacao();
            frete = frete.add(m.valorFrete());
            custo = custo.add(m.custoTotal());
        }
        BigDecimal rentabilidade = CalculoIndicadores.rentabilidade(frete, custo);

        return new IndicadoresMes.Totais(
                motoristas.size(),
                viagens,
                CalculoIndicadores.utilizacao(diasOperacao, diasDisponiveis * motoristas.size()),
                frete,
                custo,
                rentabilidade,
                CalculoIndicadores.rentabilidadeMediaPorViagem(rentabilidade, viagens),
                CalculoIndicadores.margem(rentabilidade, frete));
    }
}
