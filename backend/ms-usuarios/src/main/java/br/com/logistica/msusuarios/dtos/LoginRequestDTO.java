package br.com.logistica.msusuarios.dtos;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
    @NotBlank String email,
    @NotBlank String senha) { 
}
