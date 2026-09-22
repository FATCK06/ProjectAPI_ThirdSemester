package br.com.newe.ms_operacoes.service.importacao;

/** Uma linha que o parser nao conseguiu converter, com o motivo. */
public record ErroLinha(int numeroLinha, String mensagem) {
}
