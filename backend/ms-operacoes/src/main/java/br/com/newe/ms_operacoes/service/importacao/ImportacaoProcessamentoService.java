package br.com.newe.ms_operacoes.service.importacao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import br.com.newe.ms_operacoes.service.ArquivoImportacaoService;

/**
 * Orquestra o processamento sincrono de um CSV de manifestos:
 * parse -> resolucao das referencias no ms-frota -> gravacao -> status final.
 *
 * As tres etapas sao separadas de proposito: a do meio faz chamada HTTP e a
 * ultima abre transacao. Junta-las seguraria a conexao do banco durante a rede.
 *
 * Qualquer falha e contida aqui e nunca propaga pro controller: a importacao ja
 * foi registrada, e a resposta ao cliente so reflete o status final.
 */
@Service
public class ImportacaoProcessamentoService {

    private static final Logger log = LoggerFactory.getLogger(ImportacaoProcessamentoService.class);

    private final CsvManifestoParser parser;
    private final ResolvedorFrota resolvedorFrota;
    private final ManifestoBatchWriter batchWriter;
    private final ArquivoImportacaoService arquivoImportacaoService;

    public ImportacaoProcessamentoService(
            CsvManifestoParser parser,
            ResolvedorFrota resolvedorFrota,
            ManifestoBatchWriter batchWriter,
            ArquivoImportacaoService arquivoImportacaoService
    ) {
        this.parser = parser;
        this.resolvedorFrota = resolvedorFrota;
        this.batchWriter = batchWriter;
        this.arquivoImportacaoService = arquivoImportacaoService;
    }

    public void processarCsv(Long arquivoImportacaoId, byte[] conteudo) {
        try {
            List<LinhaManifesto> linhas = parser.parse(conteudo);
            ReferenciasFrota referencias = resolvedorFrota.resolver(linhas);
            int gravadas = batchWriter.gravarEmLote(arquivoImportacaoId, linhas, referencias);
            arquivoImportacaoService.marcarConcluido(arquivoImportacaoId, gravadas);
        } catch (Exception e) {
            log.error("Falha ao processar importacao {}", arquivoImportacaoId, e);
            arquivoImportacaoService.marcarErro(arquivoImportacaoId);
        }
    }
}
