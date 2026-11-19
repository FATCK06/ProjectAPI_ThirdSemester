package br.com.newe.ms_operacoes.service.frota;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.newe.ms_operacoes.client.FrotaClient;
import br.com.newe.ms_operacoes.dto.ResultadoTratamentoVeiculosDTO;
import br.com.newe.ms_operacoes.dto.VeiculoClassificacao;
import br.com.newe.ms_operacoes.repository.ViagemRepository;
import br.com.newe.ms_operacoes.repository.ViagemRepository.ManifestoVeiculoView;

class TratamentoVeiculosServiceTest {

    private final ViagemRepository repository = mock(ViagemRepository.class);
    private final FrotaClient frotaClient = mock(FrotaClient.class);
    private final TratamentoVeiculosService service = new TratamentoVeiculosService(repository, frotaClient);

    private record Manifesto(Integer getManifesto, UUID getVeiculoId, LocalDate getData) implements ManifestoVeiculoView {
    }

    @Test
    void separaComCategoriaSemCategoriaENaoIdentificado() {
        UUID fiorino = UUID.randomUUID();
        UUID carro1 = UUID.randomUUID();
        UUID carro2 = UUID.randomUUID();
        UUID soManifesto = UUID.randomUUID();
        UUID sumiu = UUID.randomUUID();
        when(repository.buscarIdsVeiculos()).thenReturn(List.of(fiorino, carro1, carro2, soManifesto, sumiu));
        when(frotaClient.classificarVeiculos(any())).thenReturn(List.of(
                new VeiculoClassificacao(fiorino, "AAA1A11", "FIORINO", "Fiorino"),
                new VeiculoClassificacao(carro1, "CCC3C33", "Automóveis", null),
                new VeiculoClassificacao(carro2, "BBB2B22", "Automóveis", null),
                new VeiculoClassificacao(soManifesto, "DDD4D44", null, null)));
        when(repository.buscarManifestosPorVeiculos(List.of(soManifesto, sumiu))).thenReturn(List.of(
                new Manifesto(101, soManifesto, LocalDate.of(2026, 7, 2)),
                new Manifesto(102, sumiu, LocalDate.of(2026, 7, 1))));

        ResultadoTratamentoVeiculosDTO r = service.resultado();

        assertThat(r.veiculosEmManifestos()).isEqualTo(5);
        assertThat(r.veiculosComCategoria()).isEqualTo(1);
        assertThat(r.tiposSemCategoria()).containsExactly(
                new ResultadoTratamentoVeiculosDTO.TipoSemCategoria("Automóveis", 2, List.of("BBB2B22", "CCC3C33")));
        assertThat(r.manifestosSemVeiculo()).containsExactly(
                new ResultadoTratamentoVeiculosDTO.ManifestoSemVeiculo(101, "DDD4D44", LocalDate.of(2026, 7, 2)),
                new ResultadoTratamentoVeiculosDTO.ManifestoSemVeiculo(102, null, LocalDate.of(2026, 7, 1)));
    }

    @Test
    void semViagensNaoChamaOFrota() {
        when(repository.buscarIdsVeiculos()).thenReturn(List.of());

        ResultadoTratamentoVeiculosDTO r = service.resultado();

        assertThat(r.veiculosEmManifestos()).isZero();
        assertThat(r.tiposSemCategoria()).isEmpty();
        verify(frotaClient, never()).classificarVeiculos(any());
    }
}
