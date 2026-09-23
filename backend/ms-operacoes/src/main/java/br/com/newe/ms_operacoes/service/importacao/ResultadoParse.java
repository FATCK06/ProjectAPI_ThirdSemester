package br.com.newe.ms_operacoes.service.importacao;

import java.util.List;

/**
 * Saida do parse: o que deu certo e o que nao deu, lado a lado.
 *
 * Antes uma unica linha invalida derrubava o arquivo inteiro. Numa planilha de
 * 1871 manifestos isso significa perder 1870 registros bons por causa de um CPF
 * digitado errado - entao os erros sao coletados e o resto segue.
 */
public record ResultadoParse(List<LinhaManifesto> linhas, List<ErroLinha> erros) {

    public int totalLinhas() {
        return linhas.size() + erros.size();
    }

    public boolean temErro() {
        return !erros.isEmpty();
    }
}
