package br.com.newe.ms_veiculos.models;

import java.time.LocalDateTime;

import br.com.newe.ms_veiculos.models.enums.StatusImportacaoEnum;


public class ArquivoImportacao {
    private Long id;
    private String arquivoNome;
    private byte[] arquivoConteudo;
    private LocalDateTime dataImportacao;
    private StatusImportacaoEnum status;
    private String mesReferencia;

    public ArquivoImportacao() {
    }

    public ArquivoImportacao(Long id, String arquivoNome, byte[] arquivoConteudo, LocalDateTime dataImportacao, StatusImportacaoEnum status, String mesReferencia) {
        this.id = id;
        this.arquivoNome = arquivoNome;
        this.arquivoConteudo = arquivoConteudo;
        this.dataImportacao = dataImportacao;
        this.status = status;
        this.mesReferencia = mesReferencia;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getArquivoNome() {
        return arquivoNome;
    }

    public void setArquivoNome(String arquivoNome) {
        this.arquivoNome = arquivoNome;
    }

    public byte[] getArquivoConteudo() {
        return arquivoConteudo;
    }

    public void setArquivoConteudo(byte[] arquivoConteudo) {
        this.arquivoConteudo = arquivoConteudo;
    }

    public LocalDateTime getDataImportacao() {
        return dataImportacao;
    }

    public void setDataImportacao(LocalDateTime dataImportacao) {
        this.dataImportacao = dataImportacao;
    }

    public StatusImportacaoEnum getStatus() {
        return status;
    }

    public void setStatus(StatusImportacaoEnum status) {
        this.status = status;
    }

    public String getMesReferencia() {
        return mesReferencia;
    }

    public void setMesReferencia(String mesReferencia) {
        this.mesReferencia = mesReferencia;
    }

}
