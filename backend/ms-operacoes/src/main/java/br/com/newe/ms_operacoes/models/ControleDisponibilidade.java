package br.com.newe.ms_operacoes.models;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Dias do motorista no mes, gravados no tratamento da importacao. Uma linha por
 * (motorista, mes), e so para motorista com viagem no mes.
 *
 * Mapeia a tabela como ela ja existe no Supabase, que nao pode ser alterada: nao
 * ha coluna para utilizacao, faixa ou situacao. Por isso a classificacao sai
 * destes dias na consulta (SituacaoMotoristaService.listar).
 *
 * idMotorista e UUID simples, sem @ManyToOne: motorista pertence ao ms-frota
 * (mesma convencao de Viagem). Como em viagens, a FK ainda existe no banco.
 *
 * Os nomes de coluna (id_controle, mes_ano, dias_em_operacao) sao os da tabela
 * existente; os campos seguem os nomes usados no resto do codigo.
 */
@Entity
@Table(name = "controle_disponibilidade", uniqueConstraints = @UniqueConstraint(
        name = "controle_disponibilidade_id_motorista_mes_ano_key",
        columnNames = { "id_motorista", "mes_ano" }))
public class ControleDisponibilidade {

    /** Gerado pelo Hibernate: funciona com ou sem default no banco. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_controle")
    private UUID id;

    @Column(name = "id_motorista", nullable = false)
    private UUID idMotorista;

    /** "yyyy-MM". */
    @Column(name = "mes_ano", nullable = false, length = 7)
    private String mesReferencia;

    @Column(name = "dias_disponiveis", nullable = false)
    private Integer diasDisponiveis;

    @Column(name = "dias_em_operacao", nullable = false)
    private Integer diasOperacao;

    public ControleDisponibilidade() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
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
}
