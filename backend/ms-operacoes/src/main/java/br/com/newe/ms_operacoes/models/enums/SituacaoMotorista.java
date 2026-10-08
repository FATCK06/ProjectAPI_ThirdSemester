package br.com.newe.ms_operacoes.models.enums;

/**
 * Situacao do motorista no mes, derivada da faixa de utilizacao. O mapeamento
 * faixa -> situacao fica em ClassificadorSituacao.situacaoDa.
 */
public enum SituacaoMotorista {

    DISPONIVEL,

    INDISPONIVEL,

    /** Mes sem dia disponivel: nao ha utilizacao a calcular. */
    SEM_DIAS_DISPONIVEIS
}
