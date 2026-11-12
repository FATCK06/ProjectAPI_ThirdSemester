package br.com.newe.ms_operacoes.service.importacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import br.com.newe.ms_operacoes.dto.VeiculoImportacaoItem;
import br.com.newe.ms_operacoes.service.importacao.CsvVeiculoParser.Linha;

/** Linhas tiradas do arquivo real (veiculos_04-09-2026_10-32.csv), algumas sujas de proposito. */
class CsvVeiculoParserTest {

    private static final String CABECALHO = "Placa;Marca;Modelo;Cor;Combustível;Nº Frota;Ano Fabricação;Ano Modelo;"
            + "Renavam;Chassi;Cidade Licenciamento;Tipo Veículo;Tipo Carroceria;Tipo Proprietário;Proprietário;"
            + "Agregado;Tabela de agregado;Motorista Padrão;Data Compra;Valor Compra;Data Venda;Valor Venda;"
            + "PBT (Peso Bruto Total);Tara (kg);Capacidade (m3);Capacidade Lotação;Número Autorização GR;"
            + "Validade GR;Status do Veículo;Lote para Fechamento do CIOT";

    private final CsvVeiculoParser parser = new CsvVeiculoParser();

    private List<Linha> ler(String... linhas) throws Exception {
        String csv = CABECALHO + "\n" + String.join("\n", linhas);
        return parser.parse(csv.getBytes(ManifestoCsvConfig.CHARSET)).linhas();
    }

    @Test
    void linhaBoaComNormalizacoesCombinadas() throws Exception {
        Linha linha = ler("HNF5D31;FIAT;FIORINO;branco;Flex;;2011;2012;=\"00314200525\";9BD255049C8915183;"
                + "São José dos Campos - SP;FIORINO;;Agregado;LUCAS DIAS;LUCAS DIAS 52998224725;;LUCAS DIAS;;0,00;;"
                + "0,00;1,00;1,00;1,00;0,00;=\"12981249213\";;Ativo;").get(0);

        assertTrue(linha.problemas().isEmpty(), () -> linha.problemas().toString());
        VeiculoImportacaoItem v = linha.item();
        assertEquals("BRANCA", v.cor());
        assertEquals("00314200525", v.renavam());
        assertEquals("São José dos Campos", v.cidadeLicenciamento());
        assertEquals("SP", v.ufLicenciamento());
        assertEquals("LUCAS DIAS", v.agregado());
        assertEquals("52998224725", v.agregadoDocumento());
        // 0,00 e 1,00 sao "nao informado" no sistema de origem; GR com celular e ignorado.
        assertNull(v.pbt());
        assertNull(v.valorCompra());
        assertNull(v.numeroAutorizacao());
    }

    @Test
    void chassiAntigoERemarcadoSaoAceitos() throws Exception {
        List<Linha> linhas = ler(
                "GMA3F24;Mercedes Benz;=\"1113\";VERMELHA ;Diesel S10;;1978;1978;=\"00264242645\";"
                        + "=\"34403312397682\";São Paulo - SP;Truck ou pesado;Baú;Agregado;MARCELO;MARCELO;;ALEX;;0,00;;"
                        + "0,00;15.000,00;1,00;1,00;14.999,00;;;Ativo;",
                "DGZ4G19;FIAT;FIORINO IE;BRANCA;Flex;;2002;2002;=\"00790222701\";9BD25504428722106REM;"
                        + "São José dos Campos - SP;FIORINO;;Agregado;LUCIANO;LUCIANO;;LUCIANO;;0,00;;0,00;1,00;1,00;"
                        + "1,00;0,00;;;Inativo;");

        assertTrue(linhas.get(0).problemas().isEmpty(), () -> linhas.get(0).problemas().toString());
        assertEquals("1113", linhas.get(0).item().modelo());
        assertEquals(new BigDecimal("15000.00"), linhas.get(0).item().pbt());
        assertTrue(linhas.get(1).problemas().isEmpty(), () -> linhas.get(1).problemas().toString());
        assertEquals("9BD25504428722106REM", linhas.get(1).item().chassi());
    }

    @Test
    void dadoSujoBloqueiaApontandoCadaColuna() throws Exception {
        Linha linha = ler("XYZ1234;FIAT;FIORINO;VAN;{:diesel=>\"Diesel S10\"};;20113;2011;=\"00314200524\";"
                + "9bstH4X2\\G3225121;Jacareí;FIORINO;;Agregado;X;X;;X;;0,00;;0,00;1,00;1,00;1,00;0,00;;;Ativo;").get(0);

        assertTrue(linha.bloqueia());
        assertEquals(Set.of("Ano Fabricação", "Cor", "Combustível", "Renavam", "Chassi", "Cidade Licenciamento"),
                linha.problemas().stream().map(ProblemaCelula::coluna).collect(Collectors.toSet()));
    }

    @Test
    void mesmoVeiculoComPlacaAntigaEMercosulBloqueiaAsDuasLinhas() throws Exception {
        String resto = ";Iveco;DAILY;CINZA;Diesel S10;;2012;2013;=\"00495119040\";93ZC35B01D8445088;Caçapava - SP;VUC;;"
                + "Agregado;DIEGO;DIEGO;;DIEGO;;0,00;;0,00;1,00;1,00;1,00;0,00;;;Ativo;";
        List<Linha> linhas = ler("FCB9D62" + resto, "FCB9362" + resto);

        for (Linha linha : linhas) {
            assertTrue(linha.bloqueia());
            assertTrue(linha.problemas().stream().anyMatch(p -> p.mensagem().contains("conversão para Mercosul")),
                    () -> linha.problemas().toString());
        }
    }

    @Test
    void placaAntigaVsMercosul() {
        assertTrue(Placas.mesmaConvertida("FCB9362", "FCB9D62"));
        assertFalse(Placas.mesmaConvertida("FFL6F10", "CUA1A88"));
    }
}
