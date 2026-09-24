package br.com.newe.ms_frota.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.newe.ms_frota.dto.MotoristaLoteItem;
import br.com.newe.ms_frota.dto.MotoristaResumo;
import br.com.newe.ms_frota.models.Cidade;
import br.com.newe.ms_frota.models.Motorista;
import br.com.newe.ms_frota.repository.CidadeRepository;
import br.com.newe.ms_frota.repository.MotoristaRepository;
import br.com.newe.ms_frota.repository.MotoristaRepository.MotoristaCpfId;

@Service
public class MotoristaService {

    private static final Logger log = LoggerFactory.getLogger(MotoristaService.class);

    private final MotoristaRepository motoristaRepository;
    private final CidadeRepository cidadeRepository;

    public MotoristaService(MotoristaRepository motoristaRepository, CidadeRepository cidadeRepository) {
        this.motoristaRepository = motoristaRepository;
        this.cidadeRepository = cidadeRepository;
    }

    public Optional<Motorista> buscarPorCpf(String cpf) {
        return motoristaRepository.findByCpf(cpf);
    }

    /** Nome e CPF de varios motoristas de uma vez, para quem so guarda o id (ms-operacoes). */
    public List<MotoristaResumo> buscarResumos(List<UUID> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return motoristaRepository.findAllById(ids).stream()
                .map(m -> new MotoristaResumo(m.getIdMotorista(), m.getNome(), m.getCpf()))
                .toList();
    }

    /**
     * Cria quem ainda nao existe e devolve o mapa cpf -> id para todos os CPFs
     * recebidos. Quem ja existe nao e alterado: o manifesto e fonte de operacao,
     * nao de cadastro, e sobrescrever apagaria dado melhor vindo do CRUD.
     */
    @Transactional
    public Map<String, UUID> upsertLote(List<MotoristaLoteItem> itens) {
        Map<String, MotoristaLoteItem> porCpf = new LinkedHashMap<>();
        for (MotoristaLoteItem item : itens) {
            if (item.cpf() != null && !item.cpf().isBlank()) {
                porCpf.putIfAbsent(item.cpf(), item);
            }
        }
        if (porCpf.isEmpty()) {
            return Map.of();
        }

        Map<String, UUID> cpfParaId = new HashMap<>();
        for (MotoristaCpfId existente : motoristaRepository.buscarIdsPorCpf(porCpf.keySet())) {
            cpfParaId.put(existente.getCpf(), existente.getId());
        }

        Map<String, Cidade> cacheCidades = new HashMap<>();
        List<Motorista> novos = new ArrayList<>();
        for (Map.Entry<String, MotoristaLoteItem> entrada : porCpf.entrySet()) {
            if (!cpfParaId.containsKey(entrada.getKey())) {
                novos.add(montar(entrada.getValue(), cacheCidades));
            }
        }

        for (Motorista salvo : motoristaRepository.saveAll(novos)) {
            cpfParaId.put(salvo.getCpf(), salvo.getIdMotorista());
        }

        return cpfParaId;
    }

    private Motorista montar(MotoristaLoteItem item, Map<String, Cidade> cacheCidades) {
        EnderecoPartes endereco = EnderecoPartes.de(item.endereco());

        Motorista motorista = new Motorista();
        motorista.setCpf(item.cpf());
        motorista.setNome(item.nome());
        motorista.setPis(item.pis());
        motorista.setDataNascimento(item.dataNascimento());
        motorista.setCep(item.cep());
        motorista.setLogradouro(endereco.logradouro());
        motorista.setNumero(endereco.numero());
        motorista.setBairro(item.bairro());
        motorista.setCidade(resolverCidade(item.cidade(), item.estado(), cacheCidades));
        return motorista;
    }

    /**
     * O CSV traz cidade e UF como texto. Cidade nao cadastrada fica nula em vez
     * de derrubar a linha: id_cidade e opcional, e perder o endereco vale menos
     * que perder a viagem inteira.
     */
    private Cidade resolverCidade(String nome, String uf, Map<String, Cidade> cache) {
        if (nome == null || nome.isBlank() || uf == null || uf.isBlank()) {
            return null;
        }

        String chave = nome.trim().toUpperCase() + "|" + uf.trim().toUpperCase();
        if (cache.containsKey(chave)) {
            return cache.get(chave);
        }

        Cidade cidade = cidadeRepository.buscarPorNomeEUf(nome, uf).orElse(null);
        if (cidade == null) {
            log.warn("Cidade nao encontrada no cadastro: {}/{}", nome, uf);
        }
        cache.put(chave, cidade);
        return cidade;
    }
}
