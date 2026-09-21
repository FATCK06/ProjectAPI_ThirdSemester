package br.com.newe.ms_operacoes.service.importacao;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
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

    private LinhaManifesto converter(CSVRecord r, int numeroLinha) {
        try {
            String cpf = digitos(r, ManifestoCsvConfig.COL_CPF);
            String manifesto = texto(r, ManifestoCsvConfig.COL_MANIFESTO);
            LocalDate data = data(r, ManifestoCsvConfig.COL_DATA);

            if (cpf == null || cpf.length() != 11) {
                throw new IllegalArgumentException("CPF ausente ou invalido: " + r.get(ManifestoCsvConfig.COL_CPF));
            }
            if (manifesto == null) {
                throw new IllegalArgumentException("Manifesto ausente");
            }
            // viagens.id_manifesto e integer: melhor falhar aqui, apontando a linha,
            // do que so na hora de gravar.
            if (!manifesto.matches("\\d+")) {
                throw new IllegalArgumentException("Manifesto nao numerico: " + manifesto);
            }
            // Obrigatoria porque o mes de referencia da viagem e derivado dela.
            if (data == null) {
                throw new IllegalArgumentException("Data do manifesto ausente");
            }

            return new LinhaManifesto(
                    texto(r, ManifestoCsvConfig.COL_NOME),
                    cpf,
                    digitos(r, ManifestoCsvConfig.COL_PIS),
                    data(r, ManifestoCsvConfig.COL_DATA_NASCIMENTO),
                    texto(r, ManifestoCsvConfig.COL_ENDERECO),
                    digitos(r, ManifestoCsvConfig.COL_CEP),
                    texto(r, ManifestoCsvConfig.COL_BAIRRO),
                    texto(r, ManifestoCsvConfig.COL_CIDADE),
                    texto(r, ManifestoCsvConfig.COL_ESTADO),
                    texto(r, ManifestoCsvConfig.COL_CHEFE_GUARNICAO),
                    texto(r, ManifestoCsvConfig.COL_VIGILANTE1),
                    texto(r, ManifestoCsvConfig.COL_VIGILANTE2),
                    texto(r, ManifestoCsvConfig.COL_AGREGADO),
                    digitos(r, ManifestoCsvConfig.COL_AGREGADO_DOCUMENTO),
                    digitos(r, ManifestoCsvConfig.COL_AGREGADO_PIS),
                    texto(r, ManifestoCsvConfig.COL_REGIME_FISCAL),
                    manifesto,
                    texto(r, ManifestoCsvConfig.COL_FILIAL),
                    data,
                    ManifestoCsvConfig.mesReferenciaDe(data),
                    texto(r, ManifestoCsvConfig.COL_VEICULO),
                    texto(r, ManifestoCsvConfig.COL_REBOQUE1),
                    texto(r, ManifestoCsvConfig.COL_REBOQUE2),
                    texto(r, ManifestoCsvConfig.COL_REBOQUE3),
                    texto(r, ManifestoCsvConfig.COL_DESTINO),
                    inteiro(r, ManifestoCsvConfig.COL_KM_SAIDA),
                    inteiro(r, ManifestoCsvConfig.COL_KM_CHEGADA),
                    inteiro(r, ManifestoCsvConfig.COL_SERVICOS),
                    inteiro(r, ManifestoCsvConfig.COL_NFS),
                    decimal(r, ManifestoCsvConfig.COL_KG_REAL),
                    decimal(r, ManifestoCsvConfig.COL_KG_TAXADO),
                    decimal(r, ManifestoCsvConfig.COL_M3),
                    decimal(r, ManifestoCsvConfig.COL_CAPACIDADE_VEICULO),
                    decimal(r, ManifestoCsvConfig.COL_PERC_APROV_VEICULO),
                    decimal(r, ManifestoCsvConfig.COL_VALE_FRETE),
                    decimal(r, ManifestoCsvConfig.COL_VALOR_NF),
                    decimal(r, ManifestoCsvConfig.COL_VALOR_FRETES),
                    decimal(r, ManifestoCsvConfig.COL_VALOR_FRETE),
                    decimal(r, ManifestoCsvConfig.COL_COMBUSTIVEL),
                    decimal(r, ManifestoCsvConfig.COL_PEDAGIO),
                    decimal(r, ManifestoCsvConfig.COL_DIARIA),
                    inteiro(r, ManifestoCsvConfig.COL_COLETAS),
                    inteiro(r, ManifestoCsvConfig.COL_ENTREGAS),
                    inteiro(r, ManifestoCsvConfig.COL_DESPACHOS),
                    inteiro(r, ManifestoCsvConfig.COL_RETIRADAS),
                    inteiro(r, ManifestoCsvConfig.COL_COLETAS_REVERSA),
                    decimal(r, ManifestoCsvConfig.COL_ADICIONAIS),
                    decimal(r, ManifestoCsvConfig.COL_DESCONTOS),
                    decimal(r, ManifestoCsvConfig.COL_ADIANTAMENTO),
                    decimal(r, ManifestoCsvConfig.COL_DESPESAS),
                    decimal(r, ManifestoCsvConfig.COL_TOTAL_DESPESAS),
                    decimal(r, ManifestoCsvConfig.COL_SALDO_DESPESAS),
                    decimal(r, ManifestoCsvConfig.COL_INSS),
                    decimal(r, ManifestoCsvConfig.COL_SEST_SENAT),
                    decimal(r, ManifestoCsvConfig.COL_IR),
                    decimal(r, ManifestoCsvConfig.COL_SALDO_A_PAGAR),
                    inteiro(r, ManifestoCsvConfig.COL_SERVICOS_FINALIZADOS),
                    decimal(r, ManifestoCsvConfig.COL_PERC_EFETIVIDADE),
                    texto(r, ManifestoCsvConfig.COL_CLASSIFICACAO),
                    texto(r, ManifestoCsvConfig.COL_OBSERVACOES),
                    texto(r, ManifestoCsvConfig.COL_STATUS),
                    texto(r, ManifestoCsvConfig.COL_USUARIO),
                    numeroLinha
            );
        } catch (RuntimeException e) {
            throw new LinhaManifestoInvalidaException(numeroLinha, e.getMessage());
        }
    }

    private String texto(CSVRecord r, String coluna) {
        return ManifestoCsvConfig.textoOuNulo(r.get(coluna));
    }

    private String digitos(CSVRecord r, String coluna) {
        return ManifestoCsvConfig.digitosOuNulo(r.get(coluna));
    }

    private Integer inteiro(CSVRecord r, String coluna) {
        return ManifestoCsvConfig.inteiroOuNulo(r.get(coluna));
    }

    private BigDecimal decimal(CSVRecord r, String coluna) {
        return ManifestoCsvConfig.decimalBrOuNulo(r.get(coluna));
    }

    private LocalDate data(CSVRecord r, String coluna) {
        return ManifestoCsvConfig.dataOuNula(r.get(coluna));
    }
}
