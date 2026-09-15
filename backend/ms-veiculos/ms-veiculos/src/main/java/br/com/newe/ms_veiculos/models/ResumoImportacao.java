package br.com.newe.ms_veiculos.models;

import java.util.Map;

import br.com.newe.ms_veiculos.models.enums.StatusImportacaoEnum;

public class ResumoImportacao {
    private long totalImportacoes;
    private Map<StatusImportacaoEnum, Long> porStatus;
    private Map<String, Long> porMesReferencia;

    public ResumoImportacao() {
    }

    public ResumoImportacao(long totalImportacoes, Map<StatusImportacaoEnum, Long> porStatus, Map<String, Long> porMesReferencia) {
        this.totalImportacoes = totalImportacoes;
        this.porStatus = porStatus;
        this.porMesReferencia = porMesReferencia;
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

    public Map<String, Long> getPorMesReferencia() {
        return porMesReferencia;
    }

    public void setPorMesReferencia(Map<String, Long> porMesReferencia) {
        this.porMesReferencia = porMesReferencia;
    }

}
