package br.com.newe.ms_frota.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.newe.ms_frota.service.ResolvedorCidade;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/cidades")
public class CidadeController {

    private final ResolvedorCidade resolvedor;

    public CidadeController(ResolvedorCidade resolvedor) {
        this.resolvedor = resolvedor;
    }

    /**
     * Id da cidade de cada parada de cada rota, na mesma posicao (null quando nao
     * achou ou ficou ambiguo). Usado pelo ms-operacoes para preencher viagem_destinos.
     */
    @PostMapping("/rotas")
    public ResponseEntity<List<List<Integer>>> resolverRotas(
            @Valid @RequestBody @Size(max = 10000) List<@NotNull List<String>> rotas) {
        return ResponseEntity.ok(resolvedor.resolverRotas(rotas));
    }
}
