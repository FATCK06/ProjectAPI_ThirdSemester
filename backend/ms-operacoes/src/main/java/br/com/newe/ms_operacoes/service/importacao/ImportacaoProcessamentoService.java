package br.com.newe.ms_operacoes.service.importacao;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import br.com.newe.ms_operacoes.models.Importacao;
import br.com.newe.ms_operacoes.service.ImportacaoService;
import br.com.newe.ms_operacoes.service.importacao.AvaliacaoCampo.Severidade;

/**
 * As duas operacoes do fluxo em etapas.
 *
 * {@link #validar} le o arquivo e diz o que ha nele - sem gravar nada. Pode ser
 * chamada quantas vezes o usuario quiser enquanto ajusta a tela.
 *
 * {@link #executar} e a unica que escreve em viagens, e so deve ser chamada no
 * passo de confirmacao.
 *
 * O parse nunca e persistido: roda em memoria e e descartado no fim da
 * requisicao. Reler 1871 linhas leva milissegundos, e guardar o resultado
 * intermediario no banco custaria milhares de inserts que seriam apagados depois.
 */
@Service
public class ImportacaoProcessamentoService {

    private static final Logger log = LoggerFactory.getLogger(ImportacaoProcessamentoService.class);

    private static final int TAMANHO_AMOSTRA = 10;

    private final CsvManifestoParser parser;
    private final ResolvedorFrota resolvedorFrota;
    private final ManifestoBatchWriter batchWriter;
    private final ImportacaoService importacaoService;

    public ImportacaoProcessamentoService(
            CsvManifestoParser parser,
            ResolvedorFrota resolvedorFrota,
            ManifestoBatchWriter batchWriter,
            ImportacaoService importacaoService
    ) {
        this.parser = parser;
        this.resolvedorFrota = resolvedorFrota;
        this.batchWriter = batchWriter;
        this.importacaoService = importacaoService;
    }

    /**
     * Le e confere o arquivo. Nao grava viagem nenhuma.
     *
     * @param severidade null traz todas as linhas com problema; ERRO ou PENDENCIA
     *                   restringe a lista (e o total da paginacao) a esse grupo
     */
    public ResultadoValidacao validar(Importacao importacao, int pagina, int tamanho, Severidade severidade)
            throws IOException {
        byte[] conteudo = importacao.getArquivoConteudo();

        ResultadoParse resultado = parser.parse(conteudo);
        List<String> colunas = parser.lerColunas(conteudo);

        importacaoService.marcarValidado(
                importacao.getId(),
                resultado.totalLinhas(),
                resultado.linhas().size(),
                resultado.linhasInvalidas());

        // Erros antes de pendencias, e so depois pagina: senao, num arquivo cheio de
        // avisos, a linha que bloqueia a importacao cai numa pagina que ninguem abre.
        // Sort estavel: dentro de cada grupo segue a ordem das linhas.
        List<LinhaComProblema> todos = resultado.problemas().stream()
                .filter(l -> severidade == null || l.severidade() == severidade)
                .sorted(Comparator.comparing((LinhaComProblema l) -> !l.bloqueia()))
                .toList();
        int de = Math.min(Math.max(pagina, 0) * tamanho, todos.size());
        int ate = Math.min(de + tamanho, todos.size());

        return new ResultadoValidacao(
                importacao.getId(),
                importacao.getArquivoNome(),
                resultado.totalLinhas(),
                resultado.linhas().size(),
                resultado.linhasInvalidas(),
                resultado.linhasComPendencia(),
                colunas,
                mapear(colunas),
                new ResultadoValidacao.Pagina(pagina, tamanho, todos.size()),
                todos.subList(de, ate),
                resultado.linhas().stream().limit(TAMANHO_AMOSTRA).map(LinhaPreview::de).toList());
    }

    /**
     * Confronta o cabecalho do arquivo com as colunas que o parser procura.
     * Comparacao sem diferenciar caixa nem espacos nas pontas, igual ao CSVFormat.
     */
    private List<MapeamentoColuna> mapear(List<String> colunasDoArquivo) {
        // Chave normalizada -> grafia original, para a tela mostrar o cabecalho como o usuario escreveu.
        Map<String, String> presentes = new HashMap<>();
        colunasDoArquivo.stream()
                .filter(Objects::nonNull)
                .forEach(c -> presentes.putIfAbsent(normalizarColuna(c), c.trim()));

        return ManifestoCsvConfig.COLUNAS_ESPERADAS.stream()
                .map(esperada -> {
                    String noArquivo = presentes.get(normalizarColuna(esperada.nome()));
                    return new MapeamentoColuna(
                            esperada.nome(),
                            esperada.obrigatoria(),
                            noArquivo != null,
                            noArquivo);
                })
                .toList();
    }

    private static String normalizarColuna(String coluna) {
        return coluna.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Grava as viagens. Unico ponto do fluxo que escreve.
     *
     * Tudo ou nada: basta uma linha invalida para recusar o arquivo inteiro. A
     * regra e do time - dado parcial no banco e pior que importacao adiada, ja
     * que ninguem saberia depois quais manifestos ficaram de fora.
     *
     * @return quantas viagens entraram
     */
    public int executar(Importacao importacao) {
        Long id = importacao.getId();

        try {
            ResultadoParse resultado = parser.parse(importacao.getArquivoConteudo());

            if (resultado.temErro()) {
                throw new ArquivoComErroException(resultado.linhasInvalidas(), resultado.totalLinhas());
            }
            if (resultado.linhas().isEmpty()) {
                throw new IllegalStateException("Nenhuma linha para importar");
            }

            // So marca processando depois de passar na conferencia: arquivo recusado
            // continua VALIDADO, e nao preso em EM_PROCESSAMENTO.
            importacaoService.marcarProcessando(id);

            // Fora de transacao: chamada HTTP ao ms-frota nao pode segurar conexao do banco.
            ReferenciasFrota referencias = resolvedorFrota.resolver(resultado.linhas());

            int gravadas = batchWriter.gravarEmLote(id, resultado.linhas(), referencias);
            importacaoService.marcarConcluido(id, gravadas);

            return gravadas;

        } catch (ArquivoComErroException e) {
            // Nao e falha da importacao: o arquivo so precisa ser corrigido e reenviado.
            // Manter o status permite ao usuario voltar, conferir os erros e tentar de novo.
            throw e;

        } catch (Exception e) {
            log.error("Falha ao executar importacao {}", id, e);
            importacaoService.marcarErro(id);
            throw new ImportacaoFalhouException(e);
        }
    }

    /** Arquivo recusado por conter linha invalida. */
    public static class ArquivoComErroException extends RuntimeException {
        public ArquivoComErroException(int invalidas, int total) {
            super(invalidas + " de " + total + " linhas estao invalidas. "
                    + "Corrija o arquivo e envie novamente - nada foi gravado.");
        }
    }

    public static class ImportacaoFalhouException extends RuntimeException {
        public ImportacaoFalhouException(Throwable causa) {
            super("Falha ao gravar a importacao: " + causa.getMessage(), causa);
        }
    }
}
