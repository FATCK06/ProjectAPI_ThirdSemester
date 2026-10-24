package br.com.newe.ms_frota.erro;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Corpo de toda resposta de erro da API: {"erro": "...", "campos": {...}}.
 *
 * "erro" e o que o front mostra. "campos" so vem em dado invalido, com o nome do
 * campo e o motivo. Nunca leva stack trace, SQL, nome de tabela ou URL interna.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record RespostaErro(String erro, Map<String, String> campos) {

    public static final String DADOS_INVALIDOS = "Dados inválidos. Verifique os campos informados.";
    public static final String FALHA_GENERICA = "Não foi possível processar a requisição.";

    public RespostaErro(String erro) {
        this(erro, null);
    }
}
