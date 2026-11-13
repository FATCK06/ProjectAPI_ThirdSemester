package br.com.newe.ms_operacoes.service.indicadores;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Um mes de /api/dashboard/indicadores/serie. Ao contrario de {@link HistoricoMes},
 * mes sem viagem nao vira zero: vem com semDados = true e sem totais.
 *
 * A variacao e sempre contra o mes imediatamente anterior do calendario
 * (mesAnterior), selecionado ou nao. Indicador sem dado em um dos dois meses
 * some de variacoes: nunca e tratado como zero.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SerieMes(
        String mesReferencia,
        boolean semDados,
        String mensagem,
        Totais totais,
        String mesAnterior,
        boolean mesAnteriorSemDados,
        Variacoes variacoes
) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Totais(
            long numeroViagens,
            BigDecimal utilizacaoMedia,
            BigDecimal valorFrete,
            BigDecimal custoTotal,
            BigDecimal rentabilidade
    ) {
        /** Copia sem os campos financeiros (viram null e somem do JSON). */
        public Totais semValoresFinanceiros() {
            return new Totais(numeroViagens, utilizacaoMedia, null, null, null);
        }
    }

    /**
     * diferenca = atual - anterior (utilizacao em pontos percentuais).
     * percentual = (atual - anterior) / |anterior| x 100, 2 casas HALF_UP;
     * ausente quando o anterior e zero.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Variacao(BigDecimal diferenca, BigDecimal percentual) {

        private static final BigDecimal CEM = BigDecimal.valueOf(100);

        /** Nulo quando falta o valor de um dos meses. */
        static Variacao entre(BigDecimal atual, BigDecimal anterior) {
            if (atual == null || anterior == null) {
                return null;
            }
            BigDecimal diferenca = atual.subtract(anterior);
            BigDecimal percentual = anterior.signum() == 0
                    ? null
                    : diferenca.multiply(CEM).divide(anterior.abs(), 2, RoundingMode.HALF_UP);
            return new Variacao(diferenca, percentual);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Variacoes(
            Variacao numeroViagens,
            Variacao utilizacaoMedia,
            Variacao valorFrete,
            Variacao custoTotal,
            Variacao rentabilidade
    ) {
        static final Variacoes VAZIAS = new Variacoes(null, null, null, null, null);

        /** Vazias (objeto {} no JSON) quando um dos meses nao tem dados. */
        static Variacoes entre(Totais atual, Totais anterior) {
            if (atual == null || anterior == null) {
                return VAZIAS;
            }
            return new Variacoes(
                    Variacao.entre(BigDecimal.valueOf(atual.numeroViagens()),
                            BigDecimal.valueOf(anterior.numeroViagens())),
                    Variacao.entre(atual.utilizacaoMedia(), anterior.utilizacaoMedia()),
                    Variacao.entre(atual.valorFrete(), anterior.valorFrete()),
                    Variacao.entre(atual.custoTotal(), anterior.custoTotal()),
                    Variacao.entre(atual.rentabilidade(), anterior.rentabilidade()));
        }

        /** Copia sem as variacoes financeiras. */
        public Variacoes semValoresFinanceiros() {
            return new Variacoes(numeroViagens, utilizacaoMedia, null, null, null);
        }
    }

    /** Copia sem valores financeiros nos totais e nas variacoes. */
    public SerieMes semValoresFinanceiros() {
        return new SerieMes(mesReferencia, semDados, mensagem,
                totais == null ? null : totais.semValoresFinanceiros(),
                mesAnterior, mesAnteriorSemDados,
                variacoes == null ? null : variacoes.semValoresFinanceiros());
    }
}
