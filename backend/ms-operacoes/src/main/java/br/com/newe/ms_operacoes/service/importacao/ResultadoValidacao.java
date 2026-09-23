package br.com.newe.ms_operacoes.service.importacao;

import java.util.List;

/**
 * O que o arquivo tem, antes de gravar qualquer coisa.
 *
 * Alimenta os passos de mapeamento, validacao e revisao da tela. Nada aqui e
 * persistido: o parse roda em memoria a cada chamada e e descartado no fim da
 * requisicao.
 */
public record ResultadoValidacao(
        Long importacaoId,
        String arquivoNome,
        int totalLinhas,
        int linhasValidas,
        int linhasInvalidas,
        /** Cabecalho do arquivo, na ordem em que veio. */
        List<String> colunasDetectadas,
        /** Colunas que o sistema espera, marcando quais foram encontradas. */
        List<MapeamentoColuna> mapeamento,
        /** Limitado: uma planilha no formato errado geraria um erro por linha. */
        List<ErroLinha> erros,
        /** Primeiras linhas validas, para o passo de revisao. */
        List<LinhaPreview> amostra
) {

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
