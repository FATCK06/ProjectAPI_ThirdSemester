package br.com.newe.api_gateway.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Regras de acesso por rota do SecurityConfig. So testa as recusas: uma rota
 * liberada seria repassada ao microsservico, que nao esta no ar no teste.
 */
@SpringBootTest(properties = "api.security.token.secret=" + AutorizacaoGatewayTest.SEGREDO)
@AutoConfigureMockMvc
class AutorizacaoGatewayTest {

    static final String SEGREDO = "segredo-apenas-para-testes-com-32-caracteres-ou-mais";

    @Autowired
    private MockMvc mvc;

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void semTokenRecebe401() throws Exception {
        mvc.perform(get("/api/dashboard/indicadores")).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenAssinadoComOutraChaveRecebe401() throws Exception {
        String forjado = JWT.create().withIssuer("ms-usuarios").withSubject("x@x.com")
                .withClaim("perfil", "ADMINISTRADOR")
                .withExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .sign(Algorithm.HMAC256("outra-chave-qualquer-com-32-caracteres-ou-mais"));

        mvc.perform(get("/api/status").header("Authorization", "Bearer " + forjado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void statusSoParaAdministrador() throws Exception {
        mvc.perform(get("/api/status").header("Authorization", bearer("OPERADOR")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/status").header("Authorization", bearer("GESTOR")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/status").header("Authorization", bearer("ADMINISTRADOR")))
                .andExpect(status().isOk());
    }

    @Test
    void tokenAntigoComPerfilEmCaixaMistaContinuaValendo() throws Exception {
        mvc.perform(get("/api/status").header("Authorization", bearer("Administrador")))
                .andExpect(status().isOk());
    }

    @Test
    void cadastroDeUsuarioExigeAdministrador() throws Exception {
        mvc.perform(post("/api/usuarios")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/usuarios").header("Authorization", bearer("GESTOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void operadorNaoAcessaEndpointsComValores() throws Exception {
        String operador = bearer("OPERADOR");
        mvc.perform(get("/api/viagens").header("Authorization", operador)).andExpect(status().isForbidden());
        mvc.perform(get("/api/importacao/arquivos/1/viagens").header("Authorization", operador))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/importacao/arquivos/1/conteudo").header("Authorization", operador))
                .andExpect(status().isForbidden());
    }

    @Test
    void perfilDesconhecidoNaoGanhaPermissao() throws Exception {
        mvc.perform(get("/api/status").header("Authorization", bearer("SUPERUSUARIO")))
                .andExpect(status().isForbidden());
    }

    @Test
    void urlMalformadaNaoBurlaARegra() throws Exception {
        int codigo = mvc.perform(get("/api//status").header("Authorization", bearer("OPERADOR")))
                .andReturn().getResponse().getStatus();
        assertThat(codigo).isNotEqualTo(200);
    }

    @Test
    void headersDeIdentidadeDoClienteSaoDescartados() throws Exception {
        MockHttpServletRequest requisicao = new MockHttpServletRequest("GET", "/api/ranking/mensal");
        requisicao.addHeader("Authorization", bearer("OPERADOR"));
        requisicao.addHeader("X-User-Role", "ADMINISTRADOR");
        requisicao.addHeader("X-User-Name", "outro@x.com");

        MockFilterChain cadeia = new MockFilterChain();
        new JwtAutenticacaoFilter(SEGREDO).doFilter(requisicao, new MockHttpServletResponse(), cadeia);

        HttpServletRequest repassada = (HttpServletRequest) cadeia.getRequest();
        assertThat(repassada.getHeader("X-User-Role")).isEqualTo("OPERADOR");
        assertThat(repassada.getHeader("X-User-Name")).isEqualTo("usuario@teste.com");
    }

    @Test
    void semTokenOsHeadersDeIdentidadeSomem() throws Exception {
        MockHttpServletRequest requisicao = new MockHttpServletRequest("POST", "/api/auth/login");
        requisicao.addHeader("Authorization", "Bearer token-vencido-ou-lixo");
        requisicao.addHeader("X-User-Role", "ADMINISTRADOR");

        MockFilterChain cadeia = new MockFilterChain();
        new JwtAutenticacaoFilter(SEGREDO).doFilter(requisicao, new MockHttpServletResponse(), cadeia);

        // Token invalido nao barra (o login e publico), mas tambem nao autentica.
        HttpServletRequest repassada = (HttpServletRequest) cadeia.getRequest();
        assertThat(repassada).isNotNull();
        assertThat(repassada.getHeader("X-User-Role")).isNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private static String bearer(String perfil) {
        return "Bearer " + JWT.create()
                .withIssuer("ms-usuarios")
                .withSubject("usuario@teste.com")
                .withClaim("perfil", perfil)
                .withExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .sign(Algorithm.HMAC256(SEGREDO));
    }
}
