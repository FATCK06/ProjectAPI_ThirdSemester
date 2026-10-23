package br.com.logistica.msusuarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import br.com.logistica.msusuarios.entities.Perfil;
import br.com.logistica.msusuarios.entities.Usuario;
import br.com.logistica.msusuarios.repositories.UsuarioRepository;
import br.com.logistica.msusuarios.services.TokenService;

@SpringBootTest(properties = {
        "app.admin-inicial.email=admin@teste.com",
        "app.admin-inicial.senha=senha-do-admin"
})
@AutoConfigureMockMvc
class CadastroUsuarioSegurancaTest {

    private static final String CORPO = """
            {"nome":"Novo","email":"novo@teste.com","senha":"1234","perfilAcesso":"ADMINISTRADOR"}
            """;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private TokenService tokenService;

    @Test
    void administradorInicialECriadoNaSubida() {
        Usuario admin = repository.findByEmail("admin@teste.com").orElseThrow();
        assertThat(admin.getPerfilAcesso()).isEqualTo(Perfil.ADMINISTRADOR);
    }

    @Test
    void cadastroSemTokenERecusado() throws Exception {
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isForbidden());
    }

    @Test
    void cadastroPorNaoAdministradorERecusado() throws Exception {
        Usuario gestor = new Usuario();
        gestor.setNome("Gestor");
        gestor.setEmail("gestor@teste.com");
        gestor.setSenha("x");
        gestor.setPerfilAcesso(Perfil.GESTOR);
        repository.save(gestor);

        mvc.perform(post("/api/usuarios")
                        .header("Authorization", "Bearer " + tokenService.gerarToken(gestor))
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorCadastra() throws Exception {
        Usuario admin = repository.findByEmail("admin@teste.com").orElseThrow();

        mvc.perform(post("/api/usuarios")
                        .header("Authorization", "Bearer " + tokenService.gerarToken(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isCreated());
    }
}
