package br.com.newe.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

// Sem UserDetailsService: quem autentica e o JWT (JwtAutenticacaoFilter). Excluir
// evita o usuario "user" com senha gerada que o Spring Security criaria sozinho.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}
