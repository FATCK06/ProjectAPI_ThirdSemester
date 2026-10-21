package br.com.newe.ms_operacoes.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import br.com.newe.ms_operacoes.client.FrotaClient;
import br.com.newe.ms_operacoes.controller.frota.DashboardFrotaController;
import br.com.newe.ms_operacoes.models.Viagem;
import br.com.newe.ms_operacoes.repository.ViagemRepository;
import br.com.newe.ms_operacoes.security.PerfilResolver;
import br.com.newe.ms_operacoes.service.indicadores.IndicadoresPorModelo;
import br.com.newe.ms_operacoes.service.indicadores.IndicadoresService;
import br.com.newe.ms_operacoes.service.frota.MotoristasMesService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

// Os endpoints que nao passam por semValoresFinanceiros ate aqui devolviam frete,
// custo e rentabilidade ao operador.
class ValoresFinanceirosPorPerfilTest {

    private static final String MES = "2026-04";

    private final ViagemRepository repository = mock(ViagemRepository.class);
    private final IndicadoresService indicadoresService = mock(IndicadoresService.class);
    private final PerfilResolver perfilResolver = new PerfilResolver();

    @Test
    void porModeloSemValoresParaOperador() {
        when(indicadoresService.calcularPorModelo(MES)).thenReturn(List.of(new IndicadoresPorModelo(
                "Truck", 2, 10, 8, new BigDecimal("13.33"),
                new BigDecimal("10000"), new BigDecimal("6000"), new BigDecimal("4000"), new BigDecimal("40.00"))));

        IndicadoresPorModelo operador = dashboard().indicadoresPorModelo(MES, comPerfil("OPERADOR")).getBody().get(0);
        IndicadoresPorModelo gestor = dashboard().indicadoresPorModelo(MES, comPerfil("GESTOR")).getBody().get(0);

        assertThat(operador.numeroViagens()).isEqualTo(10);
        assertThat(operador.valorFrete()).isNull();
        assertThat(operador.custoTotal()).isNull();
        assertThat(operador.rentabilidade()).isNull();
        assertThat(operador.margem()).isNull();
        assertThat(gestor.rentabilidade()).isEqualByComparingTo("4000");
    }

    @Test
    void viagensSemValoresParaOperador() {
        when(repository.findByMesReferencia(MES)).thenReturn(List.of(viagem()));

        JsonNode operador = (JsonNode) viagens().listarPorMesReferencia(MES, comPerfil("OPERADOR")).getBody();

        assertThat(operador.get(0).get("idManifesto").asInt()).isEqualTo(16882);
        for (String campo : ViagemController.CAMPOS_FINANCEIROS) {
            assertThat(operador.get(0).has(campo)).as(campo).isFalse();
        }
    }

    @Test
    void viagensComValoresParaGestor() {
        when(repository.findByMesReferencia(MES)).thenReturn(List.of(viagem()));

        List<?> gestor = (List<?>) viagens().listarPorMesReferencia(MES, comPerfil("GESTOR")).getBody();

        assertThat(((Viagem) gestor.get(0)).getValorFretes()).isEqualByComparingTo("1226.21");
    }

    private DashboardFrotaController dashboard() {
        return new DashboardFrotaController(repository, mock(FrotaClient.class), indicadoresService,
                mock(MotoristasMesService.class), perfilResolver);
    }

    private ViagemController viagens() {
        return new ViagemController(repository, perfilResolver, JsonMapper.builder().build());
    }

    private static MockHttpServletRequest comPerfil(String perfil) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(PerfilResolver.HEADER_PERFIL, perfil);
        return request;
    }

    private static Viagem viagem() {
        Viagem v = new Viagem();
        v.setIdManifesto(16882);
        v.setDataViagem(LocalDate.of(2026, 4, 1));
        v.setValorFrete(new BigDecimal("335.80"));
        v.setValorFretes(new BigDecimal("1226.21"));
        v.setValeFrete(new BigDecimal("430.75"));
        v.setValorNf(new BigDecimal("5000.00"));
        v.setTotalDespesas(BigDecimal.ZERO);
        v.setSaldoDespesas(BigDecimal.ZERO);
        v.setSaldoAPagar(new BigDecimal("258.45"));
        return v;
    }
}
