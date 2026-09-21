package br.com.newe.ms_veiculos.service.importacao;

public class LinhaManifestoInvalidaException extends RuntimeException {

    private final int numeroLinha;

    public LinhaManifestoInvalidaException(int numeroLinha, String motivo) {
        super("Linha " + numeroLinha + " invalida: " + motivo);
        this.numeroLinha = numeroLinha;
    }

    public int getNumeroLinha() {
        return numeroLinha;
    }
}
