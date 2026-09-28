package br.com.newe.ms_operacoes.service.importacao;

import java.math.BigDecimal;
import java.util.function.Function;

/**
 * Liga cada coluna de desembolso do CSV a uma descricao de tipos_custo.
 */
public enum CustoDoManifesto {

    VALE_FRETE("Vale frete", LinhaManifesto::valeFrete),
    COMBUSTIVEL("Combustível", LinhaManifesto::combustivel),
    PEDAGIO("Pedágio", LinhaManifesto::pedagio),
    DIARIA("Diária", LinhaManifesto::diaria),
    ADICIONAIS("Adicionais", LinhaManifesto::adicionais),
    DESCONTOS("Descontos", LinhaManifesto::descontos),
    ADIANTAMENTO("Adiantamento", LinhaManifesto::adiantamento),
    DESPESAS("Despesas", LinhaManifesto::despesas),
    INSS("INSS", LinhaManifesto::inss),
    SEST_SENAT("SEST/SENAT", LinhaManifesto::sestSenat),
    IR("IR", LinhaManifesto::ir);

    private final String descricao;
    private final Function<LinhaManifesto, BigDecimal> extrator;

    CustoDoManifesto(String descricao, Function<LinhaManifesto, BigDecimal> extrator) {
        this.descricao = descricao;
        this.extrator = extrator;
    }

    /** Descricao em tipos_custo.descricao (UNIQUE desde a V3). */
    public String descricao() {
        return descricao;
    }

    public BigDecimal valorEm(LinhaManifesto linha) {
        return extrator.apply(linha);
    }
}
