package br.com.newe.ms_operacoes.service.importacao;

import java.math.BigDecimal;

/**
 * Catalogo dos campos do manifesto: coluna no CSV, tipo e exigencia.
 *
 * Fonte unica: o parser le o tipo daqui e a tela de mapeamento consome a mesma lista.
 */
public enum CampoManifesto {

    MANIFESTO("Manifesto", Tipo.INTEIRO, Nivel.OBRIGATORIO, false),
    FILIAL("Filial", Tipo.TEXTO, Nivel.IMPORTANTE, false),
    DATA("Data", Tipo.DATA, Nivel.OBRIGATORIO, false),
    NOME("Motorista", Tipo.TEXTO, Nivel.IMPORTANTE, false),
    CPF("CPF", Tipo.DOCUMENTO, Nivel.OBRIGATORIO, false, 11),
    PIS("PIS", Tipo.DOCUMENTO, Nivel.OPCIONAL, false),
    DATA_NASCIMENTO("Data de nascimento", Tipo.DATA, Nivel.OPCIONAL, false),
    ENDERECO("Endereço motorista", Tipo.TEXTO, Nivel.OPCIONAL, false),
    CEP("CEP Motorista", Tipo.DOCUMENTO, Nivel.OPCIONAL, false),
    BAIRRO("Bairro Motorista", Tipo.TEXTO, Nivel.OPCIONAL, false),
    CIDADE("Cidade Motorista", Tipo.TEXTO, Nivel.OPCIONAL, false),
    ESTADO("Estado Motorista", Tipo.TEXTO, Nivel.OPCIONAL, false),
    CHEFE_GUARNICAO("Chefe de Guarnição", Tipo.TEXTO, Nivel.OPCIONAL, false),
    VIGILANTE1("Vigilante 1", Tipo.TEXTO, Nivel.OPCIONAL, false),
    VIGILANTE2("Vigilante 2", Tipo.TEXTO, Nivel.OPCIONAL, false),
    AGREGADO("Agregado", Tipo.TEXTO, Nivel.IMPORTANTE, false),
    AGREGADO_DOCUMENTO("CPF/CNPJ Agregado", Tipo.DOCUMENTO, Nivel.IMPORTANTE, false),
    AGREGADO_PIS("PIS Agregado", Tipo.DOCUMENTO, Nivel.OPCIONAL, false),
    REGIME_FISCAL("Regime Fiscal", Tipo.TEXTO, Nivel.OPCIONAL, false),
    VEICULO("Veículo", Tipo.TEXTO, Nivel.OBRIGATORIO, false),
    REBOQUE1("Reboque 1", Tipo.TEXTO, Nivel.OPCIONAL, false),
    REBOQUE2("Reboque 2", Tipo.TEXTO, Nivel.OPCIONAL, false),
    REBOQUE3("Reboque 3", Tipo.TEXTO, Nivel.OPCIONAL, false),
    DESTINO("Destino", Tipo.TEXTO, Nivel.IMPORTANTE, false),
    KM_SAIDA("Km saída", Tipo.INTEIRO, Nivel.OPCIONAL, false),
    KM_CHEGADA("Km chegada", Tipo.INTEIRO, Nivel.OPCIONAL, false),
    CAPACIDADE_VEICULO("Capacidade Veículo", Tipo.DECIMAL, Nivel.IMPORTANTE, true),
    PERC_APROV_VEICULO("% Aprov. Veículo", Tipo.DECIMAL, Nivel.DERIVADO, false),
    SERVICOS("Serviços", Tipo.INTEIRO, Nivel.IMPORTANTE, true),
    NFS("NFs", Tipo.INTEIRO, Nivel.IMPORTANTE, true),
    KG_REAL("Kg Real", Tipo.DECIMAL, Nivel.IMPORTANTE, true),
    KG_TAXADO("Kg Taxado", Tipo.DECIMAL, Nivel.IMPORTANTE, true),
    M3("M3", Tipo.DECIMAL, Nivel.OPCIONAL, false),
    COLETAS("Coletas", Tipo.INTEIRO, Nivel.OPCIONAL, false),
    ENTREGAS("Entregas", Tipo.INTEIRO, Nivel.OPCIONAL, false),
    DESPACHOS("Despachos", Tipo.INTEIRO, Nivel.OPCIONAL, false),
    RETIRADAS("Retiradas", Tipo.INTEIRO, Nivel.OPCIONAL, false),
    COLETAS_REVERSA("Coletas Reversa", Tipo.INTEIRO, Nivel.OPCIONAL, false),
    SERVICOS_FINALIZADOS("Serviços Finalizados", Tipo.INTEIRO, Nivel.IMPORTANTE, false),
    PERC_EFETIVIDADE("% Efetividade", Tipo.DECIMAL, Nivel.DERIVADO, false),
    VALE_FRETE("Vale frete", Tipo.DECIMAL, Nivel.DERIVADO, false),
    VALOR_NF("Valor NF", Tipo.DECIMAL, Nivel.IMPORTANTE, true),
    VALOR_FRETES("Valor Fretes", Tipo.DECIMAL, Nivel.IMPORTANTE, true),
    VALOR_FRETE("Valor Frete", Tipo.DECIMAL, Nivel.IMPORTANTE, true),
    COMBUSTIVEL("Combustível", Tipo.DECIMAL, Nivel.OPCIONAL, false),
    PEDAGIO("Pedágio", Tipo.DECIMAL, Nivel.OPCIONAL, false),
    DIARIA("Diária", Tipo.DECIMAL, Nivel.OPCIONAL, false),
    ADICIONAIS("Adicionais", Tipo.DECIMAL, Nivel.OPCIONAL, false),
    DESCONTOS("Descontos", Tipo.DECIMAL, Nivel.OPCIONAL, false),
    ADIANTAMENTO("Adiantamento", Tipo.DECIMAL, Nivel.OPCIONAL, false),
    DESPESAS("Despesas", Tipo.DECIMAL, Nivel.OPCIONAL, false),
    TOTAL_DESPESAS("Total Despesas", Tipo.DECIMAL, Nivel.OPCIONAL, false),
    SALDO_DESPESAS("Saldo Despesas", Tipo.DECIMAL, Nivel.OPCIONAL, false),
    INSS("INSS", Tipo.DECIMAL, Nivel.OPCIONAL, false),
    SEST_SENAT("SEST/SENAT", Tipo.DECIMAL, Nivel.OPCIONAL, false),
    IR("IR", Tipo.DECIMAL, Nivel.OPCIONAL, false),
    SALDO_A_PAGAR("Saldo a pagar", Tipo.DECIMAL, Nivel.DERIVADO, false),
    CLASSIFICACAO("Classificação", Tipo.TEXTO, Nivel.OPCIONAL, false),
    OBSERVACOES("Observações operacionais", Tipo.TEXTO, Nivel.OPCIONAL, false),
    STATUS("Status", Tipo.TEXTO, Nivel.IMPORTANTE, false),
    USUARIO("Usuário", Tipo.TEXTO, Nivel.IMPORTANTE, false);

    public enum Tipo { TEXTO, DOCUMENTO, INTEIRO, DECIMAL, DATA }

    /** OBRIGATORIO barra a linha; IMPORTANTE vira pendencia; o resto so informa. */
    public enum Nivel { OBRIGATORIO, IMPORTANTE, OPCIONAL, DERIVADO }

    private final String coluna;
    private final Tipo tipo;
    private final Nivel nivel;
    private final boolean zeroAusente;
    private final Integer tamanhoExato;

    CampoManifesto(String coluna, Tipo tipo, Nivel nivel, boolean zeroAusente) {
        this(coluna, tipo, nivel, zeroAusente, null);
    }

    CampoManifesto(String coluna, Tipo tipo, Nivel nivel, boolean zeroAusente, Integer tamanhoExato) {
        this.coluna = coluna;
        this.tipo = tipo;
        this.nivel = nivel;
        this.zeroAusente = zeroAusente;
        this.tamanhoExato = tamanhoExato;
    }

    public String coluna() {
        return coluna;
    }

    public Tipo tipo() {
        return tipo;
    }

    public Nivel nivel() {
        return nivel;
    }

    /** Colunas onde o CSV escreve 0,00 no lugar de "nao informado". */
    public boolean zeroAusente() {
        return zeroAusente;
    }

    /** Quantidade exata de digitos, quando o campo tem uma. Hoje so o CPF. */
    public Integer tamanhoExato() {
        return tamanhoExato;
    }

    /** O que a celula deveria conter, para a tela explicar o erro. */
    public String formatoEsperado() {
        return switch (tipo) {
            case TEXTO -> "Texto";
            case DOCUMENTO -> tamanhoExato == null
                    ? "Somente digitos"
                    : tamanhoExato + " digitos";
            case INTEIRO -> "Numero inteiro";
            case DECIMAL -> "Numero decimal (1.234,56)";
            case DATA -> "Data no formato DD/MM/AAAA";
        };
    }

    /** Classifica a celula sem lancar excecao. */
    public AvaliacaoCampo avaliar(String valorCru) {
        if (ManifestoCsvConfig.textoOuNulo(valorCru) == null) {
            return new AvaliacaoCampo(this, valorCru, EstadoCampo.VAZIO, null, null);
        }
        try {
            Object valor = converter(valorCru);
            if (valor == null) {
                return new AvaliacaoCampo(this, valorCru, EstadoCampo.NAO_CONVERTE, null,
                        "Valor sem digitos: " + valorCru);
            }
            if (tamanhoExato != null && valor instanceof String digitos && digitos.length() != tamanhoExato) {
                return new AvaliacaoCampo(this, valorCru, EstadoCampo.NAO_CONVERTE, null,
                        "Esperado " + tamanhoExato + " digitos, veio com " + digitos.length());
            }
            if (zeroAusente && ehZero(valor)) {
                // O zero segue para a gravacao como veio no arquivo; o estado so sinaliza.
                return new AvaliacaoCampo(this, valorCru, EstadoCampo.AUSENTE_COMO_ZERO, valor, null);
            }
            return new AvaliacaoCampo(this, valorCru, EstadoCampo.OK, valor, null);
        } catch (IllegalArgumentException e) {
            return new AvaliacaoCampo(this, valorCru, EstadoCampo.NAO_CONVERTE, null, e.getMessage());
        }
    }

    private Object converter(String valorCru) {
        return switch (tipo) {
            case TEXTO -> ManifestoCsvConfig.textoOuNulo(valorCru);
            case DOCUMENTO -> ManifestoCsvConfig.digitosOuNulo(valorCru);
            case INTEIRO -> inteiroExato(valorCru);
            case DECIMAL -> ManifestoCsvConfig.decimalBrOuNulo(valorCru);
            case DATA -> ManifestoCsvConfig.dataOuNula(valorCru);
        };
    }

    /**
     * Aceita "1,00" (as contagens vem assim no CSV), mas recusa "12,5" em vez de
     * truncar para 12 sem avisar.
     */
    private static Integer inteiroExato(String valorCru) {
        BigDecimal decimal = ManifestoCsvConfig.decimalBrOuNulo(valorCru);
        if (decimal == null) {
            return null;
        }
        try {
            return decimal.intValueExact();
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Numero inteiro invalido: " + valorCru.trim(), e);
        }
    }

    private static boolean ehZero(Object valor) {
        if (valor instanceof BigDecimal decimal) {
            return decimal.signum() == 0;
        }
        return valor instanceof Integer inteiro && inteiro == 0;
    }
}
