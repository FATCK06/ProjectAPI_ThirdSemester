package br.com.newe.ms_operacoes.models;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import br.com.newe.ms_operacoes.models.enums.StatusImportacaoEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Uma execucao de importacao de manifestos.
 *
 * Guarda o arquivo enquanto ele passa pelas etapas da tela. O conteudo so vira
 * viagens quando o usuario confirma - ate la o registro fica em AGUARDANDO ou
 * VALIDADO, e o parse acontece em memoria a cada consulta, sem ser persistido.
 */
@Entity
@Table(name = "importacoes")
public class Importacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "arquivo_nome")
    private String arquivoNome;

    /**
     * bytea, sem @Lob. Com @Lob o Hibernate mapeia para "oid" e joga o binario em
     * pg_largeobject: o download quebra fora de transacao e o conteudo vira lixo
     * orfao quando a linha some.
     */
    @Column(name = "arquivo_conteudo")
    @JsonIgnore
    private byte[] arquivoConteudo;

    @Column(name = "data_importacao")
    private LocalDateTime dataImportacao;

    @Enumerated(EnumType.STRING)
    private StatusImportacaoEnum status;

    @Column(name = "usuario_responsavel")
    private String usuarioResponsavel;

    /** Linhas lidas do arquivo. */
    @Column(name = "total_linhas")
    private Integer totalLinhas;

    @Column(name = "linhas_validas")
    private Integer linhasValidas;

    @Column(name = "linhas_invalidas")
    private Integer linhasInvalidas;

    /** Viagens inseridas; menor que totalLinhas quando o arquivo repete manifesto ja importado. */
    @Column(name = "linhas_gravadas")
    private Integer linhasGravadas;

    public Importacao() {
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

    public String getUsuarioResponsavel() {
        return usuarioResponsavel;
    }

    public void setUsuarioResponsavel(String usuarioResponsavel) {
        this.usuarioResponsavel = usuarioResponsavel;
    }

    public Integer getTotalLinhas() {
        return totalLinhas;
    }

    public void setTotalLinhas(Integer totalLinhas) {
        this.totalLinhas = totalLinhas;
    }

    public Integer getLinhasValidas() {
        return linhasValidas;
    }

    public void setLinhasValidas(Integer linhasValidas) {
        this.linhasValidas = linhasValidas;
    }

    public Integer getLinhasInvalidas() {
        return linhasInvalidas;
    }

    public void setLinhasInvalidas(Integer linhasInvalidas) {
        this.linhasInvalidas = linhasInvalidas;
    }

    public Integer getLinhasGravadas() {
        return linhasGravadas;
    }

    public void setLinhasGravadas(Integer linhasGravadas) {
        this.linhasGravadas = linhasGravadas;
    }

}
