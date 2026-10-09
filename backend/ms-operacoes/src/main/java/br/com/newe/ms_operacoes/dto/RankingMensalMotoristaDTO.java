package br.com.newe.ms_operacoes.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record RankingMensalMotoristaDTO(
        UUID motoristaId,
        String motorista,
        String tipoVeiculo,
        long viagens,
        BigDecimal disponibilidade,
        BigDecimal utilizacao,
        BigDecimal valorFretes,
        BigDecimal custos,
        BigDecimal rentabilidade,
        BigDecimal rentabilidadeMediaViagem) {
}
