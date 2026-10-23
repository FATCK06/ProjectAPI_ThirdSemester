package br.com.logistica.msusuarios.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDTO(
    @NotBlank(message = "obrigatório") @Size(max = 100, message = "até 100 caracteres") String email,
    @NotBlank(message = "obrigatório") @Size(max = 72, message = "até 72 caracteres") String senha) {
}
