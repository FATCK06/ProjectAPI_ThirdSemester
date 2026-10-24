package br.com.logistica.msusuarios.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.unit.DataSize;

import br.com.logistica.msusuarios.erro.ErroHandlerGlobal;
import br.com.logistica.msusuarios.erro.RespostaErro;
import br.com.logistica.msusuarios.services.UsuarioService;

/** Sem banco: o service e mock, o que se testa e a validacao antes dele. */
class UsuarioControllerTest {

    private final UsuarioService service = mock(UsuarioService.class);
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new UsuarioController(service))
                .setControllerAdvice(new ErroHandlerGlobal(DataSize.ofMegabytes(10)))
                .build();
    }

    @Test
    void cadastroInvalidoDevolve400ComOsCamposENaoChegaNoService() throws Exception {
        String corpo = """
                {"nome": "", "email": "nao-e-email", "senha": "%s", "perfilAcesso": "Gestor"}
                """.formatted("x".repeat(73));

        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value(RespostaErro.DADOS_INVALIDOS))
                .andExpect(jsonPath("$.campos.nome").value("obrigatório"))
                .andExpect(jsonPath("$.campos.email").value("e-mail inválido"))
                .andExpect(jsonPath("$.campos.senha").value("até 72 caracteres"));

        verify(service, never()).criar(any());
    }
}
