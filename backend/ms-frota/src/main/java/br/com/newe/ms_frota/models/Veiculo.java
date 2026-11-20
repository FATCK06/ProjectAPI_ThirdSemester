package br.com.newe.ms_frota.models;

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
 * Tabela veiculos. A importacao de manifestos cria o veiculo minimo (placa,
 * lotacao, agregado) porque viagens.id_veiculo e NOT NULL; a importacao de
 * veiculos completa o cadastro.
 *
 * As chaves estrangeiras ficam como id simples, sem relacionamento JPA: quem le
 * veiculo nunca precisa navegar para a cidade ou o motorista.
 */
@Entity
@Table(name = "veiculos")
public class Veiculo {

    public static final String STATUS_ATIVO = "Ativo";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_veiculo")
    private UUID idVeiculo;

    @Column(name = "placa", nullable = false, unique = true)
    private String placa;

    @Column(name = "id_tipo")
    private Integer idTipo;

    @Column(name = "id_motorista_padrao")
    private UUID idMotoristaPadrao;

    @Column(name = "id_agregado")
    private UUID idAgregado;

    @Column(name = "id_cidade_licenciamento")
    private Integer idCidadeLicenciamento;

    @Column(name = "marca")
    private String marca;

    @Column(name = "modelo")
    private String modelo;

    @Column(name = "cor")
    private String cor;

    @Column(name = "combustivel")
    private String combustivel;

    @Column(name = "numero_frota")
    private String numeroFrota;

    @Column(name = "ano_fabricacao")
    private Integer anoFabricacao;

    @Column(name = "ano_modelo")
    private Integer anoModelo;

    @Column(name = "renavam")
    private String renavam;

    @Column(name = "chassi")
    private String chassi;

    @Column(name = "tipo_carroceria")
    private String tipoCarroceria;

    @Column(name = "tipo_proprietario")
    private String tipoProprietario;

    @Column(name = "nome_proprietario")
    private String nomeProprietario;

    /** Continua mesmo com id_agregado: o CSV de veiculos traz agregado sem documento. */
    @Column(name = "nome_agregado")
    private String nomeAgregado;

    @Column(name = "tabela_agregado")
    private String tabelaAgregado;

    @Column(name = "data_compra")
    private LocalDate dataCompra;

    @Column(name = "valor_compra")
    private BigDecimal valorCompra;

    @Column(name = "data_venda")
    private LocalDate dataVenda;

    @Column(name = "valor_venda")
    private BigDecimal valorVenda;

    @Column(name = "pbt")
    private BigDecimal pbt;

    @Column(name = "tara")
    private BigDecimal tara;

    /** Volume do baú, em m³. */
    @Column(name = "capacidade")
    private BigDecimal capacidade;

    /** Quanto de carga leva, em peso. É o que o manifesto chama de "Capacidade Veículo". */
    @Column(name = "capacidade_lotacao")
    private BigDecimal capacidadeLotacao;

    @Column(name = "numero_autorizacao")
    private String numeroAutorizacao;

    @Column(name = "validade_guarda")
    private LocalDate validadeGuarda;

    @Column(name = "status_veiculo")
    private String statusVeiculo;

    @Column(name = "lote_fechamento_ciot")
    private String loteFechamentoCiot;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private LocalDateTime criadoEm;

    public Veiculo() {
    }

    public UUID getIdVeiculo() { return idVeiculo; }
    public void setIdVeiculo(UUID idVeiculo) { this.idVeiculo = idVeiculo; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public Integer getIdTipo() { return idTipo; }
    public void setIdTipo(Integer idTipo) { this.idTipo = idTipo; }

    public UUID getIdMotoristaPadrao() { return idMotoristaPadrao; }
    public void setIdMotoristaPadrao(UUID idMotoristaPadrao) { this.idMotoristaPadrao = idMotoristaPadrao; }

    public UUID getIdAgregado() { return idAgregado; }
    public void setIdAgregado(UUID idAgregado) { this.idAgregado = idAgregado; }

    public Integer getIdCidadeLicenciamento() { return idCidadeLicenciamento; }
    public void setIdCidadeLicenciamento(Integer idCidadeLicenciamento) { this.idCidadeLicenciamento = idCidadeLicenciamento; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getCor() { return cor; }
    public void setCor(String cor) { this.cor = cor; }

    public String getCombustivel() { return combustivel; }
    public void setCombustivel(String combustivel) { this.combustivel = combustivel; }

    public String getNumeroFrota() { return numeroFrota; }
    public void setNumeroFrota(String numeroFrota) { this.numeroFrota = numeroFrota; }

    public Integer getAnoFabricacao() { return anoFabricacao; }
    public void setAnoFabricacao(Integer anoFabricacao) { this.anoFabricacao = anoFabricacao; }

    public Integer getAnoModelo() { return anoModelo; }
    public void setAnoModelo(Integer anoModelo) { this.anoModelo = anoModelo; }

    public String getRenavam() { return renavam; }
    public void setRenavam(String renavam) { this.renavam = renavam; }

    public String getChassi() { return chassi; }
    public void setChassi(String chassi) { this.chassi = chassi; }

    public String getTipoCarroceria() { return tipoCarroceria; }
    public void setTipoCarroceria(String tipoCarroceria) { this.tipoCarroceria = tipoCarroceria; }

    public String getTipoProprietario() { return tipoProprietario; }
    public void setTipoProprietario(String tipoProprietario) { this.tipoProprietario = tipoProprietario; }

    public String getNomeProprietario() { return nomeProprietario; }
    public void setNomeProprietario(String nomeProprietario) { this.nomeProprietario = nomeProprietario; }

    public String getNomeAgregado() { return nomeAgregado; }
    public void setNomeAgregado(String nomeAgregado) { this.nomeAgregado = nomeAgregado; }

    public String getTabelaAgregado() { return tabelaAgregado; }
    public void setTabelaAgregado(String tabelaAgregado) { this.tabelaAgregado = tabelaAgregado; }

    public LocalDate getDataCompra() { return dataCompra; }
    public void setDataCompra(LocalDate dataCompra) { this.dataCompra = dataCompra; }

    public BigDecimal getValorCompra() { return valorCompra; }
    public void setValorCompra(BigDecimal valorCompra) { this.valorCompra = valorCompra; }

    public LocalDate getDataVenda() { return dataVenda; }
    public void setDataVenda(LocalDate dataVenda) { this.dataVenda = dataVenda; }

    public BigDecimal getValorVenda() { return valorVenda; }
    public void setValorVenda(BigDecimal valorVenda) { this.valorVenda = valorVenda; }

    public BigDecimal getPbt() { return pbt; }
    public void setPbt(BigDecimal pbt) { this.pbt = pbt; }

    public BigDecimal getTara() { return tara; }
    public void setTara(BigDecimal tara) { this.tara = tara; }

    public BigDecimal getCapacidade() { return capacidade; }
    public void setCapacidade(BigDecimal capacidade) { this.capacidade = capacidade; }

    public BigDecimal getCapacidadeLotacao() { return capacidadeLotacao; }
    public void setCapacidadeLotacao(BigDecimal capacidadeLotacao) { this.capacidadeLotacao = capacidadeLotacao; }

    public String getNumeroAutorizacao() { return numeroAutorizacao; }
    public void setNumeroAutorizacao(String numeroAutorizacao) { this.numeroAutorizacao = numeroAutorizacao; }

    public LocalDate getValidadeGuarda() { return validadeGuarda; }
    public void setValidadeGuarda(LocalDate validadeGuarda) { this.validadeGuarda = validadeGuarda; }

    public String getStatusVeiculo() { return statusVeiculo; }
    public void setStatusVeiculo(String statusVeiculo) { this.statusVeiculo = statusVeiculo; }

    public String getLoteFechamentoCiot() { return loteFechamentoCiot; }
    public void setLoteFechamentoCiot(String loteFechamentoCiot) { this.loteFechamentoCiot = loteFechamentoCiot; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
}
