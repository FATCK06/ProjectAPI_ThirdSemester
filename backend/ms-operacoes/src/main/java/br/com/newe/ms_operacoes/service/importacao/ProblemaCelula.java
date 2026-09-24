package br.com.newe.ms_operacoes.service.importacao;

import br.com.newe.ms_operacoes.service.importacao.AvaliacaoCampo.Severidade;

/**
 * Um problema em uma celula, com o bastante para a tela apontar onde esta e
 * o que fazer: coluna, valor que veio, formato esperado e a mensagem pronta.
 */
public record ProblemaCelula(
        String campo,
        String coluna,
        Severidade severidade,
        EstadoCampo estado,
        String valorEncontrado,
        String valorEsperado,
        String mensagem) {

    public static ProblemaCelula de(AvaliacaoCampo avaliacao, int numeroLinha) {
        CampoManifesto campo = avaliacao.campo();
        return new ProblemaCelula(
                campo.name(),
                campo.coluna(),
                avaliacao.severidade(),
                avaliacao.estado(),
                avaliacao.valorCru(),
                campo.formatoEsperado(),
                mensagem(avaliacao, numeroLinha));
    }

    /** Falha que o catalogo nao previu; mantem a linha rastreavel. */
    static ProblemaCelula inesperado(String mensagem) {
        return new ProblemaCelula(null, null, Severidade.ERRO, EstadoCampo.NAO_CONVERTE, null, null, mensagem);
    }

    private static String mensagem(AvaliacaoCampo avaliacao, int numeroLinha) {
        String coluna = avaliacao.campo().coluna();

        return switch (avaliacao.estado()) {
            case VAZIO -> avaliacao.campo().nivel() == CampoManifesto.Nivel.OBRIGATORIO
                    ? "A coluna '" + coluna + "' e obrigatoria e nao possui valor na linha " + numeroLinha
                    : "A coluna '" + coluna + "' nao foi preenchida na linha " + numeroLinha;
            case AUSENTE_COMO_ZERO -> "A coluna '" + coluna + "' veio zerada na linha " + numeroLinha
                    + ": no manifesto zero significa nao informado";
            case NAO_CONVERTE -> "A coluna '" + coluna + "' na linha " + numeroLinha
                    + " nao esta no formato esperado"
                    + (avaliacao.detalhe() == null ? "" : ": " + avaliacao.detalhe());
            case OK -> "";
        };
    }
}
