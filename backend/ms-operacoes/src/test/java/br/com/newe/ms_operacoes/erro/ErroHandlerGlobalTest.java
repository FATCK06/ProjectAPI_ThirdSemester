package br.com.newe.ms_operacoes.erro;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Sem banco: um controller falso dispara cada tipo de erro. */
class ErroHandlerGlobalTest {

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ControllerFalso())
                .setControllerAdvice(new ErroHandlerGlobal(DataSize.ofMegabytes(10)))
                .build();
    }

    @Test
    void parametroFaltandoDevolve400ComOCampo() throws Exception {
        mvc.perform(get("/falso/mes"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value(RespostaErro.DADOS_INVALIDOS))
                .andExpect(jsonPath("$.campos.mesReferencia").value("obrigatório"));
    }

    @Test
    void tipoErradoDevolve400ComOCampo() throws Exception {
        mvc.perform(get("/falso/limite").param("limite", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.limite").value("formato inválido"));
    }

    @Test
    void parametroForaDoFormatoDevolve400ComOCampo() throws Exception {
        mvc.perform(get("/falso/mes").param("mesReferencia", "abril"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value(RespostaErro.DADOS_INVALIDOS))
                .andExpect(jsonPath("$.campos.mesReferencia").value("use AAAA-MM"));
    }

    @Test
    void corpoInvalidoDevolve400ComOCampo() throws Exception {
        mvc.perform(post("/falso/corpo").contentType(MediaType.APPLICATION_JSON).content("{\"nome\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").value("obrigatório"));
    }

    @Test
    void jsonQuebradoDevolve400SemCampos() throws Exception {
        mvc.perform(post("/falso/corpo").contentType(MediaType.APPLICATION_JSON).content("{nome"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value(RespostaErro.DADOS_INVALIDOS))
                .andExpect(jsonPath("$.campos").doesNotExist());
    }

    @Test
    void erroInesperadoNaoVazaODetalhe() throws Exception {
        mvc.perform(get("/falso/quebra"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.erro").value(RespostaErro.FALHA_GENERICA))
                .andExpect(content().string(Matchers.not(Matchers.containsString("viagens"))));
    }

    @Test
    void servicoForaDoArDevolve503SemAUrl() throws Exception {
        mvc.perform(get("/falso/frota"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.erro").value(RespostaErro.FALHA_GENERICA))
                .andExpect(content().string(Matchers.not(Matchers.containsString("8082"))));
    }

    @RestController
    static class ControllerFalso {

        @GetMapping("/falso/mes")
        String mes(@RequestParam @Pattern(regexp = "\\d{4}-\\d{2}", message = "use AAAA-MM") String mesReferencia) {
            return mesReferencia;
        }

        @GetMapping("/falso/limite")
        int limite(@RequestParam int limite) {
            return limite;
        }

        @PostMapping("/falso/corpo")
        String corpo(@Valid @RequestBody Corpo corpo) {
            return corpo.nome();
        }

        @GetMapping("/falso/quebra")
        String quebra() {
            throw new IllegalStateException("ERROR: relation \"viagens\" does not exist");
        }

        @GetMapping("/falso/frota")
        String frota() {
            throw new ResourceAccessException("I/O error on GET request for \"http://localhost:8082/api/motoristas\"");
        }
    }

    record Corpo(@NotBlank(message = "obrigatório") String nome) {
    }
}
