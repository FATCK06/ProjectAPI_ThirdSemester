package br.com.newe.ms_operacoes.service.importacao;

/** Estado de uma celula do CSV, independente da exigencia do campo. */
public enum EstadoCampo {

    OK,

    /** Celula em branco. */
    VAZIO,

    /** 0,00 nas colunas onde o CSV usa zero no lugar de "nao informado". */
    AUSENTE_COMO_ZERO,

    /** Tem conteudo, mas nao no formato esperado. */
    NAO_CONVERTE
}
