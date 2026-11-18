package br.com.newe.ms_frota.dto;

/**
 * Problema que so o banco revela (tipo inexistente, Renavam de outra placa...).
 *
 * @param bloqueia true impede gravar o arquivo; false so avisa
 */
public record ProblemaImportacao(int numeroLinha, String coluna, String valor, boolean bloqueia, String mensagem) {
}
