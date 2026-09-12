package br.com.logistica.msveiculos.models; 

import jakarta.persistence.*;

@Entity
@Table(name = "cidades")
public class Cidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cidade")
    private Integer idCidade;

    @Column(name = "nome_cidade", nullable = false)
    private String nomeCidade;

}