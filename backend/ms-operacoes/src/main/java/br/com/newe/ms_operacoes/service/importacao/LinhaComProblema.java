package br.com.newe.ms_operacoes.service.importacao;

import java.util.List;
import java.util.Map;

import br.com.newe.ms_operacoes.service.importacao.AvaliacaoCampo.Severidade;

/**
 * Uma linha do arquivo com o que ha de errado nela.
 *
 * @param valores celulas cruas, como vieram no CSV - e o valor invalido que a
 *                tela precisa mostrar, nao o convertido
 */
public record LinhaComProblema(
        int numeroLinha,
        Map<String, String> valores,
        List<ProblemaCelula> problemas) {

    /** Linha que impede a importacao do arquivo. Pendencia sozinha nao bloqueia. */
    public boolean bloqueia() {
        return problemas.stream().anyMatch(p -> p.severidade() == Severidade.ERRO);
    }

    public Severidade severidade() {
        return bloqueia() ? Severidade.ERRO : Severidade.PENDENCIA;
    }
}
