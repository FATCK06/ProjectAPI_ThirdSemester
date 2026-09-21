package br.com.newe.ms_veiculos.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.newe.ms_veiculos.models.Viagem;
import br.com.newe.ms_veiculos.repository.ViagemRepository;

@RestController
@RequestMapping("/viagens")
public class ViagemController {

    private final ViagemRepository repository;

    public ViagemController(ViagemRepository repository) {
        this.repository = repository;
    }

    @GetMapping // "/viagens?mesReferencia=2026-09"
    public ResponseEntity<List<Viagem>> listarPorMesReferencia(@RequestParam("mesReferencia") String mesReferencia) {
        if (!mesReferencia.matches("\\d{4}-\\d{2}")) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(repository.findByMesReferencia(mesReferencia));
    }
}
