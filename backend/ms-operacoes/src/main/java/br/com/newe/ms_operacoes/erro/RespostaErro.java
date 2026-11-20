package br.com.newe.ms_operacoes.erro;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Corpo de toda resposta de erro da API: {"erro": "...", "campos": {...}}.
 *
 * "erro" e o que o front mostra. "campos" so vem em dado invalido, com o nome do
 * campo e o motivo. "codigo" so vem em falha inesperada: e o mesmo valor gravado
 * no log junto com o stack trace, para achar o erro sem expor detalhe tecnico.
 * Nunca leva stack trace, SQL, nome de tabela ou URL interna.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record RespostaErro(String erro, Map<String, String> campos, String codigo) {

    public static final String DADOS_INVALIDOS = "Dados inválidos. Verifique os campos informados.";
    public static final String FALHA_GENERICA = "Não foi possível processar a requisição.";

    public RespostaErro(String erro) {
        this(erro, null, null);
    }

    public RespostaErro(String erro, Map<String, String> campos) {
        this(erro, campos, null);
    }

    /** Falha inesperada: mensagem generica e o codigo que aparece no log. */
    public static RespostaErro falha(String codigo) {
        return new RespostaErro(FALHA_GENERICA, null, codigo);
    }
}
