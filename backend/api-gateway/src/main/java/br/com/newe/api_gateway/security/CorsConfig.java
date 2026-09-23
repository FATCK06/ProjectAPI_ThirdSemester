package br.com.newe.api_gateway.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS fica so no gateway: como o navegador nunca chama os servicos direto,
 * nao faz sentido cada um repetir esta configuracao. Os servicos de tras NAO
 * devem ter @CrossOrigin - dois Access-Control-Allow-Origin na mesma resposta
 * fazem o navegador recusa-la.
 *
 * A origem e explicita (nao "*") porque o navegador recusa curinga em requisicao
 * que carrega credenciais - e toda chamada nossa leva o header Authorization.
 */
@Configuration
public class CorsConfig {

    /**
     * Registrado via FilterRegistrationBean para garantir a ordem.
     *
     * @Order em @Bean de filtro nao estava sendo respeitado: o CorsFilter rodava
     * depois do JwtAutenticacaoFilter, entao toda resposta 401 saia sem cabecalho
     * de CORS. O navegador bloqueava, e o front via "erro de rede" em vez de 401 -
     * escondendo a causa real. Aqui a precedencia e explicita.
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration(
            @Value("${cors.origem-permitida}") String origemPermitida) {

        CorsConfiguration configuracao = new CorsConfiguration();
        // Padrao em vez de origem fixa: o Vite sobe em 5174 quando a 5173 esta
        // ocupada, e uma origem fixa faria o navegador bloquear toda resposta -
        // aparecendo no front como "servidor indisponivel", nao como erro de CORS.
        //
        // setAllowedOriginPatterns (e nao setAllowedOrigins com "*") porque com
        // allowCredentials o navegador recusa o curinga literal.
        configuracao.setAllowedOriginPatterns(List.of(origemPermitida));
        configuracao.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracao.setAllowedHeaders(List.of("*"));
        configuracao.setAllowCredentials(true);
        configuracao.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/**", configuracao);

        FilterRegistrationBean<CorsFilter> registro = new FilterRegistrationBean<>(new CorsFilter(fonte));
        registro.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registro;
    }
}
