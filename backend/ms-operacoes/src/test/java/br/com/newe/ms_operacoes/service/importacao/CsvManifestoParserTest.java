package br.com.newe.ms_operacoes.service.importacao;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import br.com.newe.ms_operacoes.service.importacao.AvaliacaoCampo.Severidade;

class CsvManifestoParserTest {

    private static final String CABECALHO = "Manifesto;Data;CPF;Veículo;Km saída;Serviços;Kg Real";

    private final CsvManifestoParser parser = new CsvManifestoParser();

    private ResultadoParse parse(String... linhas) throws IOException {
        String csv = CABECALHO + "\n" + String.join("\n", linhas) + "\n";
        return parser.parse(csv.getBytes(ManifestoCsvConfig.CHARSET));
    }

    private static ProblemaCelula problemaNaColuna(LinhaComProblema linha, String coluna) {
        return linha.problemas().stream()
                .filter(p -> coluna.equals(p.coluna()))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void valorInvalidoEmCampoOpcionalViraPendenciaENaoBloqueia() throws IOException {
        ResultadoParse resultado = parse("123;01/04/2026;12345678901;ABC1D23;abc;5;10,00");

        assertThat(resultado.temErro()).isFalse();
        assertThat(resultado.linhas()).hasSize(1);
        assertThat(resultado.linhas().get(0).kmSaida()).isNull();

        LinhaComProblema linha = resultado.problemas().get(0);
        assertThat(linha.bloqueia()).isFalse();
        ProblemaCelula km = problemaNaColuna(linha, "Km saída");
        assertThat(km.severidade()).isEqualTo(Severidade.PENDENCIA);
        assertThat(km.estado()).isEqualTo(EstadoCampo.NAO_CONVERTE);
        assertThat(linha.problemas()).noneMatch(p -> p.coluna() == null);
    }

    @Test
    void colunaOpcionalAusenteNoCabecalhoNaoBloqueia() throws IOException {
        String csv = "Manifesto;Data;CPF;Veículo\n123;01/04/2026;12345678901;ABC1D23\n";

        ResultadoParse resultado = parser.parse(csv.getBytes(ManifestoCsvConfig.CHARSET));

        assertThat(resultado.temErro()).isFalse();
        assertThat(resultado.linhas()).hasSize(1);
    }

    @Test
    void inteiroComCasaDecimalNaoETruncado() throws IOException {
        ResultadoParse resultado = parse("123;01/04/2026;12345678901;ABC1D23;100;12,5;10,00");

        assertThat(resultado.linhas().get(0).servicos()).isNull();
        ProblemaCelula servicos = problemaNaColuna(resultado.problemas().get(0), "Serviços");
        assertThat(servicos.estado()).isEqualTo(EstadoCampo.NAO_CONVERTE);
    }

    @Test
    void contagemComCasasZeradasContinuaAceita() throws IOException {
        ResultadoParse resultado = parse("123;01/04/2026;12345678901;ABC1D23;1.000,00;5,00;10,00");

        assertThat(resultado.linhas().get(0).kmSaida()).isEqualTo(1000);
        assertThat(resultado.linhas().get(0).servicos()).isEqualTo(5);
    }

    @Test
    void zeroComoAusenteSinalizaMasGravaOZero() throws IOException {
        ResultadoParse resultado = parse("123;01/04/2026;12345678901;ABC1D23;100;5;0,00");

        assertThat(resultado.linhas().get(0).kgReal()).isEqualByComparingTo(BigDecimal.ZERO);
        ProblemaCelula kg = problemaNaColuna(resultado.problemas().get(0), "Kg Real");
        assertThat(kg.estado()).isEqualTo(EstadoCampo.AUSENTE_COMO_ZERO);
    }

    @Test
    void erroEmCampoObrigatorioBloqueiaALinha() throws IOException {
        ResultadoParse resultado = parse(
                "123;01/04/2026;123;ABC1D23;100;5;10,00",
                "124;31/02/2026;12345678901;ABC1D23;100;5;10,00");

        assertThat(resultado.temErro()).isTrue();
        assertThat(resultado.linhas()).isEmpty();
        assertThat(resultado.linhasInvalidas()).isEqualTo(2);
        assertThat(problemaNaColuna(resultado.problemas().get(0), "CPF").severidade()).isEqualTo(Severidade.ERRO);
        assertThat(problemaNaColuna(resultado.problemas().get(1), "Data").severidade()).isEqualTo(Severidade.ERRO);
    }

    @Test
    void manifestoComZeroAEsquerdaViraOMesmoId() throws IOException {
        ResultadoParse resultado = parse("00123;01/04/2026;12345678901;ABC1D23;100;5;10,00");

        assertThat(resultado.linhas().get(0).idManifesto()).isEqualTo(123);
    }
}
