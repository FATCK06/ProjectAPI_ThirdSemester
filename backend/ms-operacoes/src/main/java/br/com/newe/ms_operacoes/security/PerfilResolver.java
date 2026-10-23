package br.com.newe.ms_operacoes.security;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Le o perfil de quem fez a requisicao no header X-User-Role, que o api-gateway
 * preenche com o valor do token JWT verificado (descartando o que vier do cliente).
 */
@Component
public class PerfilResolver {

    public static final String HEADER_PERFIL = "X-User-Role";
    public static final String HEADER_USUARIO = "X-User-Name";

    public boolean possui(HttpServletRequest request, Permissao permissao) {
        return permissao.concede(resolver(request));
    }

    /** Atalho para {@code possui(request, VER_DADOS_FINANCEIROS)}: a regra continua em Permissao. */
    public boolean podeVerDadosFinanceiros(HttpServletRequest request) {
        return possui(request, Permissao.VER_DADOS_FINANCEIROS);
    }

    public String resolverUsuario(HttpServletRequest request) {
        String usuario = request.getHeader(HEADER_USUARIO);
        return usuario != null ? usuario.trim() : null;
    }

    private String resolver(HttpServletRequest request) {
        String perfil = request.getHeader(HEADER_PERFIL);
        return perfil != null ? perfil.trim().toUpperCase() : null;
    }
}
