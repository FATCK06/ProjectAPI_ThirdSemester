package br.com.newe.ms_operacoes.service.indicadores;

import java.math.BigDecimal;

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
}