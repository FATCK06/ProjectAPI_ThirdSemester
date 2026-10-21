package br.com.newe.ms_operacoes.service.indicadores;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record IndicadoresPorModelo(
        String modelo,
        long numeroVeiculos,
        long numeroViagens,
        long diasOperacao,
        BigDecimal utilizacao,
        BigDecimal valorFrete,
        BigDecimal custoTotal,
        BigDecimal rentabilidade,
        BigDecimal margem) {

    /** Copia sem os campos financeiros (viram null e somem do JSON). */
    public IndicadoresPorModelo semValoresFinanceiros() {
        return new IndicadoresPorModelo(modelo, numeroVeiculos, numeroViagens, diasOperacao, utilizacao,
                null, null, null, null);
    }
}
