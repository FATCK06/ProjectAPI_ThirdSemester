package br.com.logistica.msveiculos.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "veiculos")
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_veiculo")
    private UUID idVeiculo;

    @Column(nullable = false,unique = true)
    private String placa;

    @ManyToOne
    @JoinColumn(name = "id_categoria")
    private CategoriaVeiculo categoria;

    @Column(name = "id_motorista_padrao")
    private UUID idMotoristaPadrao;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private LocalDateTime criadoEm;

    private String marca;
    private String modelo;
    private String cor;
    private String combustivel;

    @Column(name = "numero_frota")
    private String numeroFrota;

    @Column(name = "ano_fabricacao")
    private Integer anoFabricacao;

    @Column(name = "ano_modelo")
    private Integer anoModelo;

    @Column(unique = true)
    private String renavam;

    @Column(unique = true)
    private String chassi;

    @ManyToOne
    @JoinColumn(name = "id_cidade_licenciamento")
    private Cidade cidadeLicenciamento;

    @Column(name = "tipo_carroceria")
    private String tipoCarroceria;

    @Column(name = "tipo_proprietario")
    private String tipoProprietario;

    @Column(name = "nome_proprietario")
    private String nomeProprietario;

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

    private BigDecimal pbt;
    private BigDecimal tara;
    private BigDecimal capacidade;

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
}