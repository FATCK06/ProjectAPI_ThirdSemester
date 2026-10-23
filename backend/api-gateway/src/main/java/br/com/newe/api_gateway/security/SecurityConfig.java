package br.com.newe.api_gateway.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Quem pode chamar o que. Unico lugar com regra de acesso por rota.
 *
 * Toda rota nao listada exige apenas login. Rota nova que precise de permissao
 * entra aqui, perguntando pela Permissao - nunca pelo perfil direto.
 *
 * O CORS continua no CorsConfig: ele roda antes desta cadeia, entao o preflight
 * (OPTIONS) e respondido la e nunca chega aqui.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Value("${api.security.token.secret}") String secret) throws Exception {

        http
                .csrf(csrf -> csrf.disable()) // API sem cookie de sessao: CSRF nao se aplica
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

                        .requestMatchers("/api/usuarios/**")
                                .hasAuthority(Permissao.GERENCIAR_USUARIOS.authority())
                        .requestMatchers("/api/status/**")
                                .hasAuthority(Permissao.VER_STATUS_SERVICO.authority())

                        // Endpoints que devolvem a viagem inteira (com valores) ou o
                        // CSV original. Os que so ESCONDEM campos financeiros ficam
                        // abertos e o ms-operacoes filtra (ver PerfilResolver la).
                        .requestMatchers(
                                "/api/viagens/**",
                                "/api/importacao/arquivos/*/viagens/**",
                                "/api/importacao/arquivos/*/conteudo/**")
                                .hasAuthority(Permissao.VER_DADOS_FINANCEIROS.authority())

                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                responder(res, HttpStatus.UNAUTHORIZED, "Token ausente, invalido ou expirado"))
                        .accessDeniedHandler((req, res, ex) ->
                                responder(res, HttpStatus.FORBIDDEN, "Sem permissao para este recurso")))
                .addFilterBefore(new JwtAutenticacaoFilter(secret), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Escreve o corpo na mao, em vez de response.sendError().
     *
     * O sendError descarta a resposta e dispara o tratamento de erro do container,
     * que monta uma resposta nova - jogando fora os cabecalhos que o CorsFilter ja
     * tinha adicionado. Sem Access-Control-Allow-Origin, o navegador bloqueia a
     * resposta e o front recebe "erro de rede" no lugar de 401/403, escondendo a causa.
     */
    private static void responder(HttpServletResponse response, HttpStatus status, String mensagem)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"erro\":\"" + mensagem + "\"}");
        response.getWriter().flush();
    }
}
