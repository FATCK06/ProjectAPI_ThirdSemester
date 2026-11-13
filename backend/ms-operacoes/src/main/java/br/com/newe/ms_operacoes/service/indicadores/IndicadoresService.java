package br.com.newe.ms_operacoes.service.indicadores;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import br.com.newe.ms_operacoes.client.FrotaClient;
import br.com.newe.ms_operacoes.dto.MotoristaResumo;
import br.com.newe.ms_operacoes.erro.DadoInvalidoException;
import br.com.newe.ms_operacoes.repository.ViagemRepository;
import br.com.newe.ms_operacoes.repository.ViagemRepository.SomaMotoristaView;

/**
 * Ranking gerencial do mes. Calculado na hora, sem tabela propria: assim o
 * numero nunca fica defasado depois de uma importacao nova.
 */
@Service
public class IndicadoresService {

    private static final int MAXIMO_MESES_SERIE = 12;
    private static final DateTimeFormatter MES_NA_MENSAGEM = DateTimeFormatter.ofPattern("MM/yyyy");

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

        return new IndicadoresMes(mesReferencia, diasNoMes, diasDisponiveis, totais(motoristas, diasDisponiveis),
                motoristas);
    }

    /**
     * Totais dos {@code quantidade} meses terminados em mesReferencia, do mais
     * antigo ao mais novo. Uma consulta so e sem ms-frota: os totais nao usam nome.
     *
     * @param mesReferencia "yyyy-MM", ultimo mes da serie
     */
    public List<HistoricoMes> historico(String mesReferencia, int quantidade) {
        YearMonth ultimo = YearMonth.parse(mesReferencia);
        List<YearMonth> meses = new ArrayList<>(quantidade);
        for (int i = quantidade - 1; i >= 0; i--) {
            meses.add(ultimo.minusMonths(i));
        }

        Map<String, List<ViagemRepository.SomaMotoristaMesView>> somasPorMes = viagemRepository
                .somasPorMotoristaNosMeses(meses.stream().map(YearMonth::toString).toList()).stream()
                .collect(Collectors.groupingBy(ViagemRepository.SomaMotoristaMesView::getMesReferencia));

        List<HistoricoMes> historico = new ArrayList<>(quantidade);
        for (YearMonth mes : meses) {
            long diasNoMes = mes.lengthOfMonth();
            long diasDisponiveis = diasDisponiveis(mes);
            List<IndicadoresMotorista> motoristas = somasPorMes.getOrDefault(mes.toString(), List.of()).stream()
                    .map(soma -> indicadoresDe(soma, null, diasDisponiveis, diasNoMes))
                    .toList();
            historico.add(new HistoricoMes(mes.toString(), totais(motoristas, diasDisponiveis)));
        }
        return historico;
    }

    /** Meses com viagem, em AAAA-MM crescente. A coluna aceita nulo, que nao e um mes. */
    public List<String> mesesImportados() {
        return viagemRepository.buscarMesesImportados().stream().filter(Objects::nonNull).toList();
    }

    /**
     * Totais gerais de cada mes pedido; mes sem viagem fica "sem dados", nunca
     * zerado. Uma consulta so e sem ms-frota: os totais nao usam nome.
     *
     * @param meses "yyyy-MM"; repetidos contam uma vez e a resposta sai em ordem crescente
     */
    public List<SerieMes> serie(List<String> meses) {
        TreeSet<YearMonth> pedidos = new TreeSet<>();
        if (meses != null) {
            meses.forEach(mes -> pedidos.add(YearMonth.parse(mes)));
        }
        if (pedidos.size() < 2) {
            throw new DadoInvalidoException("meses", "Selecione ao menos dois meses para comparar.");
        }
        if (pedidos.size() > MAXIMO_MESES_SERIE) {
            throw new DadoInvalidoException("meses", "de 2 a 12");
        }

        // Cada mes pedido mais o anterior dele, para a variacao: ainda uma consulta so
        TreeSet<YearMonth> consultados = new TreeSet<>(pedidos);
        pedidos.forEach(mes -> consultados.add(mes.minusMonths(1)));

        Map<String, List<ViagemRepository.SomaMotoristaMesView>> somasPorMes = viagemRepository
                .somasPorMotoristaNosMeses(consultados.stream().map(YearMonth::toString).toList()).stream()
                .collect(Collectors.groupingBy(ViagemRepository.SomaMotoristaMesView::getMesReferencia));

        List<SerieMes> serie = new ArrayList<>(pedidos.size());
        for (YearMonth mes : pedidos) {
            YearMonth anterior = mes.minusMonths(1);
            SerieMes.Totais totais = totaisDaSerie(somasPorMes.get(mes.toString()), mes);
            SerieMes.Totais totaisAnterior = totaisDaSerie(somasPorMes.get(anterior.toString()), anterior);
            serie.add(new SerieMes(
                    mes.toString(),
                    totais == null,
                    totais == null ? "Sem dados para " + mes.format(MES_NA_MENSAGEM) + "." : null,
                    totais,
                    anterior.toString(),
                    totaisAnterior == null,
                    SerieMes.Variacoes.entre(totais, totaisAnterior)));
        }
        return serie;
    }

    /** Nulo quando o mes nao tem viagem: "sem dados", nunca zero. */
    private static SerieMes.Totais totaisDaSerie(List<ViagemRepository.SomaMotoristaMesView> somas, YearMonth mes) {
        if (somas == null || somas.isEmpty()) {
            return null;
        }
        long diasNoMes = mes.lengthOfMonth();
        long diasDisponiveis = diasDisponiveis(mes);
        IndicadoresMes.Totais totais = totais(
                somas.stream().map(soma -> indicadoresDe(soma, null, diasDisponiveis, diasNoMes)).toList(),
                diasDisponiveis);
        return new SerieMes.Totais(totais.numeroViagens(), totais.utilizacaoMedia(), totais.valorFrete(),
                totais.custoTotal(), totais.rentabilidade());
    }

    /**
     * PENDENTE (cliente): nada no sistema registra ferias, afastamento ou
     * manutencao, entao por ora todo dia do mes conta como disponivel - a
     * disponibilidade sai 100% para todos. Dias uteis foi descartado: quem roda
     * no sabado passaria de 100% de utilizacao.
     */
    public static long diasDisponiveis(YearMonth mes) {
        return mes.lengthOfMonth();
    }

    static IndicadoresMotorista indicadoresDe(SomaMotoristaView soma, String nome, long diasDisponiveis,
            long diasNoMes) {
        long viagens = soma.getNumeroViagens();
        long apuradas = soma.getViagensApuradas() == null ? 0 : soma.getViagensApuradas();
        long diasOperacao = soma.getDiasOperacao();
        BigDecimal frete = CalculoIndicadores.zeroSeNulo(soma.getValorFrete());
        BigDecimal custo = CalculoIndicadores.zeroSeNulo(soma.getCustoTotal());
        BigDecimal rentabilidade = CalculoIndicadores.rentabilidade(frete, custo);

        return new IndicadoresMotorista(
                soma.getMotoristaId(),
                nome,
                viagens,
                apuradas,
                diasOperacao,
                diasDisponiveis,
                CalculoIndicadores.utilizacao(diasOperacao, diasDisponiveis),
                CalculoIndicadores.disponibilidade(diasDisponiveis, diasNoMes),
                frete,
                custo,
                rentabilidade,
                // So as viagens que entraram na rentabilidade: dividir pelas demais
                // puxaria a media para baixo sem motivo.
                CalculoIndicadores.rentabilidadeMediaPorViagem(rentabilidade, apuradas),
                CalculoIndicadores.margem(rentabilidade, frete));
    }

    static IndicadoresMes.Totais totais(List<IndicadoresMotorista> motoristas, long diasDisponiveis) {
        long viagens = 0;
        long apuradas = 0;
        long diasOperacao = 0;
        BigDecimal frete = BigDecimal.ZERO;
        BigDecimal custo = BigDecimal.ZERO;

        for (IndicadoresMotorista m : motoristas) {
            viagens += m.numeroViagens();
            apuradas += m.viagensApuradas();
            diasOperacao += m.diasOperacao();
            frete = frete.add(m.valorFrete());
            custo = custo.add(m.custoTotal());
        }
        BigDecimal rentabilidade = CalculoIndicadores.rentabilidade(frete, custo);

        return new IndicadoresMes.Totais(
                motoristas.size(),
                viagens,
                diasOperacao,
                apuradas,
                CalculoIndicadores.utilizacao(diasOperacao, diasDisponiveis * motoristas.size()),
                frete,
                custo,
                rentabilidade,
                CalculoIndicadores.rentabilidadeMediaPorViagem(rentabilidade, apuradas),
                CalculoIndicadores.margem(rentabilidade, frete));
    }

    /** @param mesReferencia "yyyy-MM" */
    public List<IndicadoresPorModelo> calcularPorModelo(String mesReferencia) {
        YearMonth mes = YearMonth.parse(mesReferencia);
        long diasDisponiveis = diasDisponiveis(mes);

        List<ViagemRepository.SomaVeiculoView> somas = viagemRepository.somasPorVeiculo(mesReferencia);

        // Modelo pertence ao ms-frota: uma chamada em lote para o mes inteiro.
        List<UUID> veiculoIds = somas.stream().map(ViagemRepository.SomaVeiculoView::getVeiculoId).toList();
        Map<UUID, String> tiposVeiculo = frotaClient.buscarTiposVeiculo(veiculoIds);

        // Agrupa as somas por veiculo em somas por modelo, ja que varios veiculos
        // podem compartilhar o mesmo modelo.
        Map<String, List<ViagemRepository.SomaVeiculoView>> porModelo = somas.stream()
                .collect(Collectors.groupingBy(s -> tiposVeiculo.getOrDefault(s.getVeiculoId(), "Não informado")));

        List<IndicadoresPorModelo> resultado = new ArrayList<>();
        for (Map.Entry<String, List<ViagemRepository.SomaVeiculoView>> entrada : porModelo.entrySet()) {
            long viagens = 0;
            long diasOperacao = 0;
            BigDecimal frete = BigDecimal.ZERO;
            BigDecimal custo = BigDecimal.ZERO;

            for (ViagemRepository.SomaVeiculoView soma : entrada.getValue()) {
                viagens += soma.getNumeroViagens();
                diasOperacao += soma.getDiasOperacao();
                frete = frete.add(CalculoIndicadores.zeroSeNulo(soma.getValorFrete()));
                custo = custo.add(CalculoIndicadores.zeroSeNulo(soma.getCustoTotal()));
            }

            BigDecimal rentabilidade = CalculoIndicadores.rentabilidade(frete, custo);
            long numeroVeiculos = entrada.getValue().size();

            resultado.add(new IndicadoresPorModelo(
                    entrada.getKey(),
                    numeroVeiculos,
                    viagens,
                    diasOperacao,
                    CalculoIndicadores.utilizacao(diasOperacao, diasDisponiveis * numeroVeiculos),
                    frete,
                    custo,
                    rentabilidade,
                    CalculoIndicadores.margem(rentabilidade, frete)));
        }

        resultado.sort(Comparator.comparing(IndicadoresPorModelo::rentabilidade).reversed());
        return resultado;
    }
}
