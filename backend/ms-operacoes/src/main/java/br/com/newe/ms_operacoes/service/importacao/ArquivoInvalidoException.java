package br.com.newe.ms_operacoes.service.importacao;

/**
 * Arquivo que nem chega a ser uma planilha de manifestos: vazio, sem cabecalho
 * ou sem nenhuma linha de dados. A mensagem vai direto para a tela.
 */
public class ArquivoInvalidoException extends RuntimeException {

    public ArquivoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
