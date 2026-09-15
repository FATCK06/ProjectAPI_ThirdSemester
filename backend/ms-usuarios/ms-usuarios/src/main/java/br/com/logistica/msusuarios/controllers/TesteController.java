package br.com.logistica.msusuarios.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teste")
@CrossOrigin(origins = "*")
public class TesteController {

    @GetMapping
    public ResponseEntity<String> testarRotaProtegida() {
        return ResponseEntity.ok("Parabéns! Se você está lendo isso, seu JWT é autêntico e a catraca liberou sua entrada!");
    }
}
