package br.com.newe.api_gateway.security;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
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
 * Valida o JWT na entrada e injeta a identidade nos headers que os servicos leem
 * (ver PerfilResolver no ms-operacoes).
 *
 * O ponto central e a limpeza: qualquer X-User-Role ou X-User-Name que venha do
 * cliente e DESCARTADO antes de rotear. Sem isso o gateway nao resolveria nada -
 * bastaria um curl com "X-User-Role: GESTOR" para ver dado financeiro, que era
 * exatamente a falha existente.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class JwtAutenticacaoFilter extends OncePerRequestFilter {

    public static final String HEADER_PERFIL = "X-User-Role";
    public static final String HEADER_USUARIO = "X-User-Name";

    /** Emissor definido no TokenService do ms-usuarios. */
    private static final String EMISSOR = "ms-usuarios";

    private static final Set<String> HEADERS_CONTROLADOS = Set.of(
            HEADER_PERFIL.toLowerCase(Locale.ROOT),
            HEADER_USUARIO.toLowerCase(Locale.ROOT));

    /**
     * Rotas alcancaveis sem token.
     *
     * /api/auth/     - o login, obviamente.
     * /api/usuarios  - cadastro, aberto para criar o primeiro Administrador.
     *                  TODO fechar assim que existir um: hoje qualquer um cria
     *                  usuario com perfil Administrador.
     */
    private static final List<String> PREFIXOS_PUBLICOS = List.of("/api/auth/", "/api/usuarios");

    private final JWTVerifier verificador;

    public JwtAutenticacaoFilter(@Value("${api.security.token.secret}") String secret) {
        // Falha na subida se a chave nao estiver configurada, em vez de aceitar
        // tudo silenciosamente.
        this.verificador = JWT.require(Algorithm.HMAC256(secret))
                .withIssuer(EMISSOR)
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // Preflight nao carrega Authorization; quem responde e o CorsFilter.
        if (HttpMethod.OPTIONS.matches(request.getMethod()) || ehPublica(request)) {
            chain.doFilter(semIdentidade(request), response);
            return;
        }

        String token = extrairToken(request);
        if (token == null) {
            recusar(response, "Token ausente");
            return;
        }

        DecodedJWT jwt;
        try {
            jwt = verificador.verify(token);
        } catch (JWTVerificationException e) {
            recusar(response, "Token invalido ou expirado");
            return;
        }

        Map<String, String> identidade = new HashMap<>();
        identidade.put(HEADER_USUARIO.toLowerCase(Locale.ROOT), jwt.getSubject());

        String perfil = jwt.getClaim("perfil").asString();
        if (perfil != null) {
            identidade.put(HEADER_PERFIL.toLowerCase(Locale.ROOT), perfil);
        }

        chain.doFilter(new RequestComIdentidade(request, identidade), response);
    }

    /**
     * Recusa escrevendo o corpo na mao, em vez de response.sendError().
     *
     * O sendError descarta a resposta e dispara o tratamento de erro do container,
     * que monta uma resposta nova - jogando fora os cabecalhos que o CorsFilter ja
     * tinha adicionado. Sem Access-Control-Allow-Origin, o navegador bloqueia a
     * resposta e o front recebe "erro de rede" no lugar de 401, escondendo a causa.
     */
    private void recusar(HttpServletResponse response, String mensagem) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"erro\":\"" + mensagem + "\"}");
        response.getWriter().flush();
    }

    private boolean ehPublica(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return PREFIXOS_PUBLICOS.stream().anyMatch(uri::startsWith);
    }

    /** Rota publica tambem precisa ter os headers limpos: ninguem entra com perfil forjado. */
    private HttpServletRequest semIdentidade(HttpServletRequest request) {
        return new RequestComIdentidade(request, Map.of());
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
