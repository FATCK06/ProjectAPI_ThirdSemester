package br.com.newe.ms_operacoes.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.newe.ms_operacoes.models.Viagem;
import br.com.newe.ms_operacoes.repository.ViagemRepository;
import br.com.newe.ms_operacoes.security.PerfilResolver;
import jakarta.servlet.http.HttpServletRequest;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

@RestController
@RequestMapping("/api/viagens")
public class ViagemController {

    /** Propriedades de valor da Viagem no JSON; so quem ve dado financeiro as recebe. */
    static final List<String> CAMPOS_FINANCEIROS = List.of(
            "valorFrete", "valorFretes", "valeFrete", "valorNf",
            "totalDespesas", "saldoDespesas", "saldoAPagar");

    private final ViagemRepository repository;
    private final PerfilResolver perfilResolver;
    private final JsonMapper jsonMapper;

    public ViagemController(ViagemRepository repository, PerfilResolver perfilResolver, JsonMapper jsonMapper) {
        this.repository = repository;
        this.perfilResolver = perfilResolver;
        this.jsonMapper = jsonMapper;
    }

    @GetMapping // "/api/viagens?mesReferencia=2026-06"
    public ResponseEntity<?> listarPorMesReferencia(@RequestParam("mesReferencia") @MesReferencia String mesReferencia,
            HttpServletRequest request) {
        List<Viagem> viagens = repository.findByMesReferencia(mesReferencia);
        if (perfilResolver.podeVerDadosFinanceiros(request)) {
            return ResponseEntity.ok(viagens);
        }

        // Remove do JSON em vez de zerar na entidade: a Viagem e gerenciada pelo
        // Hibernate, e alterar o campo dela arrisca gravar o valor apagado.
        ArrayNode semValores = jsonMapper.valueToTree(viagens);
        semValores.forEach(viagem -> ((ObjectNode) viagem).remove(CAMPOS_FINANCEIROS));
        return ResponseEntity.ok(semValores);
    }
}
