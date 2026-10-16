package br.com.logistica.msusuarios.services;

import br.com.logistica.msusuarios.entities.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class TokenService {

    private final String secret;

    public TokenService(@Value("${api.security.token.secret}") String secret) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                    "api.security.token.secret ausente ou curto demais (minimo 32 caracteres)");
        }
        this.secret = secret;
    }

    public String gerarToken(Usuario usuario) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

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

    // Instant.now() é absoluto: não depende do fuso do servidor.
    private Instant gerarDataExpiracao() {
        return Instant.now().plus(2, ChronoUnit.HOURS);
    }

    public String validarToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("ms-usuarios")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return "";
        }
    }
}