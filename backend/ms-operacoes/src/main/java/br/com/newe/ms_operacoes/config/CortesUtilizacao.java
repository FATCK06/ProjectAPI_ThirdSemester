package br.com.newe.ms_operacoes.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cortes da faixa de utilizacao (%), lidos de situacao-utilizacao.properties.
 * A validacao roda no bind, entao valor invalido derruba a subida com o nome
 * da propriedade na mensagem.
 */
@ConfigurationProperties(prefix = CortesUtilizacao.PREFIXO)
public record CortesUtilizacao(BigDecimal corteBaixa, BigDecimal corteAlta) {

    static final String PREFIXO = "situacao.utilizacao";
    static final String CORTE_BAIXA = PREFIXO + ".corte-baixa";
    static final String CORTE_ALTA = PREFIXO + ".corte-alta";

    public CortesUtilizacao {
        exigirNaoNegativo(CORTE_BAIXA, corteBaixa);
        exigirNaoNegativo(CORTE_ALTA, corteAlta);
        if (corteBaixa.compareTo(corteAlta) > 0) {
            throw new IllegalArgumentException(CORTE_BAIXA + " (" + corteBaixa + ") maior que "
                    + CORTE_ALTA + " (" + corteAlta + ")");
        }
    }

    private static void exigirNaoNegativo(String propriedade, BigDecimal valor) {
        if (valor == null) {
            throw new IllegalArgumentException(propriedade + " nao configurado");
        }
        if (valor.signum() < 0) {
            throw new IllegalArgumentException(propriedade + " negativo (" + valor + ")");
        }
    }
}
