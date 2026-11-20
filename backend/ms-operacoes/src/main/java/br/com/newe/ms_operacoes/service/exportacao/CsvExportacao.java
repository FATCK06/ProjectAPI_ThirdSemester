package br.com.newe.ms_operacoes.service.exportacao;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public final class CsvExportacao {
    private static final String BOM_UTF_8 = "\uFEFF";

    private CsvExportacao() {
    }

    public static byte[] gerar(List<String> cabecalho, List<? extends List<?>> linhas) throws IOException {
        StringWriter conteudo = new StringWriter();
        try (CSVPrinter impressora = new CSVPrinter(conteudo, CSVFormat.DEFAULT)) {
            impressora.printRecord(cabecalho);
            for (List<?> linha : linhas) {
                impressora.printRecord(linha.stream().map(CsvExportacao::protegerCelula).toList());
            }
        }
        return (BOM_UTF_8 + conteudo).getBytes(StandardCharsets.UTF_8);
    }

    private static Object protegerCelula(Object valor) {
        if (!(valor instanceof String texto)) {
            return valor == null ? "" : valor;
        }
        String inicio = texto.stripLeading();
        if (!inicio.isEmpty() && "=+-@\t\r".indexOf(inicio.charAt(0)) >= 0) {
            return "'" + texto;
        }
        return texto;
    }
}
