package br.com.newe.ms_operacoes.service.indicadores;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.newe.ms_operacoes.client.FrotaClient;
import br.com.newe.ms_operacoes.repository.ViagemRepository;
import br.com.newe.ms_operacoes.repository.ViagemRepository.SomaMotoristaMesView;

class IndicadoresServiceHistoricoTest {

    private final ViagemRepository viagemRepository = mock(ViagemRepository.class);
    private final FrotaClient frotaClient = mock(FrotaClient.class);
    private final IndicadoresService service = new IndicadoresService(viagemRepository, frotaClient);

    @Test
    void devolveTodosOsMesesEmOrdemComMesVazioZerado() {
        when(viagemRepository.somasPorMotoristaNosMeses(anyCollection())).thenReturn(List.of(
                soma("2026-04", 10, 4, "1000.00", "600.00"),
                soma("2026-04", 5, 2, "500.00", "400.00"),
                soma("2026-06", 8, 3, "800.00", "500.00")));

        List<HistoricoMes> historico = service.historico("2026-06", 3);

        assertThat(historico).extracting(HistoricoMes::mesReferencia)
                .containsExactly("2026-04", "2026-05", "2026-06");

        IndicadoresMes.Totais abril = historico.get(0).totais();
        assertThat(abril.motoristas()).isEqualTo(2);
        assertThat(abril.numeroViagens()).isEqualTo(15);
        assertThat(abril.diasOperacao()).isEqualTo(6);
        assertThat(abril.valorFrete()).isEqualByComparingTo("1500.00");
        assertThat(abril.rentabilidade()).isEqualByComparingTo("500.00");

        IndicadoresMes.Totais maio = historico.get(1).totais();
        assertThat(maio.motoristas()).isZero();
        assertThat(maio.numeroViagens()).isZero();
        assertThat(maio.valorFrete()).isEqualByComparingTo("0");

        // Totais nao usam nome: o historico nao precisa do ms-frota
        verifyNoInteractions(frotaClient);
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
