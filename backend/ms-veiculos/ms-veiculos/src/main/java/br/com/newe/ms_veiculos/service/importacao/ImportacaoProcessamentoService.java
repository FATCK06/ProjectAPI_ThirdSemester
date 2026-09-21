package br.com.newe.ms_veiculos.service.importacao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import br.com.newe.ms_veiculos.service.ArquivoImportacaoService;

/**
 * Orquestra o processamento sincrono de um CSV de manifestos: parse -> gravacao em lote
 * -> atualizacao final do status da importacao (CONCLUIDO/ERRO). Qualquer falha (parsing
 * ou gravacao) e' contida aqui: nunca propaga pro controller, pois a importacao ja foi
 * registrada e a resposta ao cliente so reflete o status final.
 */
@Service
public class ImportacaoProcessamentoService {

    private static final Logger log = LoggerFactory.getLogger(ImportacaoProcessamentoService.class);

    private final CsvManifestoParser parser;
    private final ManifestoBatchWriter batchWriter;
    private final ArquivoImportacaoService arquivoImportacaoService;

    public ImportacaoProcessamentoService(
            CsvManifestoParser parser,
            ManifestoBatchWriter batchWriter,
            ArquivoImportacaoService arquivoImportacaoService
    ) {
        this.parser = parser;
        this.batchWriter = batchWriter;
        this.arquivoImportacaoService = arquivoImportacaoService;
    }

    public void processarCsv(Long arquivoImportacaoId, byte[] conteudo, String mesReferencia) {
        try {
            List<LinhaManifesto> linhas = parser.parse(conteudo);
            int quantidadeLinhas = batchWriter.gravarEmLote(arquivoImportacaoId, linhas, mesReferencia);
            arquivoImportacaoService.marcarConcluido(arquivoImportacaoId, quantidadeLinhas);
        } catch (Exception e) {
            log.error("Falha ao processar importacao {}", arquivoImportacaoId, e);
            arquivoImportacaoService.marcarErro(arquivoImportacaoId);
        }
    }
}
