package br.com.newe.ms_operacoes.models;

import java.util.Map;

import br.com.newe.ms_operacoes.models.enums.StatusImportacaoEnum;

/**
 * Quantas importacoes existem em cada estado.
 *
 * Sem recorte por mes: a planilha da Newelog e trimestral, entao o arquivo nunca
 * pertenceu a um mes so. Indicador mensal sai de viagens.mes_referencia, que e
 * derivado da data de cada linha.
 */
public record ResumoImportacao(
        long totalImportacoes,
        Map<StatusImportacaoEnum, Long> porStatus
) {
}
