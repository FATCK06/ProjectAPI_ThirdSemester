package br.com.newe.ms_operacoes.security;

import java.util.Set;

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

    public static final String PERFIL_ADMINISTRADOR = "ADMINISTRADOR";
    public static final String PERFIL_GESTOR = "GESTOR";
    public static final String PERFIL_OPERADOR = "OPERADOR";

    // Quem PODE ver dados financeiros. Perfil ausente, desconhecido ou novo
    // fica de fora por padrao: na duvida, nega.
    private static final Set<String> PERFIS_DADOS_FINANCEIROS = Set.of(PERFIL_ADMINISTRADOR, PERFIL_GESTOR);

    public boolean podeVerDadosFinanceiros(HttpServletRequest request) {
        String perfil = resolver(request);
        return perfil != null && PERFIS_DADOS_FINANCEIROS.contains(perfil);
    }

    public boolean isOperador(HttpServletRequest request) {
        return PERFIL_OPERADOR.equals(resolver(request));
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