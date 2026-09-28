package br.com.newe.ms_frota.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Mapeamento parcial da tabela veiculos: so o que a importacao de manifestos
 * precisa hoje (o CSV traz apenas a placa, e viagens.id_veiculo e NOT NULL,
 * entao o veiculo e criado sob demanda).
 *
 * A tabela tem ~30 colunas a mais - renavam, chassi, tara, pbt, dados de compra
 * e venda, licenciamento - que serao mapeadas quando existir o CRUD de frota.
 * Coluna nao mapeada nao quebra o ddl-auto=validate.
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

    @Column(name = "marca")
    private String marca;

    @Column(name = "modelo")
    private String modelo;

    @Column(name = "numero_frota")
    private String numeroFrota;

    @Column(name = "capacidade")
    private BigDecimal capacidade;

    @Column(name = "nome_agregado")
    private String nomeAgregado;

    @Column(name = "status_veiculo")
    private String statusVeiculo;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private LocalDateTime criadoEm;

    public Veiculo() {
    }

    public UUID getIdVeiculo() {
        return idVeiculo;
    }

    public void setIdVeiculo(UUID idVeiculo) {
        this.idVeiculo = idVeiculo;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getNumeroFrota() {
        return numeroFrota;
    }

    public void setNumeroFrota(String numeroFrota) {
        this.numeroFrota = numeroFrota;
    }

    public BigDecimal getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(BigDecimal capacidade) {
        this.capacidade = capacidade;
    }

    public String getNomeAgregado() {
        return nomeAgregado;
    }

    public void setNomeAgregado(String nomeAgregado) {
        this.nomeAgregado = nomeAgregado;
    }

    public String getStatusVeiculo() {
        return statusVeiculo;
    }

    public void setStatusVeiculo(String statusVeiculo) {
        this.statusVeiculo = statusVeiculo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

}
