package br.com.newe.ms_veiculos.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.newe.ms_veiculos.models.Motorista;
import br.com.newe.ms_veiculos.repository.MotoristaRepository;

@RestController
@RequestMapping("/motoristas")
public class MotoristaController {

    private final MotoristaRepository repository;

    public MotoristaController(MotoristaRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{cpf}") // "/motoristas/{cpf}"
    public ResponseEntity<Motorista> buscarPorCpf(@PathVariable String cpf) {
        return repository.findByCpf(cpf)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
