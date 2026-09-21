package br.com.newe.ms_frota.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Quem dirige o veiculo. Nao confundir com {@link Agregado}, que e o contratado
 * dono do veiculo: no arquivo real da Newelog sao populacoes diferentes
 * (150 motoristas para 193 agregados), embora coincidam quando o motorista
 * e autonomo.
 */
@Entity
@Table(name = "motoristas")
public class Motorista {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_motorista")
    private UUID idMotorista;

    @Column(name = "nome", nullable = false)
    private String nome;

    /** Somente digitos, 11 posicoes. */
    @Column(name = "cpf", nullable = false, unique = true)
    private String cpf;

    @Column(name = "pis")
    private String pis;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "cep")
    private String cep;

    @Column(name = "logradouro")
    private String logradouro;

    @Column(name = "numero")
    private String numero;

    @Column(name = "bairro")
    private String bairro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cidade")
    @JsonIgnore
    private Cidade cidade;

    /**
     * Mesma coluna, so leitura. Existe para o JSON expor o id sem tocar no proxy
     * LAZY acima - o que exigiria sessao aberta na serializacao.
     */
    @Column(name = "id_cidade", insertable = false, updatable = false)
    private Integer idCidade;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private LocalDateTime criadoEm;

    public Motorista() {
    }

    public UUID getIdMotorista() {
        return idMotorista;
    }

    public void setIdMotorista(UUID idMotorista) {
        this.idMotorista = idMotorista;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getPis() {
        return pis;
    }

    public void setPis(String pis) {
        this.pis = pis;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public Cidade getCidade() {
        return cidade;
    }

    public void setCidade(Cidade cidade) {
        this.cidade = cidade;
    }

    @JsonProperty("idCidade")
    public Integer getIdCidade() {
        return idCidade;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

}
