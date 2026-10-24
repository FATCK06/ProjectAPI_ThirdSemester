package br.com.logistica.msusuarios.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import br.com.logistica.msusuarios.dtos.CriarUsuarioRequest;
import br.com.logistica.msusuarios.entities.Perfil;
import br.com.logistica.msusuarios.repositories.UsuarioRepository;
import br.com.logistica.msusuarios.services.UsuarioService;
import br.com.logistica.msusuarios.services.UsuarioService.EmailJaCadastradoException;

/**
 * Cria o primeiro Administrador na subida, quando ainda nao existe nenhum.
 *
 * Substitui o cadastro aberto: antes o POST /api/usuarios era publico so para
 * permitir esse primeiro cadastro, e com isso qualquer um criava Administrador.
 * Agora o cadastro exige Administrador, e o primeiro vem daqui.
 *
 * So age com e-mail e senha configurados (ADMIN_INICIAL_EMAIL e
 * ADMIN_INICIAL_SENHA). Depois que existe um Administrador, nao faz nada - pode
 * deixar as variaveis configuradas, mas o ideal e trocar a senha e remove-las.
 */
@Component
public class AdministradorInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdministradorInicial.class);

    private final UsuarioRepository repository;
    private final UsuarioService service;
    private final String nome;
    private final String email;
    private final String senha;

    public AdministradorInicial(
            UsuarioRepository repository,
            UsuarioService service,
            @Value("${app.admin-inicial.nome:Administrador}") String nome,
            @Value("${app.admin-inicial.email:}") String email,
            @Value("${app.admin-inicial.senha:}") String senha) {
        this.repository = repository;
        this.service = service;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (repository.existsByPerfilAcesso(Perfil.ADMINISTRADOR)) {
            return;
        }
        if (email.isBlank() || senha.isBlank()) {
            log.warn("Nenhum Administrador cadastrado e ADMIN_INICIAL_EMAIL/ADMIN_INICIAL_SENHA nao configurados: "
                    + "ninguem conseguira cadastrar usuarios.");
            return;
        }

        try {
            service.criar(new CriarUsuarioRequest(nome, email, senha, Perfil.ADMINISTRADOR.name()));
            log.info("Administrador inicial criado: {}", email);
        } catch (EmailJaCadastradoException e) {
            log.warn("ADMIN_INICIAL_EMAIL ({}) ja pertence a um usuario que nao e Administrador. "
                    + "Promova-o no banco ou use outro e-mail.", email);
        }
    }
}
