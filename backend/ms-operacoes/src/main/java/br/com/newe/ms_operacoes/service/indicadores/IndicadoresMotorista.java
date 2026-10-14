package br.com.newe.ms_operacoes.service.indicadores;

import java.math.BigDecimal;
import java.util.UUID;

/** Uma linha do ranking gerencial: um motorista em um mes. Percentuais de 0 a 100. */
public record IndicadoresMotorista(
        UUID motoristaId,
        String nome,
        long numeroViagens,
        /** Viagens com receita e custo conhecidos; base da rentabilidade media. */
        long viagensApuradas,
        long diasOperacao,
        long diasDisponiveis,
        BigDecimal utilizacao,
        BigDecimal disponibilidade,
        BigDecimal valorFrete,
        BigDecimal custoTotal,
        BigDecimal rentabilidade,
        BigDecimal rentabilidadeMediaViagem,
        BigDecimal margem
) {
}
