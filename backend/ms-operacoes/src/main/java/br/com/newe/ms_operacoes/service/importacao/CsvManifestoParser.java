package br.com.newe.ms_operacoes.service.importacao;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import br.com.newe.ms_operacoes.service.importacao.AvaliacaoCampo.Severidade;

@Component
public class CsvManifestoParser {

    /**
     * Converte o arquivo inteiro, separando o que deu certo do que nao deu.
     *
     * Uma linha invalida nao interrompe as demais: numa planilha de 1871
     * manifestos, um CPF digitado errado nao pode custar os outros 1870.
     */
    public ResultadoParse parse(byte[] conteudo) throws IOException {
        List<LinhaManifesto> linhas = new ArrayList<>();
        List<LinhaComProblema> problemas = new ArrayList<>();

        try (CSVParser parser = ManifestoCsvConfig.CSV_FORMAT.parse(
                new InputStreamReader(new ByteArrayInputStream(conteudo), ManifestoCsvConfig.CHARSET))) {

            for (CSVRecord record : parser) {
                int numeroLinha = (int) record.getRecordNumber() + 1;
                Map<CampoManifesto, AvaliacaoCampo> avaliacoes = avaliarCelulas(record);
                List<ProblemaCelula> daLinha = problemasDe(avaliacoes, numeroLinha);
                boolean bloqueia = daLinha.stream().anyMatch(c -> c.severidade() == Severidade.ERRO);

                if (!bloqueia) {
                    try {
                        linhas.add(montar(avaliacoes, numeroLinha));
                    } catch (RuntimeException e) {
                        // Rede de seguranca: o catalogo ja converteu tudo, montar nao devia falhar.
                        daLinha = new ArrayList<>(daLinha);
                        daLinha.add(ProblemaCelula.inesperado(e.getMessage()));
                    }
                }

                if (!daLinha.isEmpty()) {
                    problemas.add(new LinhaComProblema(numeroLinha, valoresCrus(record), daLinha));
                }
            }
        }

        return new ResultadoParse(linhas, problemas);
    }

    /**
     * Classifica cada celula da linha pelo catalogo, sem interromper na primeira falha.
     * E a unica conversao da linha: {@link #montar} so le o que sai daqui.
     */
    private Map<CampoManifesto, AvaliacaoCampo> avaliarCelulas(CSVRecord record) {
        Map<CampoManifesto, AvaliacaoCampo> avaliacoes = new EnumMap<>(CampoManifesto.class);
        for (CampoManifesto campo : CampoManifesto.values()) {
            avaliacoes.put(campo, campo.avaliar(valorDe(record, campo.coluna())));
        }
        return avaliacoes;
    }

    private List<ProblemaCelula> problemasDe(Map<CampoManifesto, AvaliacaoCampo> avaliacoes, int numeroLinha) {
        List<ProblemaCelula> problemas = new ArrayList<>();
        for (AvaliacaoCampo avaliacao : avaliacoes.values()) {
            if (avaliacao.severidade() != Severidade.OK) {
                problemas.add(ProblemaCelula.de(avaliacao, numeroLinha));
            }
        }
        return problemas;
    }

    /** Celulas como vieram no arquivo: e o valor errado que a tela precisa mostrar. */
    private Map<String, String> valoresCrus(CSVRecord record) {
        Map<String, String> valores = new LinkedHashMap<>();
        for (CampoManifesto campo : CampoManifesto.values()) {
            valores.put(campo.coluna(), valorDe(record, campo.coluna()));
        }
        return valores;
    }

    /**
     * null quando a coluna nao existe no cabecalho ou a linha tem menos valores
     * que ele (linha truncada), em vez de estourar.
     */
    private String valorDe(CSVRecord record, String coluna) {
        return record.isSet(coluna) ? record.get(coluna) : null;
    }

    /**
     * Nomes das colunas do cabecalho, na ordem do arquivo. Alimenta o passo de
     * mapeamento da tela, que precisa mostrar o que veio na planilha antes de
     * qualquer conversao.
     */
    public List<String> lerColunas(byte[] conteudo) throws IOException {
        try (CSVParser parser = ManifestoCsvConfig.CSV_FORMAT.parse(
                new InputStreamReader(new ByteArrayInputStream(conteudo), ManifestoCsvConfig.CHARSET))) {
            return parser.getHeaderNames();
        }
    }

    /**
     * So e chamado sem ERRO na linha, entao os obrigatorios estao OK. Campo com
     * pendencia de formato chega aqui como null: o valor foi descartado e a
     * pendencia ja avisou o usuario.
     */
    private LinhaManifesto montar(Map<CampoManifesto, AvaliacaoCampo> a, int numeroLinha) {
        LocalDate data = data(a, CampoManifesto.DATA);

        return new LinhaManifesto(
                texto(a, CampoManifesto.NOME),
                texto(a, CampoManifesto.CPF),
                texto(a, CampoManifesto.PIS),
                data(a, CampoManifesto.DATA_NASCIMENTO),
                texto(a, CampoManifesto.ENDERECO),
                texto(a, CampoManifesto.CEP),
                texto(a, CampoManifesto.BAIRRO),
                texto(a, CampoManifesto.CIDADE),
                texto(a, CampoManifesto.ESTADO),
                texto(a, CampoManifesto.CHEFE_GUARNICAO),
                texto(a, CampoManifesto.VIGILANTE1),
                texto(a, CampoManifesto.VIGILANTE2),
                texto(a, CampoManifesto.AGREGADO),
                texto(a, CampoManifesto.AGREGADO_DOCUMENTO),
                texto(a, CampoManifesto.AGREGADO_PIS),
                texto(a, CampoManifesto.REGIME_FISCAL),
                String.valueOf(inteiro(a, CampoManifesto.MANIFESTO)),
                texto(a, CampoManifesto.FILIAL),
                data,
                ManifestoCsvConfig.mesReferenciaDe(data),
                texto(a, CampoManifesto.VEICULO),
                texto(a, CampoManifesto.REBOQUE1),
                texto(a, CampoManifesto.REBOQUE2),
                texto(a, CampoManifesto.REBOQUE3),
                texto(a, CampoManifesto.DESTINO),
                inteiro(a, CampoManifesto.KM_SAIDA),
                inteiro(a, CampoManifesto.KM_CHEGADA),
                inteiro(a, CampoManifesto.SERVICOS),
                inteiro(a, CampoManifesto.NFS),
                decimal(a, CampoManifesto.KG_REAL),
                decimal(a, CampoManifesto.KG_TAXADO),
                decimal(a, CampoManifesto.M3),
                decimal(a, CampoManifesto.CAPACIDADE_VEICULO),
                decimal(a, CampoManifesto.PERC_APROV_VEICULO),
                decimal(a, CampoManifesto.VALE_FRETE),
                decimal(a, CampoManifesto.VALOR_NF),
                decimal(a, CampoManifesto.VALOR_FRETES),
                decimal(a, CampoManifesto.VALOR_FRETE),
                decimal(a, CampoManifesto.COMBUSTIVEL),
                decimal(a, CampoManifesto.PEDAGIO),
                decimal(a, CampoManifesto.DIARIA),
                inteiro(a, CampoManifesto.COLETAS),
                inteiro(a, CampoManifesto.ENTREGAS),
                inteiro(a, CampoManifesto.DESPACHOS),
                inteiro(a, CampoManifesto.RETIRADAS),
                inteiro(a, CampoManifesto.COLETAS_REVERSA),
                decimal(a, CampoManifesto.ADICIONAIS),
                decimal(a, CampoManifesto.DESCONTOS),
                decimal(a, CampoManifesto.ADIANTAMENTO),
                decimal(a, CampoManifesto.DESPESAS),
                decimal(a, CampoManifesto.TOTAL_DESPESAS),
                decimal(a, CampoManifesto.SALDO_DESPESAS),
                decimal(a, CampoManifesto.INSS),
                decimal(a, CampoManifesto.SEST_SENAT),
                decimal(a, CampoManifesto.IR),
                decimal(a, CampoManifesto.SALDO_A_PAGAR),
                inteiro(a, CampoManifesto.SERVICOS_FINALIZADOS),
                decimal(a, CampoManifesto.PERC_EFETIVIDADE),
                texto(a, CampoManifesto.CLASSIFICACAO),
                texto(a, CampoManifesto.OBSERVACOES),
                texto(a, CampoManifesto.STATUS),
                texto(a, CampoManifesto.USUARIO),
                numeroLinha
        );
    }

    private static String texto(Map<CampoManifesto, AvaliacaoCampo> a, CampoManifesto campo) {
        return (String) a.get(campo).valor();
    }

    private static Integer inteiro(Map<CampoManifesto, AvaliacaoCampo> a, CampoManifesto campo) {
        return (Integer) a.get(campo).valor();
    }

    private static BigDecimal decimal(Map<CampoManifesto, AvaliacaoCampo> a, CampoManifesto campo) {
        return (BigDecimal) a.get(campo).valor();
    }

    private static LocalDate data(Map<CampoManifesto, AvaliacaoCampo> a, CampoManifesto campo) {
        return (LocalDate) a.get(campo).valor();
    }
}
