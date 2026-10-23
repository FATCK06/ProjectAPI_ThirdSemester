package br.com.newe.ms_operacoes.service.indicadores;

import java.math.BigDecimal;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Resposta de /api/dashboard/indicadores: totais do mes (cards e grafico de
 * valores) mais o ranking por motorista, ordenado por rentabilidade.
 */
public record IndicadoresMes(
        String mesReferencia,
        long diasNoMes,
        long diasDisponiveis,
        Totais totais,
        List<IndicadoresMotorista> motoristas
) {

    /**
     * diasOperacao: soma dos dias distintos com viagem de cada motorista
     * (motorista-dia). utilizacaoMedia: diasOperacao / (dias disponiveis x motoristas).
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Totais(
            long motoristas,
            long numeroViagens,
            long diasOperacao,
            long viagensApuradas,
            BigDecimal utilizacaoMedia,
            BigDecimal valorFrete,
            BigDecimal custoTotal,
            BigDecimal rentabilidade,
            BigDecimal rentabilidadeMediaViagem,
            BigDecimal margem
    ) {
        /** Copia sem os campos financeiros (viram null e somem do JSON). */
        public Totais semValoresFinanceiros() {
            return new Totais(motoristas, numeroViagens, diasOperacao, viagensApuradas, utilizacaoMedia,
                    null, null, null, null, null);
        }
    }

    /** Copia sem valores financeiros, nos totais e em cada motorista. */
    public IndicadoresMes semValoresFinanceiros() {
        return new IndicadoresMes(mesReferencia, diasNoMes, diasDisponiveis,
                totais.semValoresFinanceiros(),
                motoristas.stream().map(IndicadoresMotorista::semValoresFinanceiros).toList());
    }
}