package br.com.newe.ms_operacoes.dto;

import java.time.LocalDate;

/** Payload enviado ao ms-frota. Espelha o contrato do endpoint, nao uma classe compartilhada. */
public record MotoristaLoteItem(
        String cpf,
        String nome,
        String pis,
        LocalDate dataNascimento,
        String endereco,
        String cep,
        String bairro,
        String cidade,
        String estado
) {
}
