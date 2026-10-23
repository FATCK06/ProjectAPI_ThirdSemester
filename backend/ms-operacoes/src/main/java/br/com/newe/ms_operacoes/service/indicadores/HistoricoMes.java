package br.com.newe.ms_operacoes.service.indicadores;

/**
 * Totais de um mes no historico de /api/dashboard/indicadores/historico. Mes sem
 * viagem vem com tudo zerado (motoristas = 0), nao fica de fora: o front precisa
 * da sequencia completa para a linha de tendencia.
 */
public record HistoricoMes(String mesReferencia, IndicadoresMes.Totais totais) {

    public HistoricoMes semValoresFinanceiros() {
        return new HistoricoMes(mesReferencia, totais.semValoresFinanceiros());
    }
}
