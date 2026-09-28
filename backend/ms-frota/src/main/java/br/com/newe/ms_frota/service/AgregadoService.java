package br.com.newe.ms_frota.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.newe.ms_frota.dto.AgregadoLoteItem;
import br.com.newe.ms_frota.models.Agregado;
import br.com.newe.ms_frota.repository.AgregadoRepository;
import br.com.newe.ms_frota.repository.AgregadoRepository.AgregadoDocumentoId;

@Service
public class AgregadoService {

    private static final int TAMANHO_CPF = 11;
    private static final int TAMANHO_CNPJ = 14;

    private final AgregadoRepository repository;

    public AgregadoService(AgregadoRepository repository) {
        this.repository = repository;
    }

    public Optional<Agregado> buscarPorDocumento(String documento) {
        return repository.findByDocumento(documento);
    }

    /** Cria quem ainda nao existe e devolve o mapa documento -> id. */
    @Transactional
    public Map<String, UUID> upsertLote(List<AgregadoLoteItem> itens) {
        Map<String, AgregadoLoteItem> porDocumento = new LinkedHashMap<>();
        for (AgregadoLoteItem item : itens) {
            if (aproveitavel(item)) {
                porDocumento.putIfAbsent(item.documento(), item);
            }
        }
        if (porDocumento.isEmpty()) {
            return Map.of();
        }

        Map<String, UUID> documentoParaId = new HashMap<>();
        for (AgregadoDocumentoId existente : repository.buscarIdsPorDocumento(porDocumento.keySet())) {
            documentoParaId.put(existente.getDocumento(), existente.getId());
        }

        List<Agregado> novos = new ArrayList<>();
        for (Map.Entry<String, AgregadoLoteItem> entrada : porDocumento.entrySet()) {
            if (!documentoParaId.containsKey(entrada.getKey())) {
                novos.add(montar(entrada.getValue()));
            }
        }

        for (Agregado salvo : repository.saveAll(novos)) {
            documentoParaId.put(salvo.getDocumento(), salvo.getIdAgregado());
        }

        return documentoParaId;
    }

    /**
     * Descarta o que o banco rejeitaria: o CHECK so aceita 11 ou 14 digitos e
     * nome e NOT NULL. Filtrar aqui evita que uma linha ruim derrube o lote inteiro.
     * No arquivo de abr-jun nenhuma linha cai aqui: ou vem documento e nome juntos
     * (1787), ou vem os dois vazios (84).
     */
    private boolean aproveitavel(AgregadoLoteItem item) {
        String documento = item.documento();
        return documento != null
                && (documento.length() == TAMANHO_CPF || documento.length() == TAMANHO_CNPJ)
                && item.nome() != null
                && !item.nome().isBlank();
    }

    private Agregado montar(AgregadoLoteItem item) {
        Agregado agregado = new Agregado();
        agregado.setDocumento(item.documento());   // deriva tipoPessoa
        agregado.setNome(item.nome());
        agregado.setPis(item.pis());
        agregado.setRegimeFiscal(item.regimeFiscal());
        return agregado;
    }
}
