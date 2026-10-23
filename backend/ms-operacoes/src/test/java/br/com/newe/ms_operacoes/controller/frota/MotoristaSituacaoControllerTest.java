package br.com.newe.ms_operacoes.controller.frota;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.unit.DataSize;

import br.com.newe.ms_operacoes.dto.frota.SituacaoMotoristaDTO;
import br.com.newe.ms_operacoes.erro.DadoInvalidoException;
import br.com.newe.ms_operacoes.erro.ErroHandlerGlobal;
import br.com.newe.ms_operacoes.erro.RespostaErro;
import br.com.newe.ms_operacoes.models.enums.frota.SituacaoMotorista;
import br.com.newe.ms_operacoes.service.frota.SituacaoMotoristaService;

// Sem MockMvc: chama o metodo do controller direto, com o service mockado.
class MotoristaSituacaoControllerTest {

    private static final String MES = "2026-09";

    private SituacaoMotoristaService service;
    private MotoristaSituacaoController controller;

    @BeforeEach
    void setUp() {
        service = mock(SituacaoMotoristaService.class);
        when(service.listar(anyString(), any())).thenReturn(List.of());
        controller = new MotoristaSituacaoController(service);
    }

    @Test
    void semFiltroListaTodas() {
        ResponseEntity<List<SituacaoMotoristaDTO>> resposta = controller.situacao(MES, null);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(service).listar(MES, null);
    }

    @Test
    void filtroVazioListaTodas() {
        assertThat(controller.situacao(MES, "  ").getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(service).listar(MES, null);
    }

    @Test
    void cadaSituacaoChegaAoService() {
        for (SituacaoMotorista s : SituacaoMotorista.values()) {
            assertThat(controller.situacao(MES, s.name()).getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(service).listar(MES, s);
        }
    }

    @Test
    void filtroNaoDiferenciaCaixa() {
        assertThat(controller.situacao(MES, "disponivel").getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(controller.situacao(MES, " Sem_Dias_Disponiveis ").getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(service).listar(MES, SituacaoMotorista.DISPONIVEL);
        verify(service).listar(MES, SituacaoMotorista.SEM_DIAS_DISPONIVEIS);
    }

    @Test
    void situacaoDesconhecidaERecusada() {
        assertThatThrownBy(() -> controller.situacao(MES, "OCUPADO"))
                .isInstanceOf(DadoInvalidoException.class);
        verifyNoInteractions(service);
    }

    // A anotacao @MesReferencia so roda dentro do Spring MVC: aqui precisa de MockMvc.
    @Test
    void mesInvalidoDa400ComOCampo() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ErroHandlerGlobal(DataSize.ofMegabytes(10)))
                .build();

        mvc.perform(get("/api/dashboard/motoristas/situacao").param("mesReferencia", "2026-13"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value(RespostaErro.DADOS_INVALIDOS))
                .andExpect(jsonPath("$.campos.mesReferencia").value("use o formato AAAA-MM"));
        verifyNoInteractions(service);
    }
}
