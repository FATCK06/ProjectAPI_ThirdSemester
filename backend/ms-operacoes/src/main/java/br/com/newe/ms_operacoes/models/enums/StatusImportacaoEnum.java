package br.com.newe.ms_operacoes.models.enums;

/**
 * Ciclo de vida de uma importacao.
 *
 * Os dois primeiros estados existem para o fluxo em etapas da tela: o arquivo
 * fica parado enquanto o usuario mapeia colunas, confere erros e revisa. Nada
 * entra em viagens antes de EM_PROCESSAMENTO.
 */
public enum StatusImportacaoEnum {

    /** Arquivo recebido e guardado. Nada foi parseado nem gravado. */
    AGUARDANDO,

    /** Parseado e conferido: os erros por linha ja sao conhecidos. Ainda nada gravado. */
    VALIDADO,

    /** Gravacao em andamento. */
    EM_PROCESSAMENTO,

    /** Viagens gravadas. */
    CONCLUIDO,

    /** Falhou no parse ou na gravacao. */
    ERRO
}
