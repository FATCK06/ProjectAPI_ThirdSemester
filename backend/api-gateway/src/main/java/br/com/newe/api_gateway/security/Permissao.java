package br.com.newe.api_gateway.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Permissoes decididas na entrada (rota inteira). A regra fina, de campos dentro
 * da resposta (como dados financeiros), fica no servico dono do dado.
 *
 * As regras do SecurityConfig perguntam pela permissao, nunca pelo perfil: um
 * perfil novo (ou um perfil ganhando acesso) muda so este mapa.
 */
public enum Permissao {
    GERENCIAR_USUARIOS(Perfil.ADMINISTRADOR),
    VER_STATUS_SERVICO(Perfil.ADMINISTRADOR),
    VER_DADOS_FINANCEIROS(Perfil.ADMINISTRADOR, Perfil.GESTOR);

    private final Set<Perfil> perfis;

    Permissao(Perfil... perfis) {
        this.perfis = EnumSet.copyOf(Arrays.asList(perfis));
    }

    /** Nome usado como authority no SecurityContext e nas regras do SecurityConfig. */
    public String authority() {
        return name();
    }

    /** O papel (ROLE_*) e todas as permissoes que o perfil concede. */
    public static List<GrantedAuthority> autoridadesDe(Perfil perfil) {
        List<GrantedAuthority> autoridades = new ArrayList<>();
        autoridades.add(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
        for (Permissao permissao : values()) {
            if (permissao.perfis.contains(perfil)) {
                autoridades.add(new SimpleGrantedAuthority(permissao.authority()));
            }
        }
        return autoridades;
    }
}
