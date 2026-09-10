package br.com.newe.ms_veiculos.models.entity;

import lombok.Data;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import br.com.newe.ms_veiculos.models.enums.StatusImportacao;


@Entity
@Data
@Table(name = "importacoes" )
public class ArquivoImport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "nome_arquivo", nullable = false)
    private String nomeArquivo;
    @Column(name = "tamanho_bytes", nullable = false)
    private Long tamanhoBytes;
    @Column(name = "extensao", nullable = false)
    private String extensao;
    @Column(name = "mes_referencia", nullable = false, length = 7)
    private String mesReferencia;
    @Lob
    @Column(name = "conteudo_arquivo", nullable = false)
    private byte[] conteudoArquivo;
    @Column(name = "data_importacao", nullable = false)
    private LocalDateTime dataImportacao;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusImportacao status;

    protected ArquivoImport() {
    }

    public ArquivoImport(
        String nomeArquivo,
        long tamanhoBytes,
        String extensao,
        String mesReferencia,
        byte[] conteudoArquivo,
        LocalDateTime dataImportacao,
        StatusImportacao status) {
        this.nomeArquivo = nomeArquivo;
        this.tamanhoBytes = tamanhoBytes;
        this.extensao = extensao;
        this.mesReferencia = mesReferencia;
        this.conteudoArquivo = conteudoArquivo;
        this.dataImportacao = dataImportacao;
        this.status = status;
    }
}