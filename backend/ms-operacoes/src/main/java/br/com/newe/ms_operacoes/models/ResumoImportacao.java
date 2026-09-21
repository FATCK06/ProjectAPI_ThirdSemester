package br.com.newe.ms_operacoes.models;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import br.com.newe.ms_operacoes.models.enums.StatusImportacaoEnum;

public class ResumoImportacao {
    private String mesReferencia;
    private long totalImportacoes;
    private Map<StatusImportacaoEnum, Long> porStatus;

    // Lista de indicadores financeiros ainda nao definida pelo time; fica null (e omitida do JSON)
    // para o perfil operador, e como mapa extensivel para os demais perfis.
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Object> indicadoresFinanceiros;

    public ResumoImportacao() {
    }

    public ResumoImportacao(String mesReferencia, long totalImportacoes,
            Map<StatusImportacaoEnum, Long> porStatus, Map<String, Object> indicadoresFinanceiros) {
        this.mesReferencia = mesReferencia;
        this.totalImportacoes = totalImportacoes;
        this.porStatus = porStatus;
        this.indicadoresFinanceiros = indicadoresFinanceiros;
    }

    public String getMesReferencia() {
        return mesReferencia;
    }

    public void setMesReferencia(String mesReferencia) {
        this.mesReferencia = mesReferencia;
    }

    public long getTotalImportacoes() {
        return totalImportacoes;
    }

    public void setTotalImportacoes(long totalImportacoes) {
        this.totalImportacoes = totalImportacoes;
    }

    public Map<StatusImportacaoEnum, Long> getPorStatus() {
        return porStatus;
    }

    public void setPorStatus(Map<StatusImportacaoEnum, Long> porStatus) {
        this.porStatus = porStatus;
    }

    public Map<String, Object> getIndicadoresFinanceiros() {
        return indicadoresFinanceiros;
    }

    public void setIndicadoresFinanceiros(Map<String, Object> indicadoresFinanceiros) {
        this.indicadoresFinanceiros = indicadoresFinanceiros;
    }

}
