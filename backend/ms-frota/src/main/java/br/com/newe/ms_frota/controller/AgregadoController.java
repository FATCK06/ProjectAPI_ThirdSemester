package br.com.newe.ms_frota.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.newe.ms_frota.dto.AgregadoLoteItem;
import br.com.newe.ms_frota.models.Agregado;
import br.com.newe.ms_frota.service.AgregadoService;

@RestController
@RequestMapping("/api/agregados")
public class AgregadoController {

    private final AgregadoService service;

    public AgregadoController(AgregadoService service) {
        this.service = service;
    }

    /** Documento e CPF (11 digitos) ou CNPJ (14), so numeros. */
    @GetMapping("/{documento}")
    public ResponseEntity<Agregado> buscarPorDocumento(@PathVariable String documento) {
        return service.buscarPorDocumento(documento)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/lote")
    public ResponseEntity<Map<String, UUID>> upsertLote(@RequestBody List<AgregadoLoteItem> itens) {
        return ResponseEntity.ok(service.upsertLote(itens));
    }
}
