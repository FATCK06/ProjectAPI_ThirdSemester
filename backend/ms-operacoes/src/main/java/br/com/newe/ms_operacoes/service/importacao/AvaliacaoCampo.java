package br.com.newe.ms_operacoes.service.importacao;

/**
 * Resultado da classificacao de uma celula.
 *
 * @param valor   convertido; null quando o estado nao e OK
 * @param detalhe mensagem do conversor; so preenchido em NAO_CONVERTE
 */
public record AvaliacaoCampo(
        CampoManifesto campo,
        String valorCru,
        EstadoCampo estado,
        Object valor,
        String detalhe) {

    public enum Severidade { OK, PENDENCIA, ERRO }

    public Severidade severidade() {
        if (estado == EstadoCampo.OK) {
            return Severidade.OK;
        }
        if (campo.nivel() == CampoManifesto.Nivel.OBRIGATORIO) {
            return Severidade.ERRO;
        }
        // Valor escrito errado importa mesmo em campo opcional: e dado perdido,
        // nao ausencia. Ausencia so vira pendencia em campo importante.
        if (estado == EstadoCampo.NAO_CONVERTE) {
            return Severidade.PENDENCIA;
        }
        return campo.nivel() == CampoManifesto.Nivel.IMPORTANTE
                ? Severidade.PENDENCIA
                : Severidade.OK;
    }
}
