package br.com.newe.ms_operacoes.service.indicadores;

import java.math.BigDecimal;
import java.util.List;

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

    /** utilizacaoMedia: dias em operacao somados / (dias disponiveis x motoristas). */
    public record Totais(
            long motoristas,
            long numeroViagens,
            BigDecimal utilizacaoMedia,
            BigDecimal valorFrete,
            BigDecimal custoTotal,
            BigDecimal rentabilidade,
            BigDecimal rentabilidadeMediaViagem,
            BigDecimal margem
    ) {
    }
}
