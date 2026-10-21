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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import br.com.newe.ms_frota.dto.MotoristaLoteItem;
import br.com.newe.ms_frota.dto.MotoristaResumo;
import br.com.newe.ms_frota.models.Motorista;
import br.com.newe.ms_frota.service.MotoristaService;

@RestController
@RequestMapping("/api/motoristas")
public class MotoristaController {

    private final MotoristaService service;

    public MotoristaController(MotoristaService service) {
        this.service = service;
    }

    @GetMapping("/{cpf}")
    public ResponseEntity<Motorista> buscarPorCpf(
        @PathVariable
        @Pattern(regexp = "\\d{11}", message = "deve conter exatamente 11 dígitos")
        String cpf) {
   
        return service.buscarPorCpf(cpf)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Motorista> buscarPorId(@PathVariable UUID id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Nome e CPF por id, em lote. Usado pelo dashboard do ms-operacoes. */
    @PostMapping("/resumos")
    public ResponseEntity<List<MotoristaResumo>> buscarResumos(
        @Valid @RequestBody @Size(max = 10000) List<@NotNull UUID> ids) {
        return ResponseEntity.ok(service.buscarResumos(ids));
    }

    /**
     * Upsert em lote usado pela importacao de manifestos. Devolve cpf -> id para
     * todos os CPFs recebidos, criados agora ou ja existentes.
     *
     * Item sem CPF e ignorado em vez de derrubar o lote: numa planilha de
     * centenas de linhas, uma linha ruim nao pode invalidar as outras.
     */
    @PostMapping("/lote")
     public ResponseEntity<Map<String, UUID>> upsertLote(
             @Valid @RequestBody @Size(max = 10000) List<MotoristaLoteItem> itens) {
         return ResponseEntity.ok(service.upsertLote(itens));
     }
}
