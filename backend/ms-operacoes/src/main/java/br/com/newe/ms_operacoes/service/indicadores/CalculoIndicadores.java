package br.com.newe.ms_operacoes.service.indicadores;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Formulas do ranking gerencial. Vieram do antigo ms-indicadores; ficam aqui,
 * sem dependencia de banco, para serem testadas isoladas.
 *
 * Divisao por zero devolve zero: motorista sem viagem ou mes sem dia disponivel
 * aparece com indicador zerado em vez de derrubar o dashboard.
 */
public final class CalculoIndicadores {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private CalculoIndicadores() {
    }

    /** Dias em operacao / dias disponiveis, em %. */
    public static BigDecimal utilizacao(long diasOperacao, long diasDisponiveis) {
        return percentual(BigDecimal.valueOf(diasOperacao), BigDecimal.valueOf(diasDisponiveis));
    }

    /** Dias disponiveis / dias do mes, em %. */
    public static BigDecimal disponibilidade(long diasDisponiveis, long diasNoMes) {
        return percentual(BigDecimal.valueOf(diasDisponiveis), BigDecimal.valueOf(diasNoMes));
    }

    public static BigDecimal rentabilidade(BigDecimal frete, BigDecimal custo) {
        return zeroSeNulo(frete).subtract(zeroSeNulo(custo));
    }

    public static BigDecimal rentabilidadeMediaPorViagem(BigDecimal rentabilidade, long numeroViagens) {
        if (numeroViagens == 0) {
            return BigDecimal.ZERO;
        }
        return zeroSeNulo(rentabilidade).divide(BigDecimal.valueOf(numeroViagens), 2, RoundingMode.HALF_UP);
    }

    /** Rentabilidade / frete, em %. */
    public static BigDecimal margem(BigDecimal rentabilidade, BigDecimal frete) {
        return percentual(zeroSeNulo(rentabilidade), zeroSeNulo(frete));
    }

    static BigDecimal percentual(BigDecimal parte, BigDecimal todo) {
        if (todo.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return parte.multiply(CEM).divide(todo, 2, RoundingMode.HALF_UP);
    }

    static BigDecimal zeroSeNulo(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
