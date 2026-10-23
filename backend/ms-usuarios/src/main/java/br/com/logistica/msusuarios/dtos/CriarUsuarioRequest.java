package br.com.logistica.msusuarios.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados de cadastro vindos da tela. A senha chega em texto e NUNCA e gravada
 * assim: o UsuarioService aplica BCrypt antes de persistir.
 *
 * Os limites de tamanho sao os das colunas. Tamanho minimo da senha e perfil
 * valido continuam no UsuarioService.
 */
public record CriarUsuarioRequest(
        @NotBlank(message = "obrigatório")
        @Size(max = 100, message = "até 100 caracteres")
        String nome,

        @NotBlank(message = "obrigatório")
        @Email(message = "e-mail inválido")
        @Size(max = 100, message = "até 100 caracteres")
        String email,

        // O BCrypt recusa senha acima de 72 bytes; aqui vira 400 antes de chegar nele.
        @NotBlank(message = "obrigatório")
        @Size(max = 72, message = "até 72 caracteres")
        String senha,

        @NotBlank(message = "obrigatório")
        String perfilAcesso
) {
}
