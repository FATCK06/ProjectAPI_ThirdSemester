package br.com.newe.ms_operacoes.erro;

/**
 * Validacao que nao cabe numa anotacao (ex.: valor fora de uma lista).
 * O ErroHandlerGlobal responde 400 com {"campos": {campo: motivo}}.
 */
public class DadoInvalidoException extends RuntimeException {

    private final String campo;
    private final String motivo;

    public DadoInvalidoException(String campo, String motivo) {
        super(campo + ": " + motivo);
        this.campo = campo;
        this.motivo = motivo;
    }

    public String getCampo() {
        return campo;
    }

    public String getMotivo() {
        return motivo;
    }
}
