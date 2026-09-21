package br.com.newe.ms_operacoes.service.importacao;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;

/**
 * Formato do CSV de manifestos, conferido contra o arquivo real da Newelog
 * (manifestos_abr-jun.csv, 1871 registros, 61 colunas).
 *
 * Caracteristicas confirmadas no arquivo:
 * - encoding ISO-8859-1 (nao UTF-8)
 * - delimitador ';'
 * - data dd/MM/yyyy, decimal no padrao BR (1.234,56)
 * - campos de documento exportados pelo Excel como ="00000000000"
 * - campos de texto podem conter quebra de linha dentro das aspas
 */
public final class ManifestoCsvConfig {

    public static final Charset CHARSET = StandardCharsets.ISO_8859_1;
    public static final int TAMANHO_CHUNK = 50;

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_MES_REFERENCIA = DateTimeFormatter.ofPattern("yyyy-MM");

    /** Envolucro que o Excel usa para exportar texto sem perder zero a esquerda: ="00012345678" */
    private static final Pattern ESCAPE_EXCEL = Pattern.compile("^=\"(.*)\"$", Pattern.DOTALL);

    public static final CSVFormat CSV_FORMAT = CSVFormat.Builder.create(CSVFormat.DEFAULT)
            .setDelimiter(';')
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreHeaderCase(true)
            .setTrim(true)
            .build();

    // ── Manifesto ────────────────────────────────────────────────────────────
    public static final String COL_MANIFESTO = "Manifesto";
    public static final String COL_FILIAL = "Filial";
    public static final String COL_DATA = "Data";

    // ── Motorista (quem dirige) ──────────────────────────────────────────────
    public static final String COL_NOME = "Motorista";
    public static final String COL_CPF = "CPF";
    public static final String COL_PIS = "PIS";
    public static final String COL_DATA_NASCIMENTO = "Data de nascimento";
    public static final String COL_ENDERECO = "Endereço motorista";
    public static final String COL_CEP = "CEP Motorista";
    public static final String COL_BAIRRO = "Bairro Motorista";
    public static final String COL_CIDADE = "Cidade Motorista";
    public static final String COL_ESTADO = "Estado Motorista";

    // ── Equipe de guarnicao ──────────────────────────────────────────────────
    public static final String COL_CHEFE_GUARNICAO = "Chefe de Guarnição";
    public static final String COL_VIGILANTE1 = "Vigilante 1";
    public static final String COL_VIGILANTE2 = "Vigilante 2";

    // ── Agregado (o contratado; pode ser PF ou PJ) ───────────────────────────
    public static final String COL_AGREGADO = "Agregado";
    public static final String COL_AGREGADO_DOCUMENTO = "CPF/CNPJ Agregado";
    public static final String COL_AGREGADO_PIS = "PIS Agregado";
    public static final String COL_REGIME_FISCAL = "Regime Fiscal";

    // ── Veiculo e rota ───────────────────────────────────────────────────────
    public static final String COL_VEICULO = "Veículo";
    public static final String COL_REBOQUE1 = "Reboque 1";
    public static final String COL_REBOQUE2 = "Reboque 2";
    public static final String COL_REBOQUE3 = "Reboque 3";
    public static final String COL_DESTINO = "Destino";
    public static final String COL_KM_SAIDA = "Km saída";
    public static final String COL_KM_CHEGADA = "Km chegada";
    public static final String COL_CAPACIDADE_VEICULO = "Capacidade Veículo";
    public static final String COL_PERC_APROV_VEICULO = "% Aprov. Veículo";

    // ── Carga ────────────────────────────────────────────────────────────────
    public static final String COL_SERVICOS = "Serviços";
    public static final String COL_NFS = "NFs";
    public static final String COL_KG_REAL = "Kg Real";
    public static final String COL_KG_TAXADO = "Kg Taxado";
    public static final String COL_M3 = "M3";

    // ── Operacoes (vem no CSV com casas decimais, ex.: "0,00") ───────────────
    public static final String COL_COLETAS = "Coletas";
    public static final String COL_ENTREGAS = "Entregas";
    public static final String COL_DESPACHOS = "Despachos";
    public static final String COL_RETIRADAS = "Retiradas";
    public static final String COL_COLETAS_REVERSA = "Coletas Reversa";
    public static final String COL_SERVICOS_FINALIZADOS = "Serviços Finalizados";
    public static final String COL_PERC_EFETIVIDADE = "% Efetividade";

    // ── Financeiro ───────────────────────────────────────────────────────────
    public static final String COL_VALE_FRETE = "Vale frete";
    public static final String COL_VALOR_NF = "Valor NF";
    public static final String COL_VALOR_FRETES = "Valor Fretes";
    public static final String COL_VALOR_FRETE = "Valor Frete";
    public static final String COL_COMBUSTIVEL = "Combustível";
    public static final String COL_PEDAGIO = "Pedágio";
    public static final String COL_DIARIA = "Diária";
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

    // ── Fechamento ───────────────────────────────────────────────────────────
    public static final String COL_CLASSIFICACAO = "Classificação";
    public static final String COL_OBSERVACOES = "Observações operacionais";
    public static final String COL_STATUS = "Status";
    public static final String COL_USUARIO = "Usuário";

    private ManifestoCsvConfig() {
    }

    /** Remove o envolucro ="..." do Excel e devolve null para vazio. */
    public static String textoOuNulo(String valor) {
        if (valor == null) {
            return null;
        }
        String texto = valor.trim();
        var matcher = ESCAPE_EXCEL.matcher(texto);
        if (matcher.matches()) {
            texto = matcher.group(1).trim();
        }
        return texto.isEmpty() ? null : texto;
    }

    /** So os digitos: serve para CPF, CNPJ, PIS e CEP, que vem formatados de formas diferentes. */
    public static String digitosOuNulo(String valor) {
        String texto = textoOuNulo(valor);
        if (texto == null) {
            return null;
        }
        String digitos = texto.replaceAll("\\D", "");
        return digitos.isEmpty() ? null : digitos;
    }

    /**
     * Le como decimal antes de truncar: colunas de contagem (Coletas, Entregas, ...)
     * vem com casas decimais no CSV, e um replaceAll de nao-digitos transformaria
     * "1,00" em 100.
     */
    public static Integer inteiroOuNulo(String valor) {
        BigDecimal decimal = decimalBrOuNulo(valor);
        return decimal == null ? null : decimal.intValue();
    }

    public static BigDecimal decimalBrOuNulo(String valor) {
        String texto = textoOuNulo(valor);
        if (texto == null) {
            return null;
        }
        String normalizado = texto.replace(".", "").replace(",", ".");
        try {
            return new BigDecimal(normalizado);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Numero invalido: " + texto, e);
        }
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

    /**
     * Mes de referencia da propria linha. Um arquivo cobre varios meses
     * (o de abr-jun tem 792/596/483 registros), entao o mes nao pode vir
     * de um parametro unico do upload.
     */
    public static String mesReferenciaDe(LocalDate data) {
        return data == null ? null : data.format(FORMATO_MES_REFERENCIA);
    }
}
