package br.com.newe.ms_frota.dto;

import java.util.UUID;

/** Dados minimos do motorista para exibicao em outros servicos (ex.: dashboard). */
public record MotoristaResumo(
        UUID id,
        String nome,
        String cpf
) {
}
