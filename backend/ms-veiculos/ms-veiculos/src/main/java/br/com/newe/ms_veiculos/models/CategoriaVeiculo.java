package br.com.logistica.msveiculos.models;

import jakarta.persistence.*;

@Entity
@Table(name = "categorias_veiculos")
public class CategoriaVeiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Integer idCategoria;

    @Column(nullable = false)
    private String descricao;
}