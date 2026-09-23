package br.com.newe.ms_operacoes.service.importacao;

import java.util.List;

/**
 * O que o arquivo tem, antes de gravar qualquer coisa.
 *
 * Alimenta os passos de mapeamento, validacao e revisao da tela. Nada aqui e
 * persistido: o parse roda em memoria a cada chamada.
 */
public record ResultadoValidacao(
        Long importacaoId,
        String arquivoNome,
        int totalLinhas,
        int linhasValidas,
        int linhasInvalidas,
        int linhasComPendencia,
        /** Cabecalho do arquivo, na ordem em que veio. */
        List<String> colunasDetectadas,
        /** Colunas que o sistema espera, marcando quais foram encontradas. */
        List<MapeamentoColuna> mapeamento,
        Pagina pagina,
        /** So as linhas com problema, da pagina pedida. */
        List<LinhaComProblema> problemas,
        /** Primeiras linhas validas, para o passo de revisao. */
        List<LinhaPreview> amostra
) {

    public record Pagina(int numero, int tamanho, int totalElementos) {
    }

    /** O arquivo so pode ser gravado quando nao ha linha invalida (regra do time). */
    public boolean podeExecutar() {
        return linhasInvalidas == 0 && linhasValidas > 0 && faltandoObrigatoria().isEmpty();
    }

    public List<String> faltandoObrigatoria() {
        return mapeamento.stream()
                .filter(m -> m.obrigatoria() && !m.encontrada())
                .map(MapeamentoColuna::campoSistema)
                .toList();
    }
}
