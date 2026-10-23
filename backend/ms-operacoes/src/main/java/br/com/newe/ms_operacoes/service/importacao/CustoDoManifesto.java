package br.com.newe.ms_operacoes.service.importacao;

import java.math.BigDecimal;
import java.util.function.Function;

/**
 * Liga cada coluna de desembolso do CSV a uma descricao de tipos_custo.
 *
 * So entra o que e custo da operacao. Adiantamento (parte do vale frete paga
 * antes) e INSS, SEST/SENAT e IR (retidos do agregado) mudam quando e quanto o
 * motorista recebe, nao quanto a viagem custou: somados aqui contariam em dobro.
 * Esses valores continuam no manifesto e em saldo_a_pagar.
 */
public enum CustoDoManifesto {

    COMBUSTIVEL("Combustível", LinhaManifesto::combustivel),
    PEDAGIO("Pedágio", LinhaManifesto::pedagio),
    DIARIA("Diária", LinhaManifesto::diaria),
    ADICIONAIS("Adicionais", LinhaManifesto::adicionais),
    DESCONTOS("Descontos", LinhaManifesto::descontos),
    DESPESAS("Despesas", LinhaManifesto::despesas);

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
