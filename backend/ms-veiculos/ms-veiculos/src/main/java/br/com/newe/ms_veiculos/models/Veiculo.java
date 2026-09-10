package br.com.newe.ms_veiculos.models;

import jakarta.persistence.*;

@Entity
@Table(name = "veiculos")
public class Veiculo {
    @Id
    private long id;
}