package br.com.newe.ms_veiculos.service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.newe.ms_veiculos.models.ArquivoImportacao;
import br.com.newe.ms_veiculos.models.ResumoImportacao;
import br.com.newe.ms_veiculos.models.enums.StatusImportacaoEnum;
import br.com.newe.ms_veiculos.repository.ArquivoImportacaoRepository;
import br.com.newe.ms_veiculos.repository.ArquivoImportacaoRepository.ContagemPorStatus;

@Service
public class ArquivoImportacaoService {

    private final ArquivoImportacaoRepository repository;

    public ArquivoImportacaoService(ArquivoImportacaoRepository repository) {
        this.repository = repository;
    }

    public ArquivoImportacao criar(
        String arquivoNome,
        byte[] conteudo,
        String mesReferencia,
        String usuarioResponsavel
    ) {
        ArquivoImportacao importacao = new ArquivoImportacao();
        importacao.setArquivoNome(arquivoNome);
        importacao.setArquivoConteudo(conteudo);
        importacao.setMesReferencia(mesReferencia);
        importacao.setUsuarioResponsavel(usuarioResponsavel);
        importacao.setDataImportacao(LocalDateTime.now());
        importacao.setStatus(StatusImportacaoEnum.EM_PROCESSAMENTO);

        return repository.save(importacao);
    }

    public ArquivoImportacao buscarPorId(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void marcarConcluido(Long id, int quantidadeLinhasLidas) {
        repository.findById(id).ifPresent(importacao -> {
            importacao.setStatus(StatusImportacaoEnum.CONCLUIDO);
            importacao.setQuantidadeLinhasLidas(quantidadeLinhasLidas);
            repository.save(importacao);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void marcarErro(Long id) {
        repository.findById(id).ifPresent(importacao -> {
            importacao.setStatus(StatusImportacaoEnum.ERRO);
            repository.save(importacao);
        });
    }

    public ResumoImportacao gerarResumo(String mesReferencia, boolean operador) {
        Map<StatusImportacaoEnum, Long> porStatus = new EnumMap<>(StatusImportacaoEnum.class);
        long total = 0;

        for (ContagemPorStatus contagem : repository.contarPorStatus(mesReferencia)) {
            porStatus.put(contagem.getStatus(), contagem.getTotal());
            total += contagem.getTotal();
        }

        Map<String, Object> indicadoresFinanceiros = operador ? null : new HashMap<>();

        return new ResumoImportacao(mesReferencia, total, porStatus, indicadoresFinanceiros);
    }
}
