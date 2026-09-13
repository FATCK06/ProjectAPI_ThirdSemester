package br.com.logistica.msusuarios.controllers;

import br.com.logistica.msusuarios.dtos.LoginResponseDTO;
import br.com.logistica.msusuarios.entities.Usuario;
import br.com.logistica.msusuarios.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository; 

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String senha = credentials.get("senha");

        Map<String, Object> response = new HashMap<>(); 

        // 1. Vai no Supabse e busca se existe algum usuário com este email
        Optional<Usuario> usuarioOp = usuarioRepository.findByEmail(email);

        // 2. if usuario
        if (usuarioOp.isPresent() && usuarioOp.get().getSenha().equals(senha)) {
            Usuario usuarioLogado = usuarioOp.get();

            //verifica se usuario está ativo
            if (usuarioLogado.getStatusAtivo() != null && !usuarioLogado.getStatusAtivo()) {
                response.put("sucesso", false);
                response.put("mensagem", "Usuário inativo. Contate o adminstrador.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            //login ok
            response.put("sucesso", true);
            response.put("token", "token-simulado-jwt-123456");
            response.put("nome", usuarioLogado.getNome());
            response.put("perfil", usuarioLogado.getPerfilAcesso());

            return ResponseEntity.ok(response);
        }
    
        //credencias incorreta
        response.put("sucesso", false);
        response.put("mensagem", "E-mal ou senha incorretos.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);

    }
}