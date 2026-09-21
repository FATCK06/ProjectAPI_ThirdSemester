package br.com.newe.ms_operacoes.security;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Le o perfil de quem fez a requisicao a partir de um header simples.
 * Placeholder ate o modulo de autenticacao (em desenvolvimento) ficar pronto:
 * quando ele existir, basta trocar a leitura do header por SecurityContext/claims do token aqui,
 * sem alterar controller nem service.
 */
@Component
public class PerfilResolver {

    public static final String HEADER_PERFIL = "X-User-Role";
    public static final String PERFIL_OPERADOR = "OPERADOR";
    public static final String HEADER_USUARIO = "X-User-Name";

    public String resolver(HttpServletRequest request) {
        String perfil = request.getHeader(HEADER_PERFIL);
        return perfil != null ? perfil.trim().toUpperCase() : null;
    }

    public boolean isOperador(HttpServletRequest request) {
        return PERFIL_OPERADOR.equals(resolver(request));
    }

    public String resolverUsuario(HttpServletRequest request) {
        String usuario = request.getHeader(HEADER_USUARIO);
        return usuario != null ? usuario.trim() : null;
    }
}
