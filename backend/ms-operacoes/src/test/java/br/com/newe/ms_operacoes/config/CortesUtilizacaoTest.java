package br.com.newe.ms_operacoes.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import br.com.newe.ms_operacoes.models.enums.FaixaUtilizacao;
import br.com.newe.ms_operacoes.service.indicadores.ClassificadorSituacao;

class CortesUtilizacaoTest {

    private final ApplicationContextRunner contexto = new ApplicationContextRunner()
            .withUserConfiguration(SituacaoUtilizacaoConfig.class);

    // Valores do situacao-utilizacao.properties, sem nada sobrescrevendo.
    @Test
    void valoresDoArquivoChegamAoClassificador() {
        contexto.run(ctx -> {
            CortesUtilizacao cortes = ctx.getBean(CortesUtilizacao.class);
            assertThat(cortes.corteBaixa()).isEqualByComparingTo("50");
            assertThat(cortes.corteAlta()).isEqualByComparingTo("80");

            ClassificadorSituacao classificador = new ClassificadorSituacao(cortes.corteBaixa(), cortes.corteAlta());
            assertThat(classificador.classificar(4, 10).faixa()).isEqualTo(FaixaUtilizacao.BAIXA);
            assertThat(classificador.classificar(5, 10).faixa()).isEqualTo(FaixaUtilizacao.INTERMEDIARIA);
            assertThat(classificador.classificar(8, 10).faixa()).isEqualTo(FaixaUtilizacao.ALTA);
        });
    }

    // Fonte de maior precedencia (como variavel de ambiente) vence o arquivo.
    @Test
    void propriedadeExternaSobrescreveOArquivo() {
        contexto.withPropertyValues("situacao.utilizacao.corte-alta=90").run(ctx -> {
            CortesUtilizacao cortes = ctx.getBean(CortesUtilizacao.class);
            assertThat(cortes.corteBaixa()).isEqualByComparingTo("50");
            assertThat(cortes.corteAlta()).isEqualByComparingTo("90");
        });
    }

    @Test
    void corteNegativoDerrubaASubida() {
        contexto.withPropertyValues("situacao.utilizacao.corte-baixa=-1").run(ctx -> {
            assertThat(ctx).hasFailed();
            assertThat(ctx.getStartupFailure()).rootCause()
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("situacao.utilizacao.corte-baixa");
        });
    }

    @Test
    void corteBaixoMaiorQueAltoDerrubaASubida() {
        contexto.withPropertyValues("situacao.utilizacao.corte-baixa=85").run(ctx -> {
            assertThat(ctx).hasFailed();
            assertThat(ctx.getStartupFailure()).rootCause()
                    .hasMessageContaining("situacao.utilizacao.corte-baixa")
                    .hasMessageContaining("situacao.utilizacao.corte-alta");
        });
    }

    @Test
    void corteVazioDerrubaASubida() {
        contexto.withPropertyValues("situacao.utilizacao.corte-alta=").run(ctx -> {
            assertThat(ctx).hasFailed();
            assertThat(ctx.getStartupFailure()).rootCause()
                    .hasMessageContaining("situacao.utilizacao.corte-alta");
        });
    }

    @Test
    void corteNuloERecusado() {
        assertThatThrownBy(() -> new CortesUtilizacao(null, new BigDecimal("80")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("situacao.utilizacao.corte-baixa");
        assertThatThrownBy(() -> new CortesUtilizacao(new BigDecimal("50"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("situacao.utilizacao.corte-alta");
    }

    @Test
    void corteNegativoERecusado() {
        assertThatThrownBy(() -> new CortesUtilizacao(new BigDecimal("50"), new BigDecimal("-0.01")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("situacao.utilizacao.corte-alta");
    }

    @Test
    void corteBaixoMaiorQueAltoERecusado() {
        assertThatThrownBy(() -> new CortesUtilizacao(new BigDecimal("80"), new BigDecimal("50")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("situacao.utilizacao.corte-baixa");
    }
}
