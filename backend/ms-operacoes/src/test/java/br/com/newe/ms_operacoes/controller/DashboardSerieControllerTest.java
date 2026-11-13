package br.com.newe.ms_operacoes.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.unit.DataSize;

import br.com.newe.ms_operacoes.client.FrotaClient;
import br.com.newe.ms_operacoes.erro.ErroHandlerGlobal;
import br.com.newe.ms_operacoes.erro.RespostaErro;
import br.com.newe.ms_operacoes.repository.ViagemRepository;
import br.com.newe.ms_operacoes.repository.ViagemRepository.SomaMotoristaMesView;
import br.com.newe.ms_operacoes.security.PerfilResolver;
import br.com.newe.ms_operacoes.service.indicadores.IndicadoresService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class DashboardSerieControllerTest {

    private static final String ROTA = "/api/dashboard/indicadores/serie";
    private static final List<String> FINANCEIROS = List.of("valorFrete", "custoTotal", "rentabilidade");

    private final ViagemRepository repository = mock(ViagemRepository.class);
    private final DashboardController controller = new DashboardController(
            new IndicadoresService(repository, mock(FrotaClient.class)), new PerfilResolver());

    @Test
    void operadorNaoRecebeCamposFinanceirosNoJson() {
        dadoMarcoEAbrilComDados();

        JsonNode abril = json(List.of("2026-03", "2026-04"), comPerfil("OPERADOR")).get(1);

        assertThat(abril.get("totais").get("numeroViagens").asInt()).isEqualTo(12);
        assertThat(abril.get("totais").has("utilizacaoMedia")).isTrue();
        for (String campo : FINANCEIROS) {
            assertThat(abril.get("totais").has(campo)).as(campo).isFalse();
        }
    }

    @Test
    void perfilAusenteNaoRecebeCamposFinanceiros() {
        dadoMarcoEAbrilComDados();

        JsonNode abril = json(List.of("2026-03", "2026-04"), new MockHttpServletRequest()).get(1);

        assertThat(abril.get("totais").has("valorFrete")).isFalse();
    }

    @Test
    void gestorRecebeCamposFinanceirosNoJson() {
        dadoMarcoEAbrilComDados();

        JsonNode abril = json(List.of("2026-03", "2026-04"), comPerfil("GESTOR")).get(1);

        for (String campo : FINANCEIROS) {
            assertThat(abril.get("totais").has(campo)).as(campo).isTrue();
        }
        assertThat(abril.get("totais").get("rentabilidade").decimalValue()).isEqualByComparingTo("150.00");
    }

    @Test
    void operadorNaoRecebeVariacoesFinanceirasNoJson() {
        dadoMarcoEAbrilComDados();

        JsonNode variacoes = json(List.of("2026-03", "2026-04"), comPerfil("OPERADOR")).get(1).get("variacoes");

        assertThat(variacoes.get("numeroViagens").get("percentual").decimalValue()).isEqualByComparingTo("20.00");
        assertThat(variacoes.has("utilizacaoMedia")).isTrue();
        for (String campo : FINANCEIROS) {
            assertThat(variacoes.has(campo)).as(campo).isFalse();
        }
    }

    @Test
    void gestorRecebeVariacoesFinanceirasNoJson() {
        dadoMarcoEAbrilComDados();

        JsonNode abril = json(List.of("2026-03", "2026-04"), comPerfil("GESTOR")).get(1);

        assertThat(abril.get("mesAnterior").asString()).isEqualTo("2026-03");
        assertThat(abril.get("mesAnteriorSemDados").asBoolean()).isFalse();
        for (String campo : FINANCEIROS) {
            assertThat(abril.get("variacoes").has(campo)).as(campo).isTrue();
        }
        // 400 -> 150
        assertThat(abril.get("variacoes").get("rentabilidade").get("diferenca").decimalValue())
                .isEqualByComparingTo("-250.00");
    }

    @Test
    void anteriorZeroNaoTemPercentualNoJson() {
        when(repository.somasPorMotoristaNosMeses(anyCollection())).thenReturn(List.of(
                soma("2026-03", 10, "0.00", "0.00"),
                soma("2026-04", 12, "750.00", "600.00")));

        JsonNode frete = json(List.of("2026-03", "2026-04"), comPerfil("GESTOR")).get(1)
                .get("variacoes").get("valorFrete");

        assertThat(frete.get("diferenca").decimalValue()).isEqualByComparingTo("750.00");
        assertThat(frete.has("percentual")).isFalse();
    }

    @Test
    void anteriorSemDadosTemVariacoesVaziasNoJson() {
        dadoMarcoEAbrilComDados();

        JsonNode marco = json(List.of("2026-03", "2026-04"), comPerfil("GESTOR")).get(0);

        assertThat(marco.get("mesAnterior").asString()).isEqualTo("2026-02");
        assertThat(marco.get("mesAnteriorSemDados").asBoolean()).isTrue();
        assertThat(marco.get("variacoes").isEmpty()).isTrue();
    }

    @Test
    void mesSemDadosNaoTemNenhumNumeroNoJson() {
        dadoMarcoEAbrilComDados();

        JsonNode maio = json(List.of("2026-04", "2026-05"), comPerfil("GESTOR")).get(1);

        assertThat(maio.get("semDados").asBoolean()).isTrue();
        assertThat(maio.get("mensagem").asString()).isEqualTo("Sem dados para 05/2026.");
        assertThat(maio.has("totais")).isFalse();
    }

    @Test
    void mesComDadosNaoTemMensagemNoJson() {
        dadoMarcoEAbrilComDados();

        JsonNode abril = json(List.of("2026-03", "2026-04"), comPerfil("GESTOR")).get(1);

        assertThat(abril.get("semDados").asBoolean()).isFalse();
        assertThat(abril.has("mensagem")).isFalse();
    }

    @Test
    void mesesImportadosEmOrdemCrescente() throws Exception {
        when(repository.buscarMesesImportados()).thenReturn(List.of("2026-03", "2026-04"));

        mvc().perform(get("/api/dashboard/indicadores/meses-importados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("2026-03"))
                .andExpect(jsonPath("$[1]").value("2026-04"));
    }

    @Test
    void umMesSoDevolve400ComAMensagemExata() throws Exception {
        mvc().perform(get(ROTA).param("meses", "2026-04"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value(RespostaErro.DADOS_INVALIDOS))
                .andExpect(jsonPath("$.campos.meses").value("Selecione ao menos dois meses para comparar."));
    }

    @Test
    void semOParametroDevolveAMesmaMensagem() throws Exception {
        mvc().perform(get(ROTA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.meses").value("Selecione ao menos dois meses para comparar."));
    }

    @Test
    void mesForaDoFormatoDevolve400NaoErroInterno() throws Exception {
        mvc().perform(get(ROTA).param("meses", "2026-04", "2026-13"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.meses").value("use o formato AAAA-MM"));
    }

    @Test
    void parametroRepetidoChegaComoListaNaRota() throws Exception {
        dadoMarcoEAbrilComDados();

        mvc().perform(get(ROTA).param("meses", "2026-04", "2026-03")
                .header(PerfilResolver.HEADER_PERFIL, "OPERADOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mesReferencia").value("2026-03"))
                .andExpect(jsonPath("$[1].totais.numeroViagens").value(12))
                .andExpect(jsonPath("$[1].totais.valorFrete").doesNotExist());
    }

    private MockMvc mvc() {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ErroHandlerGlobal(DataSize.ofMegabytes(10)))
                .build();
    }

    private JsonNode json(List<String> meses, MockHttpServletRequest request) {
        return JsonMapper.builder().build().valueToTree(controller.serie(meses, request).getBody());
    }

    private void dadoMarcoEAbrilComDados() {
        when(repository.somasPorMotoristaNosMeses(anyCollection())).thenReturn(List.of(
                soma("2026-03", 10, "1000.00", "600.00"),
                soma("2026-04", 12, "750.00", "600.00")));
    }

    private static MockHttpServletRequest comPerfil(String perfil) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(PerfilResolver.HEADER_PERFIL, perfil);
        return request;
    }

    private static SomaMotoristaMesView soma(String mes, long viagens, String frete, String custo) {
        UUID motoristaId = UUID.randomUUID();
        return new SomaMotoristaMesView() {
            public String getMesReferencia() { return mes; }
            public UUID getMotoristaId() { return motoristaId; }
            public Long getNumeroViagens() { return viagens; }
            public Long getDiasOperacao() { return 3L; }
            public Long getViagensApuradas() { return viagens; }
            public BigDecimal getValorFrete() { return new BigDecimal(frete); }
            public BigDecimal getCustoTotal() { return new BigDecimal(custo); }
        };
    }
}
