package br.com.newe.ms_operacoes.service.importacao;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import br.com.newe.ms_operacoes.dto.VeiculoImportacaoItem;
import br.com.newe.ms_operacoes.service.importacao.AvaliacaoCampo.Severidade;
import br.com.newe.ms_operacoes.service.importacao.ManifestoCsvConfig.ColunaEsperada;

/**
 * Le o CSV de veiculos exportado do sistema de frota (mesmo formato do de
 * manifestos: ISO-8859-1, ';', decimal BR, documentos em ="...").
 *
 * Regra do time: dado sujo bloqueia. Excecoes combinadas, que gravam vazio sem
 * bloquear: 0,00 e 1,00 nas medidas (o sistema de origem usa como "nao
 * informado"), 0,00 nos valores, e telefone digitado em "Número Autorização GR".
 *
 * O que depende do banco (tipo, cidade, Renavam de outra placa, agregado,
 * motorista) e conferido depois, no ms-frota.
 */
@Component
public class CsvVeiculoParser {

    static final String COL_PLACA = "Placa";
    static final String COL_MARCA = "Marca";
    static final String COL_MODELO = "Modelo";
    static final String COL_COR = "Cor";
    static final String COL_COMBUSTIVEL = "Combustível";
    static final String COL_NUMERO_FROTA = "Nº Frota";
    static final String COL_ANO_FABRICACAO = "Ano Fabricação";
    static final String COL_ANO_MODELO = "Ano Modelo";
    static final String COL_RENAVAM = "Renavam";
    static final String COL_CHASSI = "Chassi";
    static final String COL_CIDADE = "Cidade Licenciamento";
    static final String COL_TIPO_VEICULO = "Tipo Veículo";
    static final String COL_TIPO_CARROCERIA = "Tipo Carroceria";
    static final String COL_TIPO_PROPRIETARIO = "Tipo Proprietário";
    static final String COL_PROPRIETARIO = "Proprietário";
    static final String COL_AGREGADO = "Agregado";
    static final String COL_TABELA_AGREGADO = "Tabela de agregado";
    static final String COL_MOTORISTA = "Motorista Padrão";
    static final String COL_DATA_COMPRA = "Data Compra";
    static final String COL_VALOR_COMPRA = "Valor Compra";
    static final String COL_DATA_VENDA = "Data Venda";
    static final String COL_VALOR_VENDA = "Valor Venda";
    static final String COL_PBT = "PBT (Peso Bruto Total)";
    static final String COL_TARA = "Tara (kg)";
    static final String COL_CAPACIDADE = "Capacidade (m3)";
    static final String COL_CAPACIDADE_LOTACAO = "Capacidade Lotação";
    static final String COL_AUTORIZACAO_GR = "Número Autorização GR";
    static final String COL_VALIDADE_GR = "Validade GR";
    static final String COL_STATUS = "Status do Veículo";
    static final String COL_LOTE_CIOT = "Lote para Fechamento do CIOT";

    public static final List<ColunaEsperada> COLUNAS_ESPERADAS = List.of(
            new ColunaEsperada(COL_PLACA, true),
            new ColunaEsperada(COL_TIPO_VEICULO, true),
            new ColunaEsperada(COL_MARCA, false),
            new ColunaEsperada(COL_MODELO, false),
            new ColunaEsperada(COL_COR, false),
            new ColunaEsperada(COL_COMBUSTIVEL, false),
            new ColunaEsperada(COL_NUMERO_FROTA, false),
            new ColunaEsperada(COL_ANO_FABRICACAO, false),
            new ColunaEsperada(COL_ANO_MODELO, false),
            new ColunaEsperada(COL_RENAVAM, false),
            new ColunaEsperada(COL_CHASSI, false),
            new ColunaEsperada(COL_CIDADE, false),
            new ColunaEsperada(COL_TIPO_CARROCERIA, false),
            new ColunaEsperada(COL_TIPO_PROPRIETARIO, false),
            new ColunaEsperada(COL_PROPRIETARIO, false),
            new ColunaEsperada(COL_AGREGADO, false),
            new ColunaEsperada(COL_TABELA_AGREGADO, false),
            new ColunaEsperada(COL_MOTORISTA, false),
            new ColunaEsperada(COL_DATA_COMPRA, false),
            new ColunaEsperada(COL_VALOR_COMPRA, false),
            new ColunaEsperada(COL_DATA_VENDA, false),
            new ColunaEsperada(COL_VALOR_VENDA, false),
            new ColunaEsperada(COL_PBT, false),
            new ColunaEsperada(COL_TARA, false),
            new ColunaEsperada(COL_CAPACIDADE, false),
            new ColunaEsperada(COL_CAPACIDADE_LOTACAO, false),
            new ColunaEsperada(COL_AUTORIZACAO_GR, false),
            new ColunaEsperada(COL_VALIDADE_GR, false),
            new ColunaEsperada(COL_STATUS, false),
            new ColunaEsperada(COL_LOTE_CIOT, false));

    /** Lista de cores do Detran; masculino e erros de digitacao vistos no arquivo real viram a cor oficial. */
    private static final Map<String, String> CORES = dominio(
            List.of("AMARELA", "AZUL", "BEGE", "BRANCA", "CINZA", "DOURADA", "GRENÁ", "LARANJA", "MARROM",
                    "PRATA", "PRETA", "ROSA", "ROXA", "VERDE", "VERMELHA", "FANTASIA"),
            Map.ofEntries(
                    Map.entry("BRANCO", "BRANCA"), Map.entry("BRANNCA", "BRANCA"), Map.entry("BRNCA", "BRANCA"),
                    Map.entry("BRANC", "BRANCA"), Map.entry("BRANCOP", "BRANCA"), Map.entry("PRETO", "PRETA"),
                    Map.entry("AMARELO", "AMARELA"), Map.entry("AMA", "AMARELA"), Map.entry("VERMELHO", "VERMELHA"),
                    Map.entry("ROXO", "ROXA"), Map.entry("DOURADO", "DOURADA"), Map.entry("CCINZA", "CINZA"),
                    Map.entry("LANRANJA", "LARANJA"), Map.entry("GRENAL", "GRENÁ")));

    private static final Map<String, String> COMBUSTIVEIS = dominio(List.of("Diesel S10", "Diesel S500", "Etanol",
            "Gasolina", "Gás Natural", "Biodiesel", "Flex", "Eletricidade", "Querosene", "Arla"), Map.of());

    private static final Map<String, String> CARROCERIAS = dominio(List.of("Baú", "Baú Frigorífico", "Sider",
            "Grade Baixa", "Granelera", "Bug porta container"), Map.of());

    private static final Map<String, String> PROPRIETARIOS = dominio(List.of("Agregado", "Próprio", "Outro"), Map.of());

    private static final Map<String, String> STATUS = dominio(List.of("Ativo", "Inativo"), Map.of());

    private static final Pattern ANO = Pattern.compile("\\d{4}");
    private static final Pattern RENAVAM = Pattern.compile("\\d{11}");
    /** 17 caracteres, sem I, O e Q (para nao confundir com 1 e 0). */
    private static final Pattern CHASSI = Pattern.compile("[A-HJ-NPR-Z0-9]{17}");
    /** Antes do padrao de 17, o chassi era so numero (ex.: Mercedes 1113 de 1978: 34403312397682). */
    private static final Pattern CHASSI_ANTIGO = Pattern.compile("\\d{14}");
    private static final int ANO_CHASSI_ANTIGO = 1990;
    private static final Pattern CIDADE_UF = Pattern.compile("^(.+?)\\s+-\\s+([A-Za-z]{2})$");
    /** "ROBERTO FELTRIN 04350172840": o sistema de origem cola o CPF/CNPJ no nome. */
    private static final Pattern NOME_COM_DOCUMENTO = Pattern.compile("^(.*?)\\s+(\\d{11}|\\d{14})$");

    /** Linhas do arquivo. Valida tambem as com problema: sao elas que a tela precisa mostrar. */
    public record Resultado(List<Linha> linhas) {

        public List<VeiculoImportacaoItem> validos() {
            return linhas.stream().filter(l -> !l.bloqueia()).map(Linha::item).toList();
        }
    }

    public record Linha(int numero, VeiculoImportacaoItem item, Map<String, String> valores, List<ProblemaCelula> problemas) {

        public boolean bloqueia() {
            return problemas.stream().anyMatch(p -> p.severidade() == Severidade.ERRO);
        }
    }

    public Resultado parse(byte[] conteudo) throws IOException {
        List<Linha> linhas = new ArrayList<>();
        try (CSVParser parser = ManifestoCsvConfig.CSV_FORMAT.parse(
                new InputStreamReader(new ByteArrayInputStream(conteudo), ManifestoCsvConfig.CHARSET))) {
            for (CSVRecord record : parser) {
                Leitor leitor = new Leitor(record, (int) record.getRecordNumber() + 1);
                VeiculoImportacaoItem item = leitor.ler();
                linhas.add(new Linha(leitor.linha, item, valoresCrus(record), leitor.problemas));
            }
        }
        repetidos(linhas, COL_PLACA, VeiculoImportacaoItem::placa);
        repetidos(linhas, COL_RENAVAM, VeiculoImportacaoItem::renavam);
        repetidos(linhas, COL_CHASSI, VeiculoImportacaoItem::chassi);
        return new Resultado(linhas);
    }

    /**
     * Mesma placa, Renavam ou chassi em duas linhas bloqueia as duas, apontando a
     * outra. No arquivo real isso e quase sempre o veiculo cadastrado duas vezes,
     * com a placa antiga e com a Mercosul.
     */
    private static void repetidos(List<Linha> linhas, String coluna, Function<VeiculoImportacaoItem, String> campo) {
        Map<String, List<Linha>> grupos = linhas.stream()
                .filter(l -> campo.apply(l.item()) != null)
                .collect(Collectors.groupingBy(l -> campo.apply(l.item()), LinkedHashMap::new, Collectors.toList()));

        for (Map.Entry<String, List<Linha>> grupo : grupos.entrySet()) {
            if (grupo.getValue().size() < 2) {
                continue;
            }
            for (Linha linha : grupo.getValue()) {
                List<Linha> outras = grupo.getValue().stream().filter(o -> o != linha).toList();
                String onde = outras.stream()
                        .map(o -> "linha " + o.numero() + " (placa " + o.item().placa() + ")")
                        .collect(Collectors.joining(", "));
                boolean mercosul = outras.stream()
                        .anyMatch(o -> Placas.mesmaConvertida(o.item().placa(), linha.item().placa()));
                linha.problemas().add(new ProblemaCelula(coluna, coluna, Severidade.ERRO, EstadoCampo.NAO_CONVERTE,
                        linha.valores().get(coluna), "Valor único no arquivo",
                        "A coluna '" + coluna + "' na linha " + linha.numero() + " repete " + grupo.getKey()
                                + ", que também está na " + onde
                                + (mercosul ? ". É o mesmo veículo antes e depois da conversão para Mercosul: "
                                        + "mantenha só a linha da placa nova" : "")));
            }
        }
    }

    private static Map<String, String> valoresCrus(CSVRecord record) {
        Map<String, String> valores = new LinkedHashMap<>();
        for (ColunaEsperada coluna : COLUNAS_ESPERADAS) {
            valores.put(coluna.nome(), record.isSet(coluna.nome()) ? record.get(coluna.nome()) : null);
        }
        return valores;
    }

    /** Converte uma linha anotando cada celula com problema, sem parar na primeira. */
    private static final class Leitor {

        private final CSVRecord record;
        private final int linha;
        private final List<ProblemaCelula> problemas = new ArrayList<>();

        Leitor(CSVRecord record, int linha) {
            this.record = record;
            this.linha = linha;
        }

        VeiculoImportacaoItem ler() {
            Integer anoFabricacao = ano(COL_ANO_FABRICACAO);
            String[] cidade = cidadeUf();
            String[] agregado = agregado();
            return new VeiculoImportacaoItem(
                    linha,
                    placa(),
                    texto(COL_MARCA),
                    texto(COL_MODELO),
                    dominio(COL_COR, CORES),
                    dominio(COL_COMBUSTIVEL, COMBUSTIVEIS),
                    texto(COL_NUMERO_FROTA),
                    anoFabricacao,
                    ano(COL_ANO_MODELO),
                    renavam(),
                    chassi(anoFabricacao),
                    cidade[0],
                    cidade[1],
                    obrigatorio(COL_TIPO_VEICULO),
                    dominio(COL_TIPO_CARROCERIA, CARROCERIAS),
                    dominio(COL_TIPO_PROPRIETARIO, PROPRIETARIOS),
                    texto(COL_PROPRIETARIO),
                    agregado[0],
                    agregado[1],
                    texto(COL_TABELA_AGREGADO),
                    texto(COL_MOTORISTA),
                    data(COL_DATA_COMPRA),
                    numero(COL_VALOR_COMPRA, false),
                    data(COL_DATA_VENDA),
                    numero(COL_VALOR_VENDA, false),
                    numero(COL_PBT, true),
                    numero(COL_TARA, true),
                    numero(COL_CAPACIDADE, true),
                    numero(COL_CAPACIDADE_LOTACAO, true),
                    autorizacaoGr(),
                    data(COL_VALIDADE_GR),
                    dominio(COL_STATUS, STATUS),
                    texto(COL_LOTE_CIOT));
        }

        private String cru(String coluna) {
            return record.isSet(coluna) ? record.get(coluna) : null;
        }

        private String texto(String coluna) {
            return ManifestoCsvConfig.textoOuNulo(cru(coluna));
        }

        private String obrigatorio(String coluna) {
            String texto = texto(coluna);
            if (texto == null) {
                problemas.add(new ProblemaCelula(coluna, coluna, Severidade.ERRO, EstadoCampo.VAZIO, cru(coluna),
                        "Preenchido", "A coluna '" + coluna + "' e obrigatoria e nao possui valor na linha " + linha));
            }
            return texto;
        }

        private void erro(String coluna, String esperado, String detalhe) {
            problemas.add(new ProblemaCelula(coluna, coluna, Severidade.ERRO, EstadoCampo.NAO_CONVERTE, cru(coluna),
                    esperado, "A coluna '" + coluna + "' na linha " + linha + " nao esta no formato esperado: " + detalhe));
        }

        /** Guardada sem hifen e em maiusculas, como o ms-frota indexa. */
        private String placa() {
            String texto = obrigatorio(COL_PLACA);
            if (texto == null) {
                return null;
            }
            try {
                ValidadorCadastral.placa(texto);
                return texto.trim().toUpperCase(Locale.ROOT).replace("-", "");
            } catch (IllegalArgumentException e) {
                erro(COL_PLACA, "Placa no padrao AAA9999 ou Mercosul AAA9A99", e.getMessage());
                return null;
            }
        }

        private Integer ano(String coluna) {
            String texto = texto(coluna);
            if (texto == null) {
                return null;
            }
            int maximo = Year.now().getValue() + 1;
            if (ANO.matcher(texto).matches()) {
                int ano = Integer.parseInt(texto);
                if (ano >= 1900 && ano <= maximo) {
                    return ano;
                }
            }
            erro(coluna, "Ano entre 1900 e " + maximo, "'" + texto + "' nao e um ano valido");
            return null;
        }

        private String renavam() {
            String texto = texto(COL_RENAVAM);
            if (texto == null) {
                return null;
            }
            if (!RENAVAM.matcher(texto).matches() || !renavamComDigitoValido(texto)) {
                erro(COL_RENAVAM, "11 digitos com digito verificador valido", "Renavam invalido: '" + texto + "'");
                return null;
            }
            return texto;
        }

        /** Remarcado pelo Detran termina em "REM": o sufixo fica guardado, a conferencia e no que vem antes. */
        private String chassi(Integer anoFabricacao) {
            String texto = texto(COL_CHASSI);
            if (texto == null) {
                return null;
            }
            String chassi = texto.toUpperCase(Locale.ROOT).replace(" ", "");
            String numero = chassi.endsWith("REM") ? chassi.substring(0, chassi.length() - 3) : chassi;
            boolean antigo = anoFabricacao != null && anoFabricacao < ANO_CHASSI_ANTIGO
                    && CHASSI_ANTIGO.matcher(numero).matches();
            if (!CHASSI.matcher(numero).matches() && !antigo) {
                erro(COL_CHASSI, "17 letras e numeros, sem I, O e Q (anterior a " + ANO_CHASSI_ANTIGO + ": 14 digitos)",
                        "chassi invalido: '" + texto + "'");
                return null;
            }
            return chassi;
        }

        /** "São José dos Campos - SP" -> {nome, UF}. Se a cidade existe, o ms-frota confere. */
        private String[] cidadeUf() {
            String texto = texto(COL_CIDADE);
            if (texto == null) {
                return new String[] {null, null};
            }
            Matcher m = CIDADE_UF.matcher(texto);
            if (m.matches()) {
                try {
                    return new String[] {m.group(1).trim(), ValidadorCadastral.uf(m.group(2))};
                } catch (IllegalArgumentException e) {
                    erro(COL_CIDADE, "Cidade - UF (ex.: Jacareí - SP)", e.getMessage());
                    return new String[] {null, null};
                }
            }
            erro(COL_CIDADE, "Cidade - UF (ex.: Jacareí - SP)", "'" + texto + "' sem a UF no fim");
            return new String[] {null, null};
        }

        /** {nome, documento}. Numero no fim que nao e CPF/CNPJ valido continua sendo parte do nome. */
        private String[] agregado() {
            String texto = texto(COL_AGREGADO);
            if (texto == null) {
                return new String[] {null, null};
            }
            Matcher m = NOME_COM_DOCUMENTO.matcher(texto);
            if (m.matches()) {
                try {
                    return new String[] {m.group(1).trim(), ValidadorCadastral.cpfOuCnpj(m.group(2))};
                } catch (IllegalArgumentException e) {
                    // segue com o texto inteiro como nome
                }
            }
            return new String[] {texto, null};
        }

        private String dominio(String coluna, Map<String, String> valores) {
            String texto = texto(coluna);
            if (texto == null) {
                return null;
            }
            String valor = valores.get(normalizar(texto));
            if (valor == null) {
                erro(coluna, String.join(", ", new TreeSet<>(valores.values())),
                        "'" + texto + "' nao e um valor aceito");
            }
            return valor;
        }

        /**
         * @param umEhVazio nas medidas (PBT, tara, capacidades) o sistema de origem
         *                  escreve 1,00 quando ninguem informou; nos valores, so 0,00
         */
        private BigDecimal numero(String coluna, boolean umEhVazio) {
            BigDecimal numero;
            try {
                numero = ManifestoCsvConfig.decimalBrOuNulo(cru(coluna));
            } catch (IllegalArgumentException e) {
                erro(coluna, "Numero decimal (1.234,56)", e.getMessage());
                return null;
            }
            if (numero == null || numero.signum() == 0 || (umEhVazio && numero.compareTo(BigDecimal.ONE) == 0)) {
                return null;
            }
            if (numero.signum() < 0) {
                erro(coluna, "Numero maior que zero", "'" + texto(coluna) + "' e negativo");
                return null;
            }
            return numero;
        }

        private LocalDate data(String coluna) {
            try {
                return ManifestoCsvConfig.dataOuNula(cru(coluna));
            } catch (IllegalArgumentException e) {
                erro(coluna, "Data no formato DD/MM/AAAA", e.getMessage());
                return null;
            }
        }

        /**
         * No arquivo real esse campo veio com celulares (DDD + 9 + 8 digitos) em
         * vez do numero da GR. Decisao do time: ignorar e gravar vazio.
         */
        private String autorizacaoGr() {
            String texto = texto(COL_AUTORIZACAO_GR);
            if (texto == null) {
                return null;
            }
            String digitos = texto.replaceAll("\\D", "");
            boolean celular = digitos.length() == 11 && digitos.charAt(2) == '9';
            return celular ? null : texto;
        }
    }

    /** Digito do Renavam: 10 primeiros digitos de tras para frente, pesos 2 a 9 e depois 2 e 3. */
    static boolean renavamComDigitoValido(String renavam) {
        int[] pesos = {2, 3, 4, 5, 6, 7, 8, 9, 2, 3};
        int soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += (renavam.charAt(9 - i) - '0') * pesos[i];
        }
        int digito = (soma * 10) % 11;
        return (digito == 10 ? 0 : digito) == renavam.charAt(10) - '0';
    }

    /** Sem acento, maiusculo, pontuacao vira espaco: "baú frigorifico" = "BAU FRIGORIFICO". */
    static String normalizar(String texto) {
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", " ").trim();
    }

    private static Map<String, String> dominio(List<String> oficiais, Map<String, String> apelidos) {
        Map<String, String> mapa = new HashMap<>();
        oficiais.forEach(v -> mapa.put(normalizar(v), v));
        apelidos.forEach((apelido, oficial) -> mapa.put(normalizar(apelido), oficial));
        return mapa;
    }
}
