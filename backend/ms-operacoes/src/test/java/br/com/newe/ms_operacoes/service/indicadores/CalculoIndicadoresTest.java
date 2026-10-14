package br.com.newe.ms_operacoes.service.indicadores;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.newe.ms_operacoes.repository.ViagemRepository.SomaMotoristaView;

class CalculoIndicadoresTest {

    // Mesmo cenario do teste do antigo ms-indicadores: 20 de 25 dias, 10 viagens, 10k - 6k.
    @Test
    void calculaIndicadoresDoMotorista() {
        IndicadoresMotorista m = IndicadoresService.indicadoresDe(
                soma(10, 20, "10000.00", "6000.00"), "Joao", 25, 30);

        assertThat(m.utilizacao()).isEqualByComparingTo("80.00");
        assertThat(m.disponibilidade()).isEqualByComparingTo("83.33");
        assertThat(m.rentabilidade()).isEqualByComparingTo("4000.00");
        assertThat(m.rentabilidadeMediaViagem()).isEqualByComparingTo("400.00");
        assertThat(m.margem()).isEqualByComparingTo("40.00");
    }

    @Test
    void semDiasDisponiveisUtilizacaoEZero() {
        assertThat(CalculoIndicadores.utilizacao(0, 0)).isEqualByComparingTo("0");
    }

    @Test
    void semViagensMediaEZero() {
        assertThat(CalculoIndicadores.rentabilidadeMediaPorViagem(new BigDecimal("4000"), 0))
                .isEqualByComparingTo("0");
    }

    @Test
    void semFreteMargemEZero() {
        assertThat(CalculoIndicadores.margem(new BigDecimal("-500"), BigDecimal.ZERO))
                .isEqualByComparingTo("0");
    }

    // sum() devolve nulo quando todas as viagens estao sem valor.
    @Test
    void somaNulaContaComoZero() {
        IndicadoresMotorista m = IndicadoresService.indicadoresDe(soma(3, 2, null, null), null, 30, 30);

        assertThat(m.valorFrete()).isEqualByComparingTo("0");
        assertThat(m.rentabilidade()).isEqualByComparingTo("0");
    }

    @Test
    void totaisPonderamPelaFrotaInteira() {
        List<IndicadoresMotorista> motoristas = List.of(
                IndicadoresService.indicadoresDe(soma(10, 15, "10000", "6000"), "A", 30, 30),
                IndicadoresService.indicadoresDe(soma(5, 9, "2000", "3000"), "B", 30, 30));

        IndicadoresMes.Totais totais = IndicadoresService.totais(motoristas, 30);

        assertThat(totais.numeroViagens()).isEqualTo(15);
        assertThat(totais.rentabilidade()).isEqualByComparingTo("3000");
        assertThat(totais.rentabilidadeMediaViagem()).isEqualByComparingTo("200.00");
        assertThat(totais.margem()).isEqualByComparingTo("25.00");
        // (15 + 9) / (30 x 2)
        assertThat(totais.utilizacaoMedia()).isEqualByComparingTo("40.00");
    }

    @Test
    void totaisDeMesVazio() {
        IndicadoresMes.Totais totais = IndicadoresService.totais(List.of(), 30);

        assertThat(totais.motoristas()).isZero();
        assertThat(totais.utilizacaoMedia()).isEqualByComparingTo("0");
        assertThat(totais.margem()).isEqualByComparingTo("0");
    }

    // Manifesto 16882 (abr/2026): Valor Fretes 1.226,21 e Vale frete 430,75
    // (Valor Frete 335,80 + Pedagio 54,95 + Adicionais 40,00).
    @Test
    void casoRealDoManifesto() {
        IndicadoresMotorista m = IndicadoresService.indicadoresDe(
                soma(1, 1, 1, "1226.21", "430.75"), "Sergio", 30, 30);

        assertThat(m.rentabilidade()).isEqualByComparingTo("795.46");
        assertThat(m.rentabilidadeMediaViagem()).isEqualByComparingTo("795.46");
        assertThat(m.margem()).isEqualByComparingTo("64.87");
    }

    // Frota propria e manifesto sem CT-e contam como viagem, mas nao como base da media.
    @Test
    void mediaUsaSoAsViagensApuradas() {
        IndicadoresMotorista m = IndicadoresService.indicadoresDe(
                soma(10, 8, 4, "10000", "6000"), "Joao", 30, 30);

        assertThat(m.numeroViagens()).isEqualTo(10);
        assertThat(m.viagensApuradas()).isEqualTo(4);
        assertThat(m.rentabilidadeMediaViagem()).isEqualByComparingTo("1000.00");
    }

    @Test
    void totaisMediaUsaSoAsViagensApuradas() {
        List<IndicadoresMotorista> motoristas = List.of(
                IndicadoresService.indicadoresDe(soma(10, 10, 6, "10000", "6000"), "A", 30, 30),
                IndicadoresService.indicadoresDe(soma(5, 5, 2, "2000", "1000"), "B", 30, 30));

        IndicadoresMes.Totais totais = IndicadoresService.totais(motoristas, 30);

        assertThat(totais.numeroViagens()).isEqualTo(15);
        assertThat(totais.viagensApuradas()).isEqualTo(8);
        // (4000 + 1000) / 8
        assertThat(totais.rentabilidadeMediaViagem()).isEqualByComparingTo("625.00");
    }

    private static SomaMotoristaView soma(long viagens, long diasOperacao, String frete, String custo) {
        return soma(viagens, diasOperacao, viagens, frete, custo);
    }

    private static SomaMotoristaView soma(long viagens, long diasOperacao, long apuradas, String frete,
            String custo) {
        UUID id = UUID.randomUUID();
        return new SomaMotoristaView() {
            public UUID getMotoristaId() { return id; }
            public Long getNumeroViagens() { return viagens; }
            public Long getDiasOperacao() { return diasOperacao; }
            public Long getViagensApuradas() { return apuradas; }
            public BigDecimal getValorFrete() { return frete == null ? null : new BigDecimal(frete); }
            public BigDecimal getCustoTotal() { return custo == null ? null : new BigDecimal(custo); }
        };
    }
}
