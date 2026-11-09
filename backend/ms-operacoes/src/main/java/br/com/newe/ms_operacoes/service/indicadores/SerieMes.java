package br.com.newe.ms_operacoes.service.indicadores;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Um mes de /api/dashboard/indicadores/serie. Ao contrario de {@link HistoricoMes},
 * mes sem viagem nao vira zero: vem com semDados = true e sem totais.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SerieMes(
        String mesReferencia,
        boolean semDados,
        String mensagem,
        Totais totais
) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Totais(
            long numeroViagens,
            BigDecimal utilizacaoMedia,
            BigDecimal valorFrete,
            BigDecimal custoTotal,
            BigDecimal rentabilidade
    ) {
        /** Copia sem os campos financeiros (viram null e somem do JSON). */
        public Totais semValoresFinanceiros() {
            return new Totais(numeroViagens, utilizacaoMedia, null, null, null);
        }
    }

    /** Copia sem valores financeiros nos totais. */
    public SerieMes semValoresFinanceiros() {
        return new SerieMes(mesReferencia, semDados, mensagem,
                totais == null ? null : totais.semValoresFinanceiros());
    }
}
