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

import br.com.newe.ms_frota.dto.ProblemaImportacao;
import br.com.newe.ms_frota.dto.ResultadoImportacaoVeiculos;
import br.com.newe.ms_frota.dto.TipoVeiculoDTO;
import br.com.newe.ms_frota.dto.VeiculoImportacaoItem;
import br.com.newe.ms_frota.dto.VeiculoLoteItem;
import br.com.newe.ms_frota.models.Veiculo;
import br.com.newe.ms_frota.service.ImportacaoVeiculoService;
import br.com.newe.ms_frota.service.VeiculoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/veiculos")
public class VeiculoController {

    private final VeiculoService service;
    private final ImportacaoVeiculoService importacaoService;

    public VeiculoController(VeiculoService service, ImportacaoVeiculoService importacaoService) {
        this.service = service;
        this.importacaoService = importacaoService;
    }

    @GetMapping("/{placa}")
    public ResponseEntity<Veiculo> buscarPorPlaca(
        @PathVariable 
        @Pattern(regexp = "[A-Za-z]{3}-?[0-9][A-Za-z0-9][0-9]{2}" , message = "Deve conter formato de placa válido, ex: ABC-1234 ou ABC1D23")
        String placa) {
        return service.buscarPorPlaca(placa)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/tipos")
    public ResponseEntity<List<TipoVeiculoDTO>> buscarTodosTipos() {
        return ResponseEntity.ok(service.buscarTodosTipos());
    }

    /** Placa por id, em lote. Usado pelo dashboard do ms-operacoes. */
    @PostMapping("/placas")
    public ResponseEntity<Map<UUID, String>> buscarPlacas(
            @Valid @RequestBody @Size(max = 10000) List<@NotNull UUID> ids) {
        return ResponseEntity.ok(service.buscarPlacas(ids));
    }

    @PostMapping("/lote")
    public ResponseEntity<Map<String, UUID>> upsertLote(
        @Valid @RequestBody @Size(max = 10000) List<@Valid VeiculoLoteItem> itens) {
        return ResponseEntity.ok(service.upsertLote(itens));
    }

    /** Confere o lote contra o banco sem gravar. Chamado pelo ms-operacoes na validação. */
    @PostMapping("/importacao/conferir")
    public ResponseEntity<List<ProblemaImportacao>> conferirImportacao(
        @Valid @RequestBody @Size(max = 10000) List<@Valid VeiculoImportacaoItem> itens) {
        return ResponseEntity.ok(importacaoService.conferir(itens));
    }

    /** Grava o lote inteiro ou nada: com problema que bloqueia, 422 e os problemas. */
    @PostMapping("/importacao")
    public ResponseEntity<ResultadoImportacaoVeiculos> importar(
        @Valid @RequestBody @Size(max = 10000) List<@Valid VeiculoImportacaoItem> itens) {
        ResultadoImportacaoVeiculos resultado = importacaoService.gravar(itens);
        return resultado.bloqueado()
                ? ResponseEntity.unprocessableEntity().body(resultado)
                : ResponseEntity.ok(resultado);
    }

    @PostMapping("/modelos")
    public ResponseEntity<Map<UUID, String>> modelos(
        @Valid @RequestBody @Size(max = 10000) List<@NotNull UUID> ids) {
        return ResponseEntity.ok(service.buscarModelosPorIds(ids));
    }

    @PostMapping("/tipos")
    public ResponseEntity<Map<UUID, String>> tipos(
        @Valid @RequestBody @Size(max = 10000) List<@NotNull UUID> ids) {
        return ResponseEntity.ok(service.buscarTiposPorIds(ids));
    }
}
