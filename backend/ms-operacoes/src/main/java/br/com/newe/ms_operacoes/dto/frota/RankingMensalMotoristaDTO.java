package br.com.newe.ms_operacoes.dto.frota;

import java.math.BigDecimal;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
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
