package br.com.logistica.msusuarios.services;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.logistica.msusuarios.dtos.CriarUsuarioRequest;
import br.com.logistica.msusuarios.entities.Perfil;
import br.com.logistica.msusuarios.entities.Usuario;
import br.com.logistica.msusuarios.repositories.UsuarioRepository;

@Service
public class UsuarioService {

    private static final int TAMANHO_MINIMO_SENHA = 4;

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * @throws EmailJaCadastradoException  email e unique no banco; tratado aqui para
     *                                     virar 409 em vez de erro de constraint
     * @throws IllegalArgumentException    campo obrigatorio ausente ou perfil invalido
     */
    public Usuario criar(CriarUsuarioRequest requisicao) {
        String nome = exigir(requisicao.nome(), "nome");
        String email = exigir(requisicao.email(), "email").toLowerCase(Locale.ROOT);
        String senha = exigir(requisicao.senha(), "senha");
        Perfil perfil = Perfil.de(exigir(requisicao.perfilAcesso(), "perfilAcesso"));

        if (senha.length() < TAMANHO_MINIMO_SENHA) {
            throw new IllegalArgumentException("A senha precisa de ao menos " + TAMANHO_MINIMO_SENHA + " caracteres");
        }
        if (repository.findByEmail(email).isPresent()) {
            throw new EmailJaCadastradoException(email);
        }

        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setPerfilAcesso(perfil);
        usuario.setStatusAtivo(true);
        // O unico lugar que grava senha: sempre com hash, nunca em texto.
        usuario.setSenha(passwordEncoder.encode(senha));

        return repository.save(usuario);
    }

    private String exigir(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Campo obrigatorio: " + campo);
        }
        return valor.trim();
    }

    public static class EmailJaCadastradoException extends RuntimeException {
        public EmailJaCadastradoException(String email) {
            super("Ja existe usuario com o e-mail " + email);
        }
    }
}
