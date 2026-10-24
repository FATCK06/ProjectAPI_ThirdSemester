package br.com.newe.ms_operacoes.security;

import java.util.Set;

/**
 * Permissoes que este servico confere dentro da resposta (campos, nao rotas - as
 * rotas inteiras ficam no SecurityConfig do api-gateway).
 *
 * Lista de quem PODE: perfil ausente, desconhecido ou novo fica de fora por
 * padrao. Na duvida, nega.
 */
public enum Permissao {
    VER_DADOS_FINANCEIROS(Set.of("ADMINISTRADOR", "GESTOR"));

    private final Set<String> perfis;

    Permissao(Set<String> perfis) {
        this.perfis = perfis;
    }

    /** @param perfil ja normalizado em maiusculas (ver PerfilResolver) */
    boolean concede(String perfil) {
        return perfil != null && perfis.contains(perfil);
    }
}
