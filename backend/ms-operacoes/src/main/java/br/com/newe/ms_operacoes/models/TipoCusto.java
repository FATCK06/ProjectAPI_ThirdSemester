package br.com.newe.ms_operacoes.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Categoria de despesa de uma viagem. */
@Entity
@Table(name = "tipos_custo")
public class TipoCusto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_custo")
    private Integer idTipoCusto;

    /** UNIQUE: e por ela que a importacao encontra o id. */
    @Column(name = "descricao", nullable = false)
    private String descricao;

    public Integer getIdTipoCusto() {
        return idTipoCusto;
    }

    public void setIdTipoCusto(Integer idTipoCusto) {
        this.idTipoCusto = idTipoCusto;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
