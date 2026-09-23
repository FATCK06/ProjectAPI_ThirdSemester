package br.com.newe.ms_indicadores.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "indicadores_motorista")
@Data
public class IndicadoresMotoristaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "motorista_id", nullable = false)
    private Long motoristaId;

    @Column(name = "dias_operacao")
    private Integer diasOperacao;

    @Column(name = "dias_disponiveis")
    private Integer diasDisponiveis;

    @Column(name = "numero_viagens")
    private Integer numeroViagens;

    @Column(name = "valor_frete", precision = 15, scale = 2)
    private BigDecimal valorFrete;

    @Column(name = "custos_operacao", precision = 15, scale = 2)
    private BigDecimal custosOperacao;

    @Column(name = "utilizacao", precision = 10, scale = 4)
    private BigDecimal utilizacao;

    @Column(name = "rentabilidade", precision = 15, scale = 2)
    private BigDecimal rentabilidade;

    @Column(name = "rentabilidade_media_viagem", precision = 15, scale = 2)
    private BigDecimal rentabilidadeMediaViagem;

}