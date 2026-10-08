package br.com.newe.ms_operacoes.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.newe.ms_operacoes.models.enums.FaixaUtilizacao;
import br.com.newe.ms_operacoes.models.enums.SituacaoMotorista;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Situacao do motorista em um mes, gravada no tratamento da importacao e so
 * lida na consulta. Uma linha por (motorista, mes), e so para motorista com
 * viagem no mes.
 *
 * idMotorista e UUID simples, sem @ManyToOne: motorista pertence ao ms-frota
 * (mesma convencao de Viagem).
 */
@Entity
@Table(name = "controle_disponibilidade", uniqueConstraints = @UniqueConstraint(
        name = "controle_disponibilidade_motorista_mes_uk",
        columnNames = { "id_motorista", "mes_referencia" }))
public class ControleDisponibilidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "id_motorista", nullable = false)
    private UUID idMotorista;

    /** "yyyy-MM". */
    @Column(name = "mes_referencia", nullable = false, length = 7)
    private String mesReferencia;

    @Column(name = "dias_disponiveis", nullable = false)
    private Integer diasDisponiveis;

    @Column(name = "dias_operacao", nullable = false)
    private Integer diasOperacao;

    /** Nula quando nao ha dia disponivel no mes. Pode passar de 100. */
    @Column(name = "utilizacao", precision = 7, scale = 2)
    private BigDecimal utilizacao;

    /** Nula quando nao ha dia disponivel no mes. */
    @Enumerated(EnumType.STRING)
    @Column(name = "faixa_utilizacao", length = 20)
    private FaixaUtilizacao faixaUtilizacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", nullable = false, length = 30)
    private SituacaoMotorista situacao;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    public ControleDisponibilidade() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UUID getIdMotorista() {
        return idMotorista;
    }

    public void setIdMotorista(UUID idMotorista) {
        this.idMotorista = idMotorista;
    }

    public String getMesReferencia() {
        return mesReferencia;
    }

    public void setMesReferencia(String mesReferencia) {
        this.mesReferencia = mesReferencia;
    }

    public Integer getDiasDisponiveis() {
        return diasDisponiveis;
    }

    public void setDiasDisponiveis(Integer diasDisponiveis) {
        this.diasDisponiveis = diasDisponiveis;
    }

    public Integer getDiasOperacao() {
        return diasOperacao;
    }

    public void setDiasOperacao(Integer diasOperacao) {
        this.diasOperacao = diasOperacao;
    }

    public BigDecimal getUtilizacao() {
        return utilizacao;
    }

    public void setUtilizacao(BigDecimal utilizacao) {
        this.utilizacao = utilizacao;
    }

    public FaixaUtilizacao getFaixaUtilizacao() {
        return faixaUtilizacao;
    }

    public void setFaixaUtilizacao(FaixaUtilizacao faixaUtilizacao) {
        this.faixaUtilizacao = faixaUtilizacao;
    }

    public SituacaoMotorista getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoMotorista situacao) {
        this.situacao = situacao;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}
