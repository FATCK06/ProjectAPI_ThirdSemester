package br.com.logistica.msusuarios.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.logistica.msusuarios.dtos.CriarUsuarioRequest;
import br.com.logistica.msusuarios.dtos.UsuarioResponse;
import br.com.logistica.msusuarios.entities.Usuario;
import br.com.logistica.msusuarios.services.UsuarioService;
import br.com.logistica.msusuarios.services.UsuarioService.EmailJaCadastradoException;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    /**
     * Cadastro de usuario. A senha chega em texto e sai gravada com BCrypt -
     * por isso criar usuario direto no banco nao funciona: o login compara hash
     * com hash, e texto puro nunca bate.
     *
     * TODO: hoje esta aberto para permitir criar o primeiro Administrador.
     * Assim que existir um, restringir a esse perfil.
     */
    @PostMapping
    public ResponseEntity<?> criar(@RequestBody CriarUsuarioRequest requisicao) {
        try {
            Usuario criado = service.criar(requisicao);
            return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.de(criado));

        } catch (EmailJaCadastradoException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", e.getMessage()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}
