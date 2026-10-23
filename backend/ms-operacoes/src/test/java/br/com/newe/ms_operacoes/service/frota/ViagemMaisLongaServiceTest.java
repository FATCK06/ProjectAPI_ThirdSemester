package br.com.newe.ms_operacoes.service.frota;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.newe.ms_operacoes.client.FrotaClient;
import br.com.newe.ms_operacoes.repository.ViagemRepository;
import br.com.newe.ms_operacoes.repository.ViagemRepository.ViagemKmView;
import br.com.newe.ms_operacoes.service.frota.ViagemMaisLongaService.ViagemMaisLonga;

class ViagemMaisLongaServiceTest {

    private static final String MES = "2026-04";
    private static final UUID MOTORISTA = UUID.randomUUID();
    private static final UUID VUC = UUID.randomUUID();
    private static final UUID TRUCK = UUID.randomUUID();

    // Km chegada vem sempre 0 no manifesto: a conta antiga do ranking mensal
    // (chegada - saida) dava negativo e escolhia a viagem MAIS CURTA.
    @Test
    void distanciaEOKmSaidaENaoChegadaMenosSaida() {
        ViagemMaisLonga v = calcular(viagem(VUC, 80, 0), viagem(TRUCK, 480, 0));

        assertThat(v.tipoVeiculo()).isEqualTo("Truck");
        assertThat(v.distanciaKm()).isEqualTo(480);
    }

    @Test
    void empateFicaComAMaisRecente() {
        // A consulta devolve da mais recente para a mais antiga.
        ViagemMaisLonga v = calcular(viagem(VUC, 300, 0), viagem(TRUCK, 300, 0));

        assertThat(v.tipoVeiculo()).isEqualTo("VUC");
    }

    @Test
    void semKmInformadoFicaComAMaisRecenteESemDistancia() {
        ViagemMaisLonga v = calcular(viagem(VUC, 0, 0), viagem(TRUCK, null, null));

        assertThat(v.tipoVeiculo()).isEqualTo("VUC");
        assertThat(v.distanciaKm()).isNull();
    }

    @Test
    void viagemComKmVenceAsSemKm() {
        ViagemMaisLonga v = calcular(viagem(VUC, 0, 0), viagem(TRUCK, 120, 0));

        assertThat(v.tipoVeiculo()).isEqualTo("Truck");
    }

    @Test
    void veiculoForaDoCadastroFicaNaoInformado() {
        ViagemMaisLonga v = calcular(viagem(UUID.randomUUID(), 200, 0));

        assertThat(v.tipoVeiculo()).isEqualTo(ViagemMaisLongaService.TIPO_NAO_INFORMADO);
    }

    private static ViagemMaisLonga calcular(ViagemKmView... viagens) {
        ViagemRepository repository = mock(ViagemRepository.class);
        FrotaClient frotaClient = mock(FrotaClient.class);
        when(repository.kmViagensPorMotoristas(eq(MES), any())).thenReturn(List.of(viagens));
        when(frotaClient.buscarTiposVeiculo(any())).thenReturn(Map.of(VUC, "VUC", TRUCK, "Truck"));

        return new ViagemMaisLongaService(repository, frotaClient).porMotorista(MES, List.of(MOTORISTA)).get(MOTORISTA);
    }

    private static ViagemKmView viagem(UUID veiculoId, Integer kmSaida, Integer kmChegada) {
        return new ViagemKmView() {
            public UUID getMotoristaId() { return MOTORISTA; }
            public UUID getVeiculoId() { return veiculoId; }
            public Integer getKmSaida() { return kmSaida; }
            public Integer getKmChegada() { return kmChegada; }
        };
    }
}
