package br.com.newe.ms_operacoes.dto.frota;

import java.util.UUID;

public record RankingMotoristaDTO(
        int posicao,
        UUID motoristaId,
        String nome,
        String cpf,
        long numeroViagens,
        String tipoVeiculo,
        Integer distanciaMaximaKm
) {
}
