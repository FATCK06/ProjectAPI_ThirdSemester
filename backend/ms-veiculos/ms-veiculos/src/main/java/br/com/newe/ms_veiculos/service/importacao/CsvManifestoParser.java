package br.com.newe.ms_veiculos.service.importacao;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

@Component
public class CsvManifestoParser {

    public List<LinhaManifesto> parse(byte[] conteudo) throws IOException {
        List<LinhaManifesto> linhas = new ArrayList<>();

        try (CSVParser parser = ManifestoCsvConfig.CSV_FORMAT.parse(
                new InputStreamReader(new ByteArrayInputStream(conteudo), ManifestoCsvConfig.CHARSET))) {

            for (CSVRecord record : parser) {
                int numeroLinha = (int) record.getRecordNumber() + 1;
                linhas.add(converter(record, numeroLinha));
            }
        }

        return linhas;
    }

    private LinhaManifesto converter(CSVRecord record, int numeroLinha) {
        try {
            String cpf = normalizarCpf(record.get(ManifestoCsvConfig.COL_CPF));
            String manifesto = ManifestoCsvConfig.textoOuNulo(record.get(ManifestoCsvConfig.COL_MANIFESTO));
            LocalDate data = ManifestoCsvConfig.dataOuNula(record.get(ManifestoCsvConfig.COL_DATA));

            if (cpf == null || cpf.length() != 11) {
                throw new IllegalArgumentException("CPF ausente ou invalido: " + record.get(ManifestoCsvConfig.COL_CPF));
            }
            if (manifesto == null) {
                throw new IllegalArgumentException("Manifesto ausente");
            }

            return new LinhaManifesto(
                    ManifestoCsvConfig.textoOuNulo(record.get(ManifestoCsvConfig.COL_NOME)),
                    cpf,
                    ManifestoCsvConfig.textoOuNulo(record.get(ManifestoCsvConfig.COL_PIS)),
                    ManifestoCsvConfig.dataOuNula(record.get(ManifestoCsvConfig.COL_DATA_NASCIMENTO)),
                    ManifestoCsvConfig.textoOuNulo(record.get(ManifestoCsvConfig.COL_ENDERECO)),
                    ManifestoCsvConfig.textoOuNulo(record.get(ManifestoCsvConfig.COL_CEP)),
                    ManifestoCsvConfig.textoOuNulo(record.get(ManifestoCsvConfig.COL_BAIRRO)),
                    ManifestoCsvConfig.textoOuNulo(record.get(ManifestoCsvConfig.COL_CIDADE)),
                    ManifestoCsvConfig.textoOuNulo(record.get(ManifestoCsvConfig.COL_ESTADO)),
                    manifesto,
                    ManifestoCsvConfig.textoOuNulo(record.get(ManifestoCsvConfig.COL_FILIAL)),
                    data,
                    ManifestoCsvConfig.textoOuNulo(record.get(ManifestoCsvConfig.COL_VEICULO)),
                    ManifestoCsvConfig.textoOuNulo(record.get(ManifestoCsvConfig.COL_REBOQUE1)),
                    ManifestoCsvConfig.textoOuNulo(record.get(ManifestoCsvConfig.COL_REBOQUE2)),
                    ManifestoCsvConfig.textoOuNulo(record.get(ManifestoCsvConfig.COL_REBOQUE3)),
                    ManifestoCsvConfig.textoOuNulo(record.get(ManifestoCsvConfig.COL_DESTINO)),
                    ManifestoCsvConfig.inteiroOuNulo(record.get(ManifestoCsvConfig.COL_KM_SAIDA)),
                    ManifestoCsvConfig.inteiroOuNulo(record.get(ManifestoCsvConfig.COL_KM_CHEGADA)),
                    ManifestoCsvConfig.inteiroOuNulo(record.get(ManifestoCsvConfig.COL_SERVICOS)),
                    ManifestoCsvConfig.inteiroOuNulo(record.get(ManifestoCsvConfig.COL_NFS)),
                    ManifestoCsvConfig.decimalBrOuNulo(record.get(ManifestoCsvConfig.COL_KG_REAL)),
                    ManifestoCsvConfig.decimalBrOuNulo(record.get(ManifestoCsvConfig.COL_KG_TAXADO)),
                    ManifestoCsvConfig.decimalBrOuNulo(record.get(ManifestoCsvConfig.COL_M3)),
                    ManifestoCsvConfig.decimalBrOuNulo(record.get(ManifestoCsvConfig.COL_CAPACIDADE_VEICULO)),
                    ManifestoCsvConfig.decimalBrOuNulo(record.get(ManifestoCsvConfig.COL_PERC_APROV_VEICULO)),
                    ManifestoCsvConfig.decimalBrOuNulo(record.get(ManifestoCsvConfig.COL_VALE_FRETE)),
                    ManifestoCsvConfig.decimalBrOuNulo(record.get(ManifestoCsvConfig.COL_VALOR_NF)),
                    ManifestoCsvConfig.decimalBrOuNulo(record.get(ManifestoCsvConfig.COL_VALOR_FRETES)),
                    ManifestoCsvConfig.decimalBrOuNulo(record.get(ManifestoCsvConfig.COL_VALOR_FRETE)),
                    ManifestoCsvConfig.decimalBrOuNulo(record.get(ManifestoCsvConfig.COL_COMBUSTIVEL)),
                    ManifestoCsvConfig.decimalBrOuNulo(record.get(ManifestoCsvConfig.COL_PEDAGIO)),
                    ManifestoCsvConfig.decimalBrOuNulo(record.get(ManifestoCsvConfig.COL_DIARIA)),
                    ManifestoCsvConfig.inteiroOuNulo(record.get(ManifestoCsvConfig.COL_COLETAS)),
                    ManifestoCsvConfig.inteiroOuNulo(record.get(ManifestoCsvConfig.COL_ENTREGAS)),
                    ManifestoCsvConfig.inteiroOuNulo(record.get(ManifestoCsvConfig.COL_DESPACHOS)),
                    ManifestoCsvConfig.inteiroOuNulo(record.get(ManifestoCsvConfig.COL_RETIRADAS)),
                    ManifestoCsvConfig.inteiroOuNulo(record.get(ManifestoCsvConfig.COL_COLETAS_REVERSA)),
                    ManifestoCsvConfig.decimalBrOuNulo(record.get(ManifestoCsvConfig.COL_ADICIONAIS)),
                    ManifestoCsvConfig.decimalBrOuNulo(record.get(ManifestoCsvConfig.COL_DESCONTOS)),
                    ManifestoCsvConfig.decimalBrOuNulo(record.get(ManifestoCsvConfig.COL_ADIANTAMENTO)),
                    ManifestoCsvConfig.decimalBrOuNulo(record.get(ManifestoCsvConfig.COL_DESPESAS)),
                    ManifestoCsvConfig.decimalBrOuNulo(record.get(ManifestoCsvConfig.COL_TOTAL_DESPESAS)),
                    ManifestoCsvConfig.decimalBrOuNulo(record.get(ManifestoCsvConfig.COL_SALDO_DESPESAS)),
                    ManifestoCsvConfig.decimalBrOuNulo(record.get(ManifestoCsvConfig.COL_INSS)),
                    ManifestoCsvConfig.decimalBrOuNulo(record.get(ManifestoCsvConfig.COL_SEST_SENAT)),
                    ManifestoCsvConfig.decimalBrOuNulo(record.get(ManifestoCsvConfig.COL_IR)),
                    ManifestoCsvConfig.decimalBrOuNulo(record.get(ManifestoCsvConfig.COL_SALDO_A_PAGAR)),
                    ManifestoCsvConfig.inteiroOuNulo(record.get(ManifestoCsvConfig.COL_SERVICOS_FINALIZADOS)),
                    ManifestoCsvConfig.decimalBrOuNulo(record.get(ManifestoCsvConfig.COL_PERC_EFETIVIDADE)),
                    ManifestoCsvConfig.textoOuNulo(record.get(ManifestoCsvConfig.COL_CLASSIFICACAO)),
                    ManifestoCsvConfig.textoOuNulo(record.get(ManifestoCsvConfig.COL_OBSERVACOES)),
                    ManifestoCsvConfig.textoOuNulo(record.get(ManifestoCsvConfig.COL_STATUS)),
                    ManifestoCsvConfig.textoOuNulo(record.get(ManifestoCsvConfig.COL_USUARIO)),
                    numeroLinha
            );
        } catch (RuntimeException e) {
            throw new LinhaManifestoInvalidaException(numeroLinha, e.getMessage());
        }
    }

    private String normalizarCpf(String valor) {
        String texto = ManifestoCsvConfig.textoOuNulo(valor);
        return texto == null ? null : texto.replaceAll("\\D", "");
    }
}
