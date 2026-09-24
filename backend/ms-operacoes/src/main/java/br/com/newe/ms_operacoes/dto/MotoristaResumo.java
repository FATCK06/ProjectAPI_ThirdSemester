package br.com.newe.ms_operacoes.dto;

import java.util.UUID;

/** Resposta do ms-frota em /api/motoristas/resumos. */
public record MotoristaResumo(
        UUID id,
        String nome,
        String cpf
) {
}
