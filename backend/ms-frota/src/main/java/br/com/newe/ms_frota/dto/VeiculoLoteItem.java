package br.com.newe.ms_frota.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Um veiculo vindo da importacao. O CSV de manifestos traz so a placa (e, as
 * vezes, capacidade e nome do agregado), entao o cadastro nasce minimo e e
 * completado depois pelo CRUD de frota.
 */
public record VeiculoLoteItem(
        @NotBlank (message = "não pode ser vazio")
        @Pattern(regexp = "[A-Za-z]{3}-?[0-9][A-Za-z0-9][0-9]{2}" , message = "Deve conter formato de placa válido, ex: ABC-1234 ou ABC1D23")
        String placa,
        @DecimalMin(value = "0", message = "deve ser maior ou igual a zero")
        @Digits(integer = 8, fraction = 2, message = "deve ter até 8 dígitos inteiros e 2 casas decimais")
        BigDecimal capacidade,
        @Size(max = 150, message = "deve conter no máximo 150 caracteres")
        String nomeAgregado
) {
}
