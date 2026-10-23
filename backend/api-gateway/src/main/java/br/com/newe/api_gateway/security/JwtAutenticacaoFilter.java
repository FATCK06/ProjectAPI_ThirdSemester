package br.com.newe.api_gateway.security;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Valida o JWT na entrada, preenche o SecurityContext com as permissoes do perfil
 * e injeta a identidade nos headers que os servicos leem (ver PerfilResolver no
 * ms-operacoes).
 *
 * Este filtro nao recusa nada: so diz QUEM e. Quem decide se a rota exige login
 * (401) ou uma permissao (403) e o SecurityConfig. Por isso token invalido nao
 * barra aqui - o login, que e publico, tem que funcionar mesmo com um token
 * vencido ainda guardado no navegador.
 *
 * O outro ponto central e a limpeza: qualquer X-User-Role ou X-User-Name que
 * venha do cliente e DESCARTADO antes de rotear, em toda requisicao. Sem isso
 * bastaria um curl com "X-User-Role: GESTOR" para ver dado financeiro.
 *
 * Nao e @Component de proposito: o Spring Boot registraria o filtro tambem como
 * filtro de servlet, fora da cadeia do Spring Security. O SecurityConfig o cria.
 */
public class JwtAutenticacaoFilter extends OncePerRequestFilter {

    public static final String HEADER_PERFIL = "X-User-Role";
    public static final String HEADER_USUARIO = "X-User-Name";

    /** Emissor definido no TokenService do ms-usuarios. */
    private static final String EMISSOR = "ms-usuarios";

    private static final Set<String> HEADERS_CONTROLADOS = Set.of(
            HEADER_PERFIL.toLowerCase(Locale.ROOT),
            HEADER_USUARIO.toLowerCase(Locale.ROOT));

    private final JWTVerifier verificador;

    public JwtAutenticacaoFilter(String secret) {
        if (secret == null || secret.length() < 32){
            throw new IllegalStateException(
                "api.security.token.secret ausente ou curto demais (minimo 32 caracteres)");
        }
        this.verificador = JWT.require(Algorithm.HMAC256(secret))
            .withIssuer(EMISSOR)
            .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        Map<String, String> identidade = new HashMap<>();

        DecodedJWT jwt = verificar(extrairToken(request));
        if (jwt != null) {
            Optional<Perfil> perfil = Perfil.de(jwt.getClaim("perfil").asString());

            // Perfil desconhecido ainda e um usuario logado, so que sem permissao nenhuma.
            List<GrantedAuthority> autoridades = perfil.map(Permissao::autoridadesDe).orElse(List.of());
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(jwt.getSubject(), null, autoridades));

            identidade.put(HEADER_USUARIO.toLowerCase(Locale.ROOT), jwt.getSubject());
            perfil.ifPresent(p -> identidade.put(HEADER_PERFIL.toLowerCase(Locale.ROOT), p.name()));
        }

        chain.doFilter(new RequestComIdentidade(request, identidade), response);
    }

    /** Token assinado por nos, do emissor certo e com validade. null em qualquer outro caso. */
    private DecodedJWT verificar(String token) {
        if (token == null) {
            return null;
        }
        try {
            DecodedJWT jwt = verificador.verify(token);
            return jwt.getExpiresAt() == null ? null : jwt;
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    private String extrairToken(HttpServletRequest request) {
        String cabecalho = request.getHeader("Authorization");
        if (cabecalho == null || !cabecalho.startsWith("Bearer ")) {
            return null;
        }
        String token = cabecalho.substring("Bearer ".length()).trim();
        return token.isEmpty() ? null : token;
    }

    /**
     * Substitui os headers de identidade pelos valores verificados e esconde
     * qualquer valor que tenha vindo do cliente.
     */
    private static class RequestComIdentidade extends HttpServletRequestWrapper {

        private final Map<String, String> identidade;

        RequestComIdentidade(HttpServletRequest request, Map<String, String> identidade) {
            super(request);
            this.identidade = identidade;
        }

        @Override
        public String getHeader(String nome) {
            String chave = nome.toLowerCase(Locale.ROOT);
            if (HEADERS_CONTROLADOS.contains(chave)) {
                return identidade.get(chave);
            }
            return super.getHeader(nome);
        }

        @Override
        public Enumeration<String> getHeaders(String nome) {
            String chave = nome.toLowerCase(Locale.ROOT);
            if (HEADERS_CONTROLADOS.contains(chave)) {
                String valor = identidade.get(chave);
                return Collections.enumeration(valor == null ? List.of() : List.of(valor));
            }
            return super.getHeaders(nome);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            java.util.List<String> nomes = new java.util.ArrayList<>();

            Enumeration<String> originais = super.getHeaderNames();
            while (originais.hasMoreElements()) {
                String nome = originais.nextElement();
                if (!HEADERS_CONTROLADOS.contains(nome.toLowerCase(Locale.ROOT))) {
                    nomes.add(nome);
                }
            }

            if (identidade.containsKey(HEADER_USUARIO.toLowerCase(Locale.ROOT))) {
                nomes.add(HEADER_USUARIO);
            }
            if (identidade.containsKey(HEADER_PERFIL.toLowerCase(Locale.ROOT))) {
                nomes.add(HEADER_PERFIL);
            }

            return Collections.enumeration(nomes);
        }
    }
}
