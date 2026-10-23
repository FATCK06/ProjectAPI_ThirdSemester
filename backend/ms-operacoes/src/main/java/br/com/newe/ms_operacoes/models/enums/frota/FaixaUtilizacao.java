package br.com.newe.ms_operacoes.models.enums.frota;

/**
 * Faixa da utilizacao do motorista no mes. Os cortes ficam em
 * situacao-utilizacao.properties (situacao.utilizacao.*).
 */
public enum FaixaUtilizacao {

    /** utilizacao >= corte alto (inclui acima de 100%). */
    ALTA,

    /** corte baixo <= utilizacao < corte alto. */
    INTERMEDIARIA,

    /** utilizacao < corte baixo. */
    BAIXA
}
