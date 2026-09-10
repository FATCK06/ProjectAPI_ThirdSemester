package br.com.newe.ms_veiculos.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.com.newe.ms_veiculos.models.entity.ArquivoImport;
import br.com.newe.ms_veiculos.service.ArquivoImportService;

@RestController
@RequestMapping("/importacoes")
public class ArquivoImportController {

    private final ArquivoImportService service;

    public ArquivoImportController(ArquivoImportService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Long>> importar(
            @RequestPart("arquivo") MultipartFile arquivo,
            @RequestParam("mesReferencia") String mesReferencia) {
        ArquivoImport importacao = service.importar(arquivo, mesReferencia);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("idImportacao", importacao.getId()));
    }
}
