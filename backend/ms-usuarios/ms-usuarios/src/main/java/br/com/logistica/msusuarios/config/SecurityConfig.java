package br.com.logistica.msusuarios.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired 
    private SecurityFilter securityFilter;

    // cria o criptografador oficial do projeto
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll() // Destranca a porta de login
                // Aberto para permitir criar o primeiro Administrador.
                // TODO restringir a ADMINISTRADOR assim que existir um.
                .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Destranca o Pre-flight do CORS para o React
                // O gateway consulta isto de servidor para servidor, sem token.
                // Nao vaza nada: show-details=never devolve so UP ou DOWN.
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated() // Tranca o resto
            )
            .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }
}
