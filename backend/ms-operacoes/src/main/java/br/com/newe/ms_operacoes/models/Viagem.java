package br.com.newe.ms_operacoes.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Uma linha do manifesto importado.
 *
 * Motorista, veiculo e agregado sao UUID simples, sem @ManyToOne: essas entidades
 * pertencem ao ms-frota. A FK ainda existe no banco porque hoje os servicos
 * compartilham a mesma instancia, mas o codigo ja trata como referencia externa -
 * quando os bancos forem separados, so a constraint some.
 */
@Entity
@Table(name = "viagens")
public class Viagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_viagem")
    private Integer idViagem;

    /** Numero do manifesto no ERP. UNIQUE no banco: reimportar o mesmo arquivo nao duplica. */
    @Column(name = "id_manifesto", nullable = false, unique = true)
    private Integer idManifesto;

    @Column(name = "id_motorista", nullable = false)
    private UUID idMotorista;

    @Column(name = "id_veiculo", nullable = false)
    private UUID idVeiculo;

    @Column(name = "id_agregado")
    private UUID idAgregado;

    @Column(name = "importacao_id")
    private Long importacaoId;

    @Column(name = "data_viagem", nullable = false)
    private LocalDate dataViagem;

    /** "yyyy-MM", derivado de dataViagem: um arquivo cobre varios meses. */
    @Column(name = "mes_referencia", length = 7)
    private String mesReferencia;

    @Column(name = "filial")
    private String filial;

    @Column(name = "destino")
    private String destino;

    @Column(name = "reboque1")
    private String reboque1;

    @Column(name = "reboque2")
    private String reboque2;

    @Column(name = "reboque3")
    private String reboque3;

    // ── Carga ────────────────────────────────────────────────────────────────
    @Column(name = "kg_real")
    private BigDecimal kgReal;

    @Column(name = "kg_taxado")
    private BigDecimal kgTaxado;

    @Column(name = "m3")
    private BigDecimal m3;

    // ── Operacao ─────────────────────────────────────────────────────────────
    @Column(name = "km_saida")
    private Integer kmSaida;

    @Column(name = "km_chegada")
    private Integer kmChegada;

    @Column(name = "servicos")
    private Integer servicos;

    @Column(name = "servicos_finalizados")
    private Integer servicosFinalizados;

    @Column(name = "nfs")
    private Integer nfs;

    @Column(name = "coletas")
    private Integer coletas;

    @Column(name = "entregas")
    private Integer entregas;

    @Column(name = "despachos")
    private Integer despachos;

    @Column(name = "retiradas")
    private Integer retiradas;

    @Column(name = "coletas_reversa")
    private Integer coletasReversa;

    @Column(name = "percentual_efetividade")
    private BigDecimal percentualEfetividade;

    @Column(name = "percentual_aprov_veiculo")
    private BigDecimal percentualAprovVeiculo;

    // ── Valores e totais ─────────────────────────────────────────────────────
    @Column(name = "valor_frete")
    private BigDecimal valorFrete;

    @Column(name = "valor_fretes")
    private BigDecimal valorFretes;

    @Column(name = "valor_nf")
    private BigDecimal valorNf;

    @Column(name = "total_despesas")
    private BigDecimal totalDespesas;

    @Column(name = "saldo_despesas")
    private BigDecimal saldoDespesas;

    @Column(name = "saldo_a_pagar")
    private BigDecimal saldoAPagar;

    // ── Fechamento ───────────────────────────────────────────────────────────
    @Column(name = "status")
    private String status;

    @Column(name = "classificacao")
    private String classificacao;

    @Column(name = "observacoes_operacionais", columnDefinition = "text")
    private String observacoesOperacionais;

    @Column(name = "usuario_manifesto")
    private String usuarioManifesto;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private LocalDateTime criadoEm;

    public Viagem() {
    }

    public Integer getIdViagem() {
        return idViagem;
    }

    public void setIdViagem(Integer idViagem) {
        this.idViagem = idViagem;
    }

    public Integer getIdManifesto() {
        return idManifesto;
    }

    public void setIdManifesto(Integer idManifesto) {
        this.idManifesto = idManifesto;
    }

    public UUID getIdMotorista() {
        return idMotorista;
    }

    public void setIdMotorista(UUID idMotorista) {
        this.idMotorista = idMotorista;
    }

    public UUID getIdVeiculo() {
        return idVeiculo;
    }

    public void setIdVeiculo(UUID idVeiculo) {
        this.idVeiculo = idVeiculo;
    }

    public UUID getIdAgregado() {
        return idAgregado;
    }

    public void setIdAgregado(UUID idAgregado) {
        this.idAgregado = idAgregado;
    }

    public Long getImportacaoId() {
        return importacaoId;
    }

    public void setImportacaoId(Long importacaoId) {
        this.importacaoId = importacaoId;
    }

    public LocalDate getDataViagem() {
        return dataViagem;
    }

    public void setDataViagem(LocalDate dataViagem) {
        this.dataViagem = dataViagem;
    }

    public String getMesReferencia() {
        return mesReferencia;
    }

    public void setMesReferencia(String mesReferencia) {
        this.mesReferencia = mesReferencia;
    }

    public String getFilial() {
        return filial;
    }

    public void setFilial(String filial) {
        this.filial = filial;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
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

    public Integer getServicosFinalizados() {
        return servicosFinalizados;
    }

    public void setServicosFinalizados(Integer servicosFinalizados) {
        this.servicosFinalizados = servicosFinalizados;
    }

    public Integer getNfs() {
        return nfs;
    }

    public void setNfs(Integer nfs) {
        this.nfs = nfs;
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

    public BigDecimal getPercentualEfetividade() {
        return percentualEfetividade;
    }

    public void setPercentualEfetividade(BigDecimal percentualEfetividade) {
        this.percentualEfetividade = percentualEfetividade;
    }

    public BigDecimal getPercentualAprovVeiculo() {
        return percentualAprovVeiculo;
    }

    public void setPercentualAprovVeiculo(BigDecimal percentualAprovVeiculo) {
        this.percentualAprovVeiculo = percentualAprovVeiculo;
    }

    public BigDecimal getValorFrete() {
        return valorFrete;
    }

    public void setValorFrete(BigDecimal valorFrete) {
        this.valorFrete = valorFrete;
    }

    public BigDecimal getValorFretes() {
        return valorFretes;
    }

    public void setValorFretes(BigDecimal valorFretes) {
        this.valorFretes = valorFretes;
    }

    public BigDecimal getValorNf() {
        return valorNf;
    }

    public void setValorNf(BigDecimal valorNf) {
        this.valorNf = valorNf;
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

    public BigDecimal getSaldoAPagar() {
        return saldoAPagar;
    }

    public void setSaldoAPagar(BigDecimal saldoAPagar) {
        this.saldoAPagar = saldoAPagar;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getUsuarioManifesto() {
        return usuarioManifesto;
    }

    public void setUsuarioManifesto(String usuarioManifesto) {
        this.usuarioManifesto = usuarioManifesto;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

}
