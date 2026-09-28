package br.com.newe.ms_frota.dto;

import java.time.LocalDate;

/**
 * Um motorista vindo da importacao de manifestos. Endereco, cidade e estado
 * chegam como texto livre do CSV; quem separa e resolve e o ms-frota.
 */
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
