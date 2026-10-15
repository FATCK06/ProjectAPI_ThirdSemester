package br.com.logistica.msusuarios.services;

import br.com.logistica.msusuarios.entities.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service 
public class TokenService {
    
    // CORREÇÃO 1: Faltava o '}' no final da chave
    @Value("${api.security.token.secret}")
    private String secret;

    public String gerarToken(Usuario usuario){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret); // algoritmo de criptografia

            return JWT.create()
                .withIssuer("ms-usuarios")
                .withSubject(usuario.getEmail())
                .withClaim("nome", usuario.getNome())
                .withClaim("perfil", usuario.getPerfilAcesso())
                .withExpiresAt(gerarDataExpiracao()) // 2 horas de validade
                .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    private Instant gerarDataExpiracao(){
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }

    public String validarToken(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("ms-usuarios")
                    .build()
                    .verify(token)
                    .getSubject(); // Devolve o e-mail do dono do token
        } catch (JWTVerificationException exception){
            return ""; // Se o token for falso, alterado ou estiver vencido, devolve vazio
        }
    }
}