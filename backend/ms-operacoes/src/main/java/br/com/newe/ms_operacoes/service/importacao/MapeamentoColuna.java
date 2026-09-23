package br.com.newe.ms_operacoes.service.importacao;

/**
 * Uma coluna que o sistema espera, confrontada com o cabecalho do arquivo.
 * Alimenta a tela de mapeamento: mostra o que foi encontrado e o que falta,
 * em vez de o usuario descobrir so quando a importacao falhar.
 */
public record MapeamentoColuna(
        String campoSistema,
        boolean obrigatoria,
        boolean encontrada
) {
}
