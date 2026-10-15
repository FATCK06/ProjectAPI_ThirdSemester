package br.com.logistica.msusuarios.dtos;

import java.util.UUID;

import br.com.logistica.msusuarios.entities.Usuario;

/**
 * Usuario devolvido pela API. Sem o campo senha - nem o hash sai daqui: expor
 * hash permite ataque de forca bruta offline.
 */
public record UsuarioResponse(
        UUID idUsuario,
        String nome,
        String email,
        String perfilAcesso,
        Boolean statusAtivo
) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getIdUsuario(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfilAcesso(),
                usuario.getStatusAtivo()
        );
    }
}
