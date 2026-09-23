package br.com.logistica.msusuarios.dtos;

/**
 * Dados de cadastro vindos da tela. A senha chega em texto e NUNCA e gravada
 * assim: o UsuarioService aplica BCrypt antes de persistir.
 */
public record CriarUsuarioRequest(
        String nome,
        String email,
        String senha,
        String perfilAcesso
) {
}
