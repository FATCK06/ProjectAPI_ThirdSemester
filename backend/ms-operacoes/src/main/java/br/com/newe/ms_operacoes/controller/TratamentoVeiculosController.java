package br.com.newe.ms_operacoes.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.newe.ms_operacoes.dto.ResultadoTratamentoVeiculosDTO;
import br.com.newe.ms_operacoes.service.frota.TratamentoVeiculosService;

/** Resultado do cruzamento manifestos x planilha de veiculos, mostrado ao fim da importacao de veiculos. */
@RestController
@RequestMapping("/api/importacao/veiculos")
public class TratamentoVeiculosController {

    private final TratamentoVeiculosService service;

    public TratamentoVeiculosController(TratamentoVeiculosService service) {
        this.service = service;
    }

    @GetMapping("/tratamento")
    public ResponseEntity<ResultadoTratamentoVeiculosDTO> tratamento() {
        return ResponseEntity.ok(service.resultado());
    }
}
