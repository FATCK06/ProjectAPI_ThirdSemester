package br.com.newe.ms_operacoes.service.importacao;

import java.util.List;

/**
 * Saida do parse: o que deu certo e o que nao deu, lado a lado.
 *
 * Uma linha invalida nao derruba o arquivo. Linha com pendencia entra em
 * {@code linhas} e tambem em {@code problemas} - e valida, mas tem o que mostrar.
 */
public record ResultadoParse(List<LinhaManifesto> linhas, List<LinhaComProblema> problemas) {

    public int linhasInvalidas() {
        return (int) problemas.stream().filter(LinhaComProblema::bloqueia).count();
    }

    public int linhasComPendencia() {
        return problemas.size() - linhasInvalidas();
    }

    public int totalLinhas() {
        return linhas.size() + linhasInvalidas();
    }

    public boolean temErro() {
        return problemas.stream().anyMatch(LinhaComProblema::bloqueia);
    }
}
