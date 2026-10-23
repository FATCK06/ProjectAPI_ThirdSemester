package br.com.logistica.msusuarios.controllers;

import br.com.logistica.msusuarios.dtos.LoginRequestDTO;
import br.com.logistica.msusuarios.dtos.LoginResponseDTO;
import br.com.logistica.msusuarios.entities.Usuario;
import br.com.logistica.msusuarios.repositories.UsuarioRepository;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import br.com.logistica.msusuarios.dtos.LoginRequestDTO;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository; 

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private br.com.logistica.msusuarios.services.TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequestDTO credentials) {
        String email = credentials.email();
        String senha = credentials.senha();

        Optional<Usuario> usuarioOp = usuarioRepository.findByEmail(email);

        Map<String, Object> response = new HashMap<>();

        if (usuarioOp.isPresent() && passwordEncoder.matches(senha, usuarioOp.get().getSenha())) {
            Usuario usuarioLogado = usuarioOp.get();

            // verifica se usuario está ativo
            if (usuarioLogado.getStatusAtivo() != null && !usuarioLogado.getStatusAtivo()) {
                response.put("sucesso", false);
                response.put("mensagem", "Usuário inativo. Contate o adminstrador.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // login ok
            response.put("sucesso", true);
            String tokenReal = tokenService.gerarToken(usuarioLogado);
            response.put("token", tokenReal);
            response.put("nome", usuarioLogado.getNome());
            response.put("perfil", usuarioLogado.getPerfilAcesso().name());

            return ResponseEntity.ok(response);
        }
    
        // credenciais incorretas
        response.put("sucesso", false);
        response.put("mensagem", "E-mail ou senha incorretos.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}