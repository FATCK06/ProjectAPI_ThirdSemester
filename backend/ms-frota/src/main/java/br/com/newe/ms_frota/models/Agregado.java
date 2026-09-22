package br.com.newe.ms_frota.models;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * O contratado dono do veiculo. Pode ser pessoa fisica (CPF, 11 digitos) ou
 * juridica (CNPJ, 14) - no arquivo real da Newelog a divisao e quase meio a meio.
 * O vinculo com o motorista muda a cada manifesto, por isso ele vive em viagens
 * (no ms-operacoes) e nao aqui.
 */
@Entity
@Table(name = "agregados")
public class Agregado {

    public static final String PESSOA_FISICA = "PF";
    public static final String PESSOA_JURIDICA = "PJ";

    private static final int TAMANHO_CNPJ = 14;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_agregado")
    private UUID idAgregado;

    /** Somente digitos: 11 = CPF, 14 = CNPJ. */
    @Column(name = "documento", nullable = false, unique = true, length = 14)
    private String documento;

    @Column(name = "tipo_pessoa", nullable = false, length = 2)
    private String tipoPessoa;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "pis", length = 11)
    private String pis;

    /** Ex.: "Simples nacional", "MEI". Nulo na maioria dos registros. */
    @Column(name = "regime_fiscal", length = 50)
    private String regimeFiscal;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private LocalDateTime criadoEm;

    public Agregado() {
    }

    /** Deriva PF/PJ do tamanho do documento, como exige o CHECK da tabela. */
    public static String tipoPessoaDe(String documento) {
        if (documento == null) {
            return null;
        }
        return documento.length() == TAMANHO_CNPJ ? PESSOA_JURIDICA : PESSOA_FISICA;
    }

    public UUID getIdAgregado() {
        return idAgregado;
    }

    public void setIdAgregado(UUID idAgregado) {
        this.idAgregado = idAgregado;
    }

    public String getDocumento() {
        return documento;
    }

    /** Mantem tipoPessoa coerente com o documento: o banco rejeita a combinacao errada. */
    public void setDocumento(String documento) {
        this.documento = documento;
        this.tipoPessoa = tipoPessoaDe(documento);
    }

    public String getTipoPessoa() {
        return tipoPessoa;
    }

    public void setTipoPessoa(String tipoPessoa) {
        this.tipoPessoa = tipoPessoa;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPis() {
        return pis;
    }

    public void setPis(String pis) {
        this.pis = pis;
    }

    public String getRegimeFiscal() {
        return regimeFiscal;
    }

    public void setRegimeFiscal(String regimeFiscal) {
        this.regimeFiscal = regimeFiscal;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

}
