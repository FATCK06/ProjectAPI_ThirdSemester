package br.com.newe.ms_frota.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Um motorista vindo da importacao de manifestos. Endereco, cidade e estado
 * chegam como texto livre do CSV; quem separa e resolve e o ms-frota.
 */
public record MotoristaLoteItem(
        @NotBlank(message = "não pode ser vazio")
        @Pattern(regexp = "(?:\\d{11})?", message = "deve conter exatamente 11 dígitos")
        String cpf,
        @NotBlank(message = "não pode ser vazio")
        @Size(max = 150, message = "deve conter no máximo 150 caracteres")
        String nome,
        @Size(max = 20, message = "deve conter no máximo 20 caracteres")
        String pis,
        LocalDate dataNascimento,
        String endereco,
        @Size(max = 10, message = "deve conter no máximo 10 caracteres")
        String cep,
        @Size(max = 100, message = "deve conter no máximo 100 caracteres")
        String bairro,
        String cidade,
        String estado
) {
}
