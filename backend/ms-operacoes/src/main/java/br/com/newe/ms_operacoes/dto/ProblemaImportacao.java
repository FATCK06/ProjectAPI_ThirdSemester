package br.com.newe.ms_operacoes.dto;

/** Problema que o ms-frota encontrou consultando o banco. bloqueia=false so avisa. */
public record ProblemaImportacao(int numeroLinha, String coluna, String valor, boolean bloqueia, String mensagem) {
}
