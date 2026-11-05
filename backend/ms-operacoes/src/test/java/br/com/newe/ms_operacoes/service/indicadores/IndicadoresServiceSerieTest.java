package br.com.newe.ms_operacoes.service.indicadores;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import br.com.newe.ms_operacoes.client.FrotaClient;
import br.com.newe.ms_operacoes.erro.DadoInvalidoException;
import br.com.newe.ms_operacoes.repository.ViagemRepository;
import br.com.newe.ms_operacoes.repository.ViagemRepository.SomaMotoristaMesView;

class IndicadoresServiceSerieTest {

    private final ViagemRepository viagemRepository = mock(ViagemRepository.class);
    private final FrotaClient frotaClient = mock(FrotaClient.class);
    private final IndicadoresService service = new IndicadoresService(viagemRepository, frotaClient);

    @Test
    void mesSemViagemFicaSemDadosENuncaZerado() {
        dadoAsSomas(soma("2026-04", 10, 6, "1000.00", "600.00"));

        SerieMes maio = service.serie(List.of("2026-04", "2026-05")).get(1);

        assertThat(maio.mesReferencia()).isEqualTo("2026-05");
        assertThat(maio.semDados()).isTrue();
        assertThat(maio.mensagem()).isEqualTo("Sem dados para 05/2026.");
        assertThat(maio.totais()).isNull();
    }

    @Test
    void somaTodosOsMotoristasDoMes() {
        dadoAsSomas(
                soma("2026-04", 10, 6, "1000.00", "600.00"),
                soma("2026-04", 5, 3, "500.00", "400.00"),
                soma("2026-05", 1, 1, "1.00", "1.00"));

        SerieMes abril = service.serie(List.of("2026-04", "2026-05")).get(0);

        assertThat(abril.semDados()).isFalse();
        assertThat(abril.mensagem()).isNull();
        assertThat(abril.totais().numeroViagens()).isEqualTo(15);
        // 9 motorista-dia / (30 dias x 2 motoristas)
        assertThat(abril.totais().utilizacaoMedia()).isEqualByComparingTo("15.00");
        assertThat(abril.totais().valorFrete()).isEqualByComparingTo("1500.00");
        assertThat(abril.totais().custoTotal()).isEqualByComparingTo("1000.00");
        assertThat(abril.totais().rentabilidade()).isEqualByComparingTo("500.00");
    }

    @Test
    void ordenaEDeduplicaOsMeses() {
        dadoAsSomas();

        assertThat(service.serie(List.of("2026-06", "2026-01", "2026-06", "2025-12")))
                .extracting(SerieMes::mesReferencia)
                .containsExactly("2025-12", "2026-01", "2026-06");
    }

    @Test
    void fazUmaConsultaSoComOsMesesPedidosESemFrota() {
        dadoAsSomas();

        service.serie(List.of("2026-06", "2026-04", "2026-04"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<String>> meses = ArgumentCaptor.forClass(Collection.class);
        verify(viagemRepository).somasPorMotoristaNosMeses(meses.capture());
        verifyNoMoreInteractions(viagemRepository);
        assertThat(meses.getValue()).containsExactly("2026-04", "2026-06");
        verifyNoInteractions(frotaClient);
    }

    @Test
    void menosDeDoisMesesEInvalidoComAMensagemExata() {
        for (List<String> meses : Arrays.asList(null, List.<String>of(), List.of("2026-04"),
                List.of("2026-04", "2026-04"))) {
            assertThatThrownBy(() -> service.serie(meses))
                    .isInstanceOfSatisfying(DadoInvalidoException.class, e -> {
                        assertThat(e.getCampo()).isEqualTo("meses");
                        assertThat(e.getMotivo()).isEqualTo("Selecione ao menos dois meses para comparar.");
                    });
        }
        verifyNoInteractions(viagemRepository);
    }

    @Test
    void maisDeDozeMesesEInvalido() {
        List<String> treze = List.of("2025-01", "2025-02", "2025-03", "2025-04", "2025-05", "2025-06", "2025-07",
                "2025-08", "2025-09", "2025-10", "2025-11", "2025-12", "2026-01");

        assertThatThrownBy(() -> service.serie(treze))
                .isInstanceOfSatisfying(DadoInvalidoException.class,
                        e -> assertThat(e.getMotivo()).isEqualTo("de 2 a 12"));
        verifyNoInteractions(viagemRepository);
    }

    @Test
    void dozeMesesSaoAceitos() {
        dadoAsSomas();
        List<String> doze = List.of("2025-01", "2025-02", "2025-03", "2025-04", "2025-05", "2025-06", "2025-07",
                "2025-08", "2025-09", "2025-10", "2025-11", "2025-12");

        assertThat(service.serie(doze)).hasSize(12);
    }

    @Test
    void mesesImportadosIgnoraNulo() {
        when(viagemRepository.buscarMesesImportados()).thenReturn(Arrays.asList("2026-04", "2026-05", null));

        assertThat(service.mesesImportados()).containsExactly("2026-04", "2026-05");
    }

    private void dadoAsSomas(SomaMotoristaMesView... somas) {
        when(viagemRepository.somasPorMotoristaNosMeses(anyCollection())).thenReturn(List.of(somas));
    }

    private static SomaMotoristaMesView soma(String mes, long viagens, long dias, String frete, String custo) {
        UUID motoristaId = UUID.randomUUID();
        return new SomaMotoristaMesView() {
            public String getMesReferencia() { return mes; }
            public UUID getMotoristaId() { return motoristaId; }
            public Long getNumeroViagens() { return viagens; }
            public Long getDiasOperacao() { return dias; }
            public Long getViagensApuradas() { return viagens; }
            public BigDecimal getValorFrete() { return new BigDecimal(frete); }
            public BigDecimal getCustoTotal() { return new BigDecimal(custo); }
        };
    }
}
