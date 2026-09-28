package br.com.newe.ms_operacoes.dto;

import java.util.UUID;

public record RankingMotoristaDTO(
        int posicao,
        UUID motoristaId,
        String nome,
        String cpf,
        long totalViagens,
        String veiculo,
        Integer distanciaMaximaKm
) {
}
