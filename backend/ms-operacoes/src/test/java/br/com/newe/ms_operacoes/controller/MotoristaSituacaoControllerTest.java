package br.com.newe.ms_operacoes.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import br.com.newe.ms_operacoes.dto.SituacaoMotoristaDTO;
import br.com.newe.ms_operacoes.models.enums.SituacaoMotorista;
import br.com.newe.ms_operacoes.service.indicadores.SituacaoMotoristaService;

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
    void situacaoDesconhecidaDa400() {
        assertThat(controller.situacao(MES, "OCUPADO").getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(service);
    }

    @Test
    void mesInvalidoDa400() {
        assertThat(controller.situacao("2026-13", null).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(service);
    }
}
