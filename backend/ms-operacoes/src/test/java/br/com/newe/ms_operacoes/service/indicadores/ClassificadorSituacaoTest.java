package br.com.newe.ms_operacoes.service.indicadores;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import br.com.newe.ms_operacoes.models.enums.FaixaUtilizacao;
import br.com.newe.ms_operacoes.models.enums.SituacaoMotorista;
import br.com.newe.ms_operacoes.service.indicadores.ClassificadorSituacao.Classificacao;

class ClassificadorSituacaoTest {

    // Mesmos cortes provisorios do application.properties.
    private final ClassificadorSituacao classificador = new ClassificadorSituacao(
            new BigDecimal("40"), new BigDecimal("70"));

    @Test
    void utilizacaoBaixaFicaDisponivel() {
        Classificacao c = classificador.classificar(3, 10);

        assertThat(c.utilizacao()).isEqualByComparingTo("30.00");
        assertThat(c.faixa()).isEqualTo(FaixaUtilizacao.BAIXA);
        assertThat(c.situacao()).isEqualTo(SituacaoMotorista.DISPONIVEL);
    }

    @Test
    void utilizacaoIntermediariaFicaDisponivel() {
        Classificacao c = classificador.classificar(5, 10);

        assertThat(c.utilizacao()).isEqualByComparingTo("50.00");
        assertThat(c.faixa()).isEqualTo(FaixaUtilizacao.INTERMEDIARIA);
        assertThat(c.situacao()).isEqualTo(SituacaoMotorista.DISPONIVEL);
    }

    @Test
    void utilizacaoAltaFicaIndisponivel() {
        Classificacao c = classificador.classificar(9, 10);

        assertThat(c.utilizacao()).isEqualByComparingTo("90.00");
        assertThat(c.faixa()).isEqualTo(FaixaUtilizacao.ALTA);
        assertThat(c.situacao()).isEqualTo(SituacaoMotorista.INDISPONIVEL);
    }

    @Test
    void exatamenteNoCorteBaixoEIntermediaria() {
        Classificacao c = classificador.classificar(4, 10);

        assertThat(c.utilizacao()).isEqualByComparingTo("40.00");
        assertThat(c.faixa()).isEqualTo(FaixaUtilizacao.INTERMEDIARIA);
    }

    @Test
    void exatamenteNoCorteAltoEAlta() {
        Classificacao c = classificador.classificar(7, 10);

        assertThat(c.utilizacao()).isEqualByComparingTo("70.00");
        assertThat(c.faixa()).isEqualTo(FaixaUtilizacao.ALTA);
    }

    @Test
    void logoAbaixoDoCorteBaixoEBaixa() {
        assertThat(classificador.faixaDa(new BigDecimal("39.99"))).isEqualTo(FaixaUtilizacao.BAIXA);
    }

    @Test
    void logoAbaixoDoCorteAltoEIntermediaria() {
        assertThat(classificador.faixaDa(new BigDecimal("69.99"))).isEqualTo(FaixaUtilizacao.INTERMEDIARIA);
    }

    // 2 casas, HALF_UP: 2/3 = 66.666... -> 66.67.
    @Test
    void utilizacaoArredondaComDuasCasas() {
        assertThat(classificador.classificar(2, 3).utilizacao()).isEqualByComparingTo("66.67");
    }

    @Test
    void semDiasDisponiveisNaoTemFaixaNemUtilizacao() {
        Classificacao c = classificador.classificar(5, 0);

        assertThat(c.utilizacao()).isNull();
        assertThat(c.faixa()).isNull();
        assertThat(c.situacao()).isEqualTo(SituacaoMotorista.SEM_DIAS_DISPONIVEIS);
    }

    @Test
    void diasDisponiveisNegativoTambemFicaSemDias() {
        assertThat(classificador.classificar(5, -1).situacao()).isEqualTo(SituacaoMotorista.SEM_DIAS_DISPONIVEIS);
    }

    // Acima de 100% nao e truncado.
    @Test
    void operacaoAcimaDosDiasDisponiveisEAltaSemTruncar() {
        Classificacao c = classificador.classificar(12, 10);

        assertThat(c.utilizacao()).isEqualByComparingTo("120.00");
        assertThat(c.faixa()).isEqualTo(FaixaUtilizacao.ALTA);
        assertThat(c.situacao()).isEqualTo(SituacaoMotorista.INDISPONIVEL);
    }

    @Test
    void semDiaDeOperacaoEBaixaDisponivel() {
        Classificacao c = classificador.classificar(0, 30);

        assertThat(c.utilizacao()).isEqualByComparingTo("0");
        assertThat(c.faixa()).isEqualTo(FaixaUtilizacao.BAIXA);
        assertThat(c.situacao()).isEqualTo(SituacaoMotorista.DISPONIVEL);
    }

    @Test
    void mapeamentoFaixaParaSituacao() {
        assertThat(ClassificadorSituacao.situacaoDa(FaixaUtilizacao.ALTA)).isEqualTo(SituacaoMotorista.INDISPONIVEL);
        assertThat(ClassificadorSituacao.situacaoDa(FaixaUtilizacao.INTERMEDIARIA))
                .isEqualTo(SituacaoMotorista.DISPONIVEL);
        assertThat(ClassificadorSituacao.situacaoDa(FaixaUtilizacao.BAIXA)).isEqualTo(SituacaoMotorista.DISPONIVEL);
    }

    @Test
    void cortesForaDeOrdemSaoRecusados() {
        assertThatThrownBy(() -> new ClassificadorSituacao(new BigDecimal("70"), new BigDecimal("40")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
