package br.com.newe.ms_operacoes.service.importacao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import br.com.newe.ms_operacoes.client.FrotaClient;
import br.com.newe.ms_operacoes.dto.ProblemaImportacao;
import br.com.newe.ms_operacoes.dto.ResultadoImportacaoVeiculos;
import br.com.newe.ms_operacoes.dto.VeiculoImportacaoItem;
import br.com.newe.ms_operacoes.service.ImportacaoService;
import br.com.newe.ms_operacoes.service.importacao.AvaliacaoCampo.Severidade;
import br.com.newe.ms_operacoes.service.importacao.CsvVeiculoParser.Linha;
import br.com.newe.ms_operacoes.service.importacao.ImportacaoProcessamentoService.ArquivoComErroException;

/**
 * Validar e executar do arquivo de veiculos. O ms-operacoes le e confere o
 * formato; o ms-frota, dono de veiculos, confere contra o cadastro e grava.
 */
@Component
public class ImportacaoVeiculoProcessamento {

    private final CsvManifestoParser estrutura;
    private final CsvVeiculoParser parser;
    private final FrotaClient frotaClient;
    private final ImportacaoService importacaoService;

    public ImportacaoVeiculoProcessamento(
            CsvManifestoParser estrutura,
            CsvVeiculoParser parser,
            FrotaClient frotaClient,
            ImportacaoService importacaoService) {
        this.estrutura = estrutura;
        this.parser = parser;
        this.frotaClient = frotaClient;
        this.importacaoService = importacaoService;
    }

    /** O arquivo inteiro conferido: formato aqui, cadastro no ms-frota. Nada e gravado. */
    public record Leitura(int totalLinhas, List<VeiculoImportacaoItem> validos, List<LinhaComProblema> problemas) {

        public int invalidas() {
            return (int) problemas.stream().filter(LinhaComProblema::bloqueia).count();
        }

        public int comPendencia() {
            return problemas.size() - invalidas();
        }
    }

    public Leitura ler(byte[] conteudo) throws IOException {
        estrutura.verificarEstrutura(conteudo, CsvVeiculoParser.COLUNAS_ESPERADAS);
        CsvVeiculoParser.Resultado resultado = parser.parse(conteudo);

        // So as linhas que passaram no formato vao ao cadastro: as outras ja bloqueiam.
        Map<Integer, Linha> porNumero = new HashMap<>();
        resultado.linhas().forEach(l -> porNumero.put(l.numero(), l));
        for (ProblemaImportacao p : frotaClient.conferirVeiculos(resultado.validos())) {
            Linha linha = porNumero.get(p.numeroLinha());
            if (linha != null) {
                linha.problemas().add(new ProblemaCelula(p.coluna(), p.coluna(),
                        p.bloqueia() ? Severidade.ERRO : Severidade.PENDENCIA,
                        EstadoCampo.NAO_CONVERTE, p.valor(), null, p.mensagem()));
            }
        }

        List<LinhaComProblema> problemas = new ArrayList<>();
        List<VeiculoImportacaoItem> validos = new ArrayList<>();
        for (Linha linha : resultado.linhas()) {
            if (!linha.problemas().isEmpty()) {
                problemas.add(new LinhaComProblema(linha.numero(), linha.valores(), linha.problemas()));
            }
            if (!linha.bloqueia()) {
                validos.add(linha.item());
            }
        }
        return new Leitura(resultado.linhas().size(), validos, problemas);
    }

    /**
     * Tudo ou nada, como o manifesto.
     *
     * @return veiculos criados + atualizados
     */
    public int executar(Long id, byte[] conteudo) throws IOException {
        Leitura leitura = ler(conteudo);
        if (leitura.invalidas() > 0) {
            throw new ArquivoComErroException(leitura.invalidas(), leitura.totalLinhas());
        }
        if (leitura.validos().isEmpty()) {
            throw new IllegalStateException("Nenhuma linha para importar");
        }

        importacaoService.reservarProcessamento(id);

        ResultadoImportacaoVeiculos resultado = frotaClient.importarVeiculos(leitura.validos());
        if (resultado == null || resultado.bloqueado()) {
            // O cadastro mudou entre a conferencia acima e a gravacao (outra pessoa importou).
            throw new IllegalStateException("O cadastro de veiculos mudou durante a importacao. "
                    + "Valide o arquivo de novo.");
        }
        return resultado.criados() + resultado.atualizados();
    }
}
