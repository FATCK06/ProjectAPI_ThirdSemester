package br.com.newe.ms_veiculos.service.importacao;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.apache.commons.csv.CSVFormat;

/**
 * Convencoes assumidas para o CSV de manifestos, sem um arquivo real de exemplo disponivel.
 * Unico ponto de ajuste quando o formato real do time divergir (delimitador, encoding, data, decimal).
 */
public final class ManifestoCsvConfig {

    public static final Charset CHARSET = StandardCharsets.UTF_8;
    public static final int TAMANHO_CHUNK = 50;

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static final CSVFormat CSV_FORMAT = CSVFormat.Builder.create(CSVFormat.DEFAULT)
            .setDelimiter(';')
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreHeaderCase(true)
            .setTrim(true)
            .build();

    // Colunas do CSV (nomes exatos do cabecalho, conforme especificado pelo time)
    public static final String COL_NOME = "Nome";
    public static final String COL_CPF = "CPF";
    public static final String COL_PIS = "PIS";
    public static final String COL_DATA_NASCIMENTO = "Data de nascimento";
    public static final String COL_ENDERECO = "Endereço";
    public static final String COL_CEP = "CEP";
    public static final String COL_BAIRRO = "Bairro";
    public static final String COL_CIDADE = "Cidade";
    public static final String COL_ESTADO = "Estado (UF)";

    public static final String COL_MANIFESTO = "Manifesto";
    public static final String COL_FILIAL = "Filial";
    public static final String COL_DATA = "Data";
    public static final String COL_VEICULO = "Veículo";
    public static final String COL_REBOQUE1 = "Reboque 1";
    public static final String COL_REBOQUE2 = "Reboque 2";
    public static final String COL_REBOQUE3 = "Reboque 3";
    public static final String COL_DESTINO = "Destino";
    public static final String COL_KM_SAIDA = "Km saída";
    public static final String COL_KM_CHEGADA = "Km chegada";
    public static final String COL_SERVICOS = "Serviços";
    public static final String COL_NFS = "NFs";
    public static final String COL_KG_REAL = "Kg Real";
    public static final String COL_KG_TAXADO = "Kg Taxado";
    public static final String COL_M3 = "M3";
    public static final String COL_CAPACIDADE_VEICULO = "Capacidade Veículo";
    public static final String COL_PERC_APROV_VEICULO = "% Aprov. Veículo";
    public static final String COL_VALE_FRETE = "Vale frete";
    public static final String COL_VALOR_NF = "Valor NF";
    public static final String COL_VALOR_FRETES = "Valor Fretes";
    public static final String COL_VALOR_FRETE = "Valor Frete";
    public static final String COL_COMBUSTIVEL = "Combustível";
    public static final String COL_PEDAGIO = "Pedágio";
    public static final String COL_DIARIA = "Diária";
    public static final String COL_COLETAS = "Coletas";
    public static final String COL_ENTREGAS = "Entregas";
    public static final String COL_DESPACHOS = "Despachos";
    public static final String COL_RETIRADAS = "Retiradas";
    public static final String COL_COLETAS_REVERSA = "Coletas Reversa";
    public static final String COL_ADICIONAIS = "Adicionais";
    public static final String COL_DESCONTOS = "Descontos";
    public static final String COL_ADIANTAMENTO = "Adiantamento";
    public static final String COL_DESPESAS = "Despesas";
    public static final String COL_TOTAL_DESPESAS = "Total Despesas";
    public static final String COL_SALDO_DESPESAS = "Saldo Despesas";
    public static final String COL_INSS = "INSS";
    public static final String COL_SEST_SENAT = "SEST/SENAT";
    public static final String COL_IR = "IR";
    public static final String COL_SALDO_A_PAGAR = "Saldo a pagar";
    public static final String COL_SERVICOS_FINALIZADOS = "Serviços Finalizados";
    public static final String COL_PERC_EFETIVIDADE = "% Efetividade";
    public static final String COL_CLASSIFICACAO = "Classificação";
    public static final String COL_OBSERVACOES = "Observações operacionais";
    public static final String COL_STATUS = "Status";
    public static final String COL_USUARIO = "Usuário";

    private ManifestoCsvConfig() {
    }

    public static String textoOuNulo(String valor) {
        if (valor == null) {
            return null;
        }
        String texto = valor.trim();
        return texto.isEmpty() ? null : texto;
    }

    public static Integer inteiroOuNulo(String valor) {
        String texto = textoOuNulo(valor);
        if (texto == null) {
            return null;
        }
        return Integer.valueOf(texto.replaceAll("[^\\d-]", ""));
    }

    public static BigDecimal decimalBrOuNulo(String valor) {
        String texto = textoOuNulo(valor);
        if (texto == null) {
            return null;
        }
        String normalizado = texto.replace(".", "").replace(",", ".");
        return new BigDecimal(normalizado);
    }

    public static LocalDate dataOuNula(String valor) {
        String texto = textoOuNulo(valor);
        if (texto == null) {
            return null;
        }
        try {
            return LocalDate.parse(texto, FORMATO_DATA);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Data invalida: " + texto, e);
        }
    }
}
