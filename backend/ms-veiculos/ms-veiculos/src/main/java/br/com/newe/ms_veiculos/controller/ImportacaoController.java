package br.com.newe.ms_veiculos.controller;

import java.io.IOException;
import java.util.Map;

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

import br.com.newe.ms_veiculos.models.ArquivoImportacao;
import br.com.newe.ms_veiculos.service.ArquivoImportacaoService;

@RestController
@RequestMapping("/importacao/arquivos")
public class ImportacaoController {

    private final ArquivoImportacaoService service;

    public ImportacaoController(ArquivoImportacaoService service) {
        this.service = service;
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> importar(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("mesReferencia") String mesReferencia
    ) throws IOException {

        if (arquivo.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if (!mesReferencia.matches("\\d{4}-\\d{2}")) {
            return ResponseEntity.badRequest().build();
        }

        String nome = arquivo.getOriginalFilename();

        if (nome == null ||
                !(nome.toLowerCase().endsWith(".xml")
                || nome.toLowerCase().endsWith(".xlsm"))) {
            return ResponseEntity.badRequest().build();
        }

        ArquivoImportacao importacao =
                service.criar(arquivo, mesReferencia);

        Map<String, Object> resposta = Map.of(
                "id", importacao.getId(),
                "status", importacao.getStatus()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

}