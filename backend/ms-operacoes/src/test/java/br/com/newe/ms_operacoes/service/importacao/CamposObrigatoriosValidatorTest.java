package br.com.newe.ms_operacoes.service.importacao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.junit.jupiter.api.Test;

class CamposObrigatoriosValidatorTest {

    @Test
    void informaTodosOsCamposObrigatoriosVazios() throws Exception {
        String csv = "Manifesto;Data;CPF;Veículo\n123; ;=\"\";\n";
        CSVFormat formato = CSVFormat.Builder.create(CSVFormat.DEFAULT)
                .setDelimiter(';')
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .build();

        try (CSVParser parser = formato.parse(new StringReader(csv))) {
            List<String> camposVazios = new CamposObrigatoriosValidator()
                    .camposVazios(parser.iterator().next());

            assertEquals(List.of("Data", "CPF", "Veículo"), camposVazios);
        }
    }
}