package br.com.newe.ms_operacoes.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.com.newe.ms_operacoes.models.ArquivoImportacao;
import br.com.newe.ms_operacoes.models.ResumoImportacao;
import br.com.newe.ms_operacoes.models.Viagem;
import br.com.newe.ms_operacoes.repository.ViagemRepository;
import br.com.newe.ms_operacoes.security.PerfilResolver;
import br.com.newe.ms_operacoes.service.ArquivoImportacaoService;
import br.com.newe.ms_operacoes.service.importacao.ImportacaoProcessamentoService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/importacao/arquivos")
public class ImportacaoController {

    private final ArquivoImportacaoService service;
    private final PerfilResolver perfilResolver;
    private final ImportacaoProcessamentoService processamentoService;
    private final ViagemRepository viagemRepository;

    public ImportacaoController(
            ArquivoImportacaoService service,
            PerfilResolver perfilResolver,
            ImportacaoProcessamentoService processamentoService,
            ViagemRepository viagemRepository
    ) {
        this.service = service;
        this.perfilResolver = perfilResolver;
        this.processamentoService = processamentoService;
        this.viagemRepository = viagemRepository;
    }

    @GetMapping("/resumo") // "/importacao/arquivos/resumo?mesReferencia=2026-09"
    public ResponseEntity<ResumoImportacao> resumo(
            @RequestParam("mesReferencia") String mesReferencia,
            HttpServletRequest request
    ) {
        if (!mesReferencia.matches("\\d{4}-\\d{2}")) {
            return ResponseEntity.badRequest().build();
        }

        boolean operador = perfilResolver.isOperador(request);

        return ResponseEntity.ok(service.gerarResumo(mesReferencia, operador));
    }

    @GetMapping("/{id}") // "/importacao/arquivos/{id}"
    public ResponseEntity<ArquivoImportacao> buscarPorId(@PathVariable Long id) {
        ArquivoImportacao importacao = service.buscarPorId(id);

        if (importacao == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(importacao);
    }

    @GetMapping("/{id}/conteudo") // "/importacao/arquivos/{id}/conteudo"
    public ResponseEntity<byte[]> buscarConteudoPorId(@PathVariable Long id) {
        ArquivoImportacao importacao = service.buscarPorId(id);

        if (importacao == null) {
            return ResponseEntity.notFound().build();
        }

        String nomeArquivo = importacao.getArquivoNome();
        MediaType tipoConteudo = nomeArquivo != null
                && nomeArquivo.toLowerCase().endsWith(".xml")
                ? MediaType.APPLICATION_XML
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(tipoConteudo)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nomeArquivo + "\"")
                .body(importacao.getArquivoConteudo());
    }

    @GetMapping("/{id}/viagens") // "/importacao/arquivos/{id}/viagens"
    public ResponseEntity<List<Viagem>> viagens(@PathVariable Long id) {
        ArquivoImportacao importacao = service.buscarPorId(id);

        if (importacao == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(viagemRepository.findByArquivoImportacaoId(id));
    }

    /**
     * Ids dos motoristas que aparecem neste arquivo. Nome e CPF ficam no ms-frota:
     * quem precisar do cadastro completo consulta /api/motoristas la com estes ids.
     */
    @GetMapping("/{id}/motoristas")
    public ResponseEntity<List<UUID>> motoristas(@PathVariable Long id) {
        ArquivoImportacao importacao = service.buscarPorId(id);

        if (importacao == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(viagemRepository.buscarIdsMotoristasPorArquivo(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> importar(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("mesReferencia") String mesReferencia,
            HttpServletRequest request
    ) throws IOException {

        if (arquivo.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if (!mesReferencia.matches("\\d{4}-\\d{2}")) {
            return ResponseEntity.badRequest().build();
        }

        String nome = arquivo.getOriginalFilename();
        String nomeMinusculo = nome != null ? nome.toLowerCase() : "";

        if (nome == null ||
                !(nomeMinusculo.endsWith(".xml")
                || nomeMinusculo.endsWith(".xlsm")
                || nomeMinusculo.endsWith(".csv"))) {
            return ResponseEntity.badRequest().build();
        }

        byte[] conteudo = arquivo.getBytes();
        String usuarioResponsavel = perfilResolver.resolverUsuario(request);

        // mesReferencia rotula apenas o arquivo. O mes de cada viagem vem da
        // coluna Data da propria linha, porque um arquivo cobre varios meses.
        ArquivoImportacao importacao = service.criar(nome, conteudo, mesReferencia, usuarioResponsavel);

        if (nomeMinusculo.endsWith(".csv")) {
            processamentoService.processarCsv(importacao.getId(), conteudo);
            importacao = service.buscarPorId(importacao.getId());
        }

        Map<String, Object> resposta = Map.of(
                "id", importacao.getId(),
                "status", importacao.getStatus()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

}
