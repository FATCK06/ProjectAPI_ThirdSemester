package br.com.newe.api_gateway.security;

import java.util.Set;

/**
 * Permissoes decididas na entrada (rota inteira). A regra fina, de campos dentro
 * da resposta (como dados financeiros), fica no servico dono do dado.
 */
public enum Permissao {
    CADASTRAR_USUARIO(Set.of("ADMINSTRADOR")),
    VER_STATUS_SERVICO(Set.of("ADMINISTRADOR"));

    private final Set<String> perfis;

    Permissao(Set<String> perfis) {
        this.perfis = perfis;
    }

    public boolean concede(String perfil) {
        return perfil != null && perfis.contains(perfil);
    }

     /** Qual permissao a rota exige. Retorna null quando basta estar logado. */
     public static Permissao exigidaPara(String uri) {
        if (corresponde(uri, "/api/usuarios")) return CADASTRAR_USUARIO;
        if (corresponde(uri, "/api/status")) return VER_STATUS_SERVICO;
        return null;
     }

     private static boolean corresponde(String uri, String base) {
        return uri.equals(base) || uri.startsWith(base + "/");
     }
}
