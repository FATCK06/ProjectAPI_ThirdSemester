package br.com.newe.ms_operacoes.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/** Um desembolso de uma viagem, classificado por {@link TipoCusto}. */
@Entity
@Table(name = "viagem_custos")
@IdClass(ViagemCusto.Chave.class)
public class ViagemCusto {

    // PK composta (id_viagem, id_tipo_custo): ja garante um valor por categoria
    // por viagem, e e o que permite reprocessar sem duplicar.
    @Id
    @Column(name = "id_viagem")
    private Integer idViagem;

    @Id
    @Column(name = "id_tipo_custo")
    private Integer idTipoCusto;

    @Column(name = "valor_desembolsado", nullable = false)
    private BigDecimal valorDesembolsado;

    protected ViagemCusto() {
    }

    public ViagemCusto(Integer idViagem, Integer idTipoCusto, BigDecimal valorDesembolsado) {
        this.idViagem = idViagem;
        this.idTipoCusto = idTipoCusto;
        this.valorDesembolsado = valorDesembolsado;
    }

    public Integer getIdViagem() {
        return idViagem;
    }

    public Integer getIdTipoCusto() {
        return idTipoCusto;
    }

    public BigDecimal getValorDesembolsado() {
        return valorDesembolsado;
    }

    public static class Chave implements Serializable {

        private Integer idViagem;
        private Integer idTipoCusto;

        public Chave() {
        }

        public Chave(Integer idViagem, Integer idTipoCusto) {
            this.idViagem = idViagem;
            this.idTipoCusto = idTipoCusto;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Chave outra)) {
                return false;
            }
            return Objects.equals(idViagem, outra.idViagem)
                    && Objects.equals(idTipoCusto, outra.idTipoCusto);
        }

        @Override
        public int hashCode() {
            return Objects.hash(idViagem, idTipoCusto);
        }
    }
}
