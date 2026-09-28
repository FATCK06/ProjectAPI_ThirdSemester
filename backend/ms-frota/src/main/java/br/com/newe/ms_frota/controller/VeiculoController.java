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

import br.com.newe.ms_frota.dto.VeiculoLoteItem;
import br.com.newe.ms_frota.models.Veiculo;
import br.com.newe.ms_frota.service.VeiculoService;

@RestController
@RequestMapping("/api/veiculos")
public class VeiculoController {

    private final VeiculoService service;

    public VeiculoController(VeiculoService service) {
        this.service = service;
    }

    @GetMapping("/{placa}")
    public ResponseEntity<Veiculo> buscarPorPlaca(@PathVariable String placa) {
        return service.buscarPorPlaca(placa)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Placa por id, em lote. Usado pelo dashboard do ms-operacoes. */
    @PostMapping("/placas")
    public ResponseEntity<Map<UUID, String>> buscarPlacas(@RequestBody List<UUID> ids) {
        return ResponseEntity.ok(service.buscarPlacas(ids));
    }

    @PostMapping("/lote")
    public ResponseEntity<Map<String, UUID>> upsertLote(@RequestBody List<VeiculoLoteItem> itens) {
        return ResponseEntity.ok(service.upsertLote(itens));
    }

    @PostMapping("/modelos")
    public ResponseEntity<Map<UUID, String>> modelos(@RequestBody List<UUID> ids) {
        return ResponseEntity.ok(service.buscarModelosPorIds(ids));
    }
}
