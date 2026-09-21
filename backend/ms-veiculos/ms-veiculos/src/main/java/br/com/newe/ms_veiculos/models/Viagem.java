package br.com.newe.ms_veiculos.models;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "viagem", indexes = {
        @Index(name = "idx_viagem_mes_referencia", columnList = "mes_referencia"),
        @Index(name = "idx_viagem_arquivo_importacao_id", columnList = "arquivo_importacao_id")
})
public class Viagem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "viagem_seq")
    @SequenceGenerator(name = "viagem_seq", sequenceName = "viagem_seq", allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arquivo_importacao_id", nullable = false)
    @JsonIgnore
    private ArquivoImportacao arquivoImportacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motorista_id", nullable = false)
    @JsonIgnore
    private Motorista motorista;

    @Column(name = "mes_referencia", nullable = false)
    private String mesReferencia;

    private String manifesto;
    private String filial;
    private LocalDate data;
    private String veiculo;
    private String reboque1;
    private String reboque2;
    private String reboque3;
    private String destino;
    private Integer kmSaida;
    private Integer kmChegada;
    private Integer servicos;
    private Integer nfs;
    private BigDecimal kgReal;
    private BigDecimal kgTaxado;
    private BigDecimal m3;
    private BigDecimal capacidadeVeiculo;
    private BigDecimal percentualAprovVeiculo;
    private BigDecimal valeFrete;
    private BigDecimal valorNf;
    private BigDecimal valorFretes;
    private BigDecimal valorFrete;
    private BigDecimal combustivel;
    private BigDecimal pedagio;
    private BigDecimal diaria;
    private Integer coletas;
    private Integer entregas;
    private Integer despachos;
    private Integer retiradas;
    private Integer coletasReversa;
    private BigDecimal adicionais;
    private BigDecimal descontos;
    private BigDecimal adiantamento;
    private BigDecimal despesas;
    private BigDecimal totalDespesas;
    private BigDecimal saldoDespesas;
    private BigDecimal inss;
    private BigDecimal sestSenat;
    private BigDecimal ir;
    private BigDecimal saldoAPagar;
    private Integer servicosFinalizados;
    private BigDecimal percentualEfetividade;
    private String classificacao;

    @Column(columnDefinition = "text")
    private String observacoesOperacionais;

    private String statusManifesto;
    private String usuarioManifesto;

    public Viagem() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ArquivoImportacao getArquivoImportacao() {
        return arquivoImportacao;
    }

    public void setArquivoImportacao(ArquivoImportacao arquivoImportacao) {
        this.arquivoImportacao = arquivoImportacao;
    }

    @JsonProperty("arquivoImportacaoId")
    public Long getArquivoImportacaoId() {
        return arquivoImportacao != null ? arquivoImportacao.getId() : null;
    }

    public Motorista getMotorista() {
        return motorista;
    }

    public void setMotorista(Motorista motorista) {
        this.motorista = motorista;
    }

    @JsonProperty("motoristaId")
    public Long getMotoristaId() {
        return motorista != null ? motorista.getId() : null;
    }

    public String getMesReferencia() {
        return mesReferencia;
    }

    public void setMesReferencia(String mesReferencia) {
        this.mesReferencia = mesReferencia;
    }

    public String getManifesto() {
        return manifesto;
    }

    public void setManifesto(String manifesto) {
        this.manifesto = manifesto;
    }

    public String getFilial() {
        return filial;
    }

    public void setFilial(String filial) {
        this.filial = filial;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(String veiculo) {
        this.veiculo = veiculo;
    }

    public String getReboque1() {
        return reboque1;
    }

    public void setReboque1(String reboque1) {
        this.reboque1 = reboque1;
    }

    public String getReboque2() {
        return reboque2;
    }

    public void setReboque2(String reboque2) {
        this.reboque2 = reboque2;
    }

    public String getReboque3() {
        return reboque3;
    }

    public void setReboque3(String reboque3) {
        this.reboque3 = reboque3;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public Integer getKmSaida() {
        return kmSaida;
    }

    public void setKmSaida(Integer kmSaida) {
        this.kmSaida = kmSaida;
    }

    public Integer getKmChegada() {
        return kmChegada;
    }

    public void setKmChegada(Integer kmChegada) {
        this.kmChegada = kmChegada;
    }

    public Integer getServicos() {
        return servicos;
    }

    public void setServicos(Integer servicos) {
        this.servicos = servicos;
    }

    public Integer getNfs() {
        return nfs;
    }

    public void setNfs(Integer nfs) {
        this.nfs = nfs;
    }

    public BigDecimal getKgReal() {
        return kgReal;
    }

    public void setKgReal(BigDecimal kgReal) {
        this.kgReal = kgReal;
    }

    public BigDecimal getKgTaxado() {
        return kgTaxado;
    }

    public void setKgTaxado(BigDecimal kgTaxado) {
        this.kgTaxado = kgTaxado;
    }

    public BigDecimal getM3() {
        return m3;
    }

    public void setM3(BigDecimal m3) {
        this.m3 = m3;
    }

    public BigDecimal getCapacidadeVeiculo() {
        return capacidadeVeiculo;
    }

    public void setCapacidadeVeiculo(BigDecimal capacidadeVeiculo) {
        this.capacidadeVeiculo = capacidadeVeiculo;
    }

    public BigDecimal getPercentualAprovVeiculo() {
        return percentualAprovVeiculo;
    }

    public void setPercentualAprovVeiculo(BigDecimal percentualAprovVeiculo) {
        this.percentualAprovVeiculo = percentualAprovVeiculo;
    }

    public BigDecimal getValeFrete() {
        return valeFrete;
    }

    public void setValeFrete(BigDecimal valeFrete) {
        this.valeFrete = valeFrete;
    }

    public BigDecimal getValorNf() {
        return valorNf;
    }

    public void setValorNf(BigDecimal valorNf) {
        this.valorNf = valorNf;
    }

    public BigDecimal getValorFretes() {
        return valorFretes;
    }

    public void setValorFretes(BigDecimal valorFretes) {
        this.valorFretes = valorFretes;
    }

    public BigDecimal getValorFrete() {
        return valorFrete;
    }

    public void setValorFrete(BigDecimal valorFrete) {
        this.valorFrete = valorFrete;
    }

    public BigDecimal getCombustivel() {
        return combustivel;
    }

    public void setCombustivel(BigDecimal combustivel) {
        this.combustivel = combustivel;
    }

    public BigDecimal getPedagio() {
        return pedagio;
    }

    public void setPedagio(BigDecimal pedagio) {
        this.pedagio = pedagio;
    }

    public BigDecimal getDiaria() {
        return diaria;
    }

    public void setDiaria(BigDecimal diaria) {
        this.diaria = diaria;
    }

    public Integer getColetas() {
        return coletas;
    }

    public void setColetas(Integer coletas) {
        this.coletas = coletas;
    }

    public Integer getEntregas() {
        return entregas;
    }

    public void setEntregas(Integer entregas) {
        this.entregas = entregas;
    }

    public Integer getDespachos() {
        return despachos;
    }

    public void setDespachos(Integer despachos) {
        this.despachos = despachos;
    }

    public Integer getRetiradas() {
        return retiradas;
    }

    public void setRetiradas(Integer retiradas) {
        this.retiradas = retiradas;
    }

    public Integer getColetasReversa() {
        return coletasReversa;
    }

    public void setColetasReversa(Integer coletasReversa) {
        this.coletasReversa = coletasReversa;
    }

    public BigDecimal getAdicionais() {
        return adicionais;
    }

    public void setAdicionais(BigDecimal adicionais) {
        this.adicionais = adicionais;
    }

    public BigDecimal getDescontos() {
        return descontos;
    }

    public void setDescontos(BigDecimal descontos) {
        this.descontos = descontos;
    }

    public BigDecimal getAdiantamento() {
        return adiantamento;
    }

    public void setAdiantamento(BigDecimal adiantamento) {
        this.adiantamento = adiantamento;
    }

    public BigDecimal getDespesas() {
        return despesas;
    }

    public void setDespesas(BigDecimal despesas) {
        this.despesas = despesas;
    }

    public BigDecimal getTotalDespesas() {
        return totalDespesas;
    }

    public void setTotalDespesas(BigDecimal totalDespesas) {
        this.totalDespesas = totalDespesas;
    }

    public BigDecimal getSaldoDespesas() {
        return saldoDespesas;
    }

    public void setSaldoDespesas(BigDecimal saldoDespesas) {
        this.saldoDespesas = saldoDespesas;
    }

    public BigDecimal getInss() {
        return inss;
    }

    public void setInss(BigDecimal inss) {
        this.inss = inss;
    }

    public BigDecimal getSestSenat() {
        return sestSenat;
    }

    public void setSestSenat(BigDecimal sestSenat) {
        this.sestSenat = sestSenat;
    }

    public BigDecimal getIr() {
        return ir;
    }

    public void setIr(BigDecimal ir) {
        this.ir = ir;
    }

    public BigDecimal getSaldoAPagar() {
        return saldoAPagar;
    }

    public void setSaldoAPagar(BigDecimal saldoAPagar) {
        this.saldoAPagar = saldoAPagar;
    }

    public Integer getServicosFinalizados() {
        return servicosFinalizados;
    }

    public void setServicosFinalizados(Integer servicosFinalizados) {
        this.servicosFinalizados = servicosFinalizados;
    }

    public BigDecimal getPercentualEfetividade() {
        return percentualEfetividade;
    }

    public void setPercentualEfetividade(BigDecimal percentualEfetividade) {
        this.percentualEfetividade = percentualEfetividade;
    }

    public String getClassificacao() {
        return classificacao;
    }

    public void setClassificacao(String classificacao) {
        this.classificacao = classificacao;
    }

    public String getObservacoesOperacionais() {
        return observacoesOperacionais;
    }

    public void setObservacoesOperacionais(String observacoesOperacionais) {
        this.observacoesOperacionais = observacoesOperacionais;
    }

    public String getStatusManifesto() {
        return statusManifesto;
    }

    public void setStatusManifesto(String statusManifesto) {
        this.statusManifesto = statusManifesto;
    }

    public String getUsuarioManifesto() {
        return usuarioManifesto;
    }

    public void setUsuarioManifesto(String usuarioManifesto) {
        this.usuarioManifesto = usuarioManifesto;
    }

}
