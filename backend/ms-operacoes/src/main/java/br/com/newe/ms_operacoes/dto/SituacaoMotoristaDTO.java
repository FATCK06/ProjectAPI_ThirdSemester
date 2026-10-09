package br.com.newe.ms_operacoes.dto;

import java.math.BigDecimal;
import java.util.UUID;

import br.com.newe.ms_operacoes.models.enums.FaixaUtilizacao;
import br.com.newe.ms_operacoes.models.enums.SituacaoMotorista;

/**
 * Uma linha de GET /api/dashboard/motoristas/situacao. utilizacao e
 * faixaUtilizacao sao nulas, e aviso preenchido, quando a situacao e
 * SEM_DIAS_DISPONIVEIS.
 */
public record SituacaoMotoristaDTO(
        UUID motoristaId,
        String nome,
        Integer diasDisponiveis,
        Integer diasOperacao,
        BigDecimal utilizacao,
        FaixaUtilizacao faixaUtilizacao,
        SituacaoMotorista situacao,
        String aviso
) {
}
