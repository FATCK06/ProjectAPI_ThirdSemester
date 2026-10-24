package br.com.newe.ms_frota.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Um agregado vindo da importacao. Documento so com digitos: 11 = CPF, 14 = CNPJ. */
public record AgregadoLoteItem(
        @NotBlank(message = "não pode ser vazio")
        @Pattern(regexp = "(?:\\d{11}|\\d{14})?", message = "deve conter 11 ou 14 dígitos")
        String documento,
        @NotBlank(message = "não pode ser vazio")
        @Size(max = 150, message = "deve conter no máximo 150 caracteres")
        String nome,
        @Size(max = 11, message = "deve conter no máximo 11 caracteres")
        String pis,
        @Size(max = 50, message = "deve conter no máximo 50 caracteres")
        String regimeFiscal
) {
}
