package br.com.newe.ms_operacoes.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Campos do resumo mensal: dados cadastrais e operacionais, sem valores
 * financeiros. diasUtilizados conta datas distintas com viagens; utilizacao e
 * o percentual desses dias sobre todos os dias do mês.
 */
public record MotoristaMesDTO(
        UUID motoristaId,
        UUID veiculoId,
        String nomeMotorista,
        String placa,
        String tipoVeiculo,
        long viagensNoMes,
        long diasUtilizados,
        long diasDisponiveis,
        BigDecimal disponibilidade,
        BigDecimal utilizacao,
        BigDecimal percentualEfetividade
) {
}
