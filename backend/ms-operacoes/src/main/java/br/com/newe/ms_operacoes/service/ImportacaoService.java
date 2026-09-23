package br.com.newe.ms_operacoes.service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.newe.ms_operacoes.models.Importacao;
import br.com.newe.ms_operacoes.models.ResumoImportacao;
import br.com.newe.ms_operacoes.models.enums.StatusImportacaoEnum;
import br.com.newe.ms_operacoes.repository.ImportacaoRepository;
import br.com.newe.ms_operacoes.repository.ImportacaoRepository.ContagemPorStatus;

/**
 * Ciclo de vida da importacao. Nao parseia nem grava viagens - so guarda o
 * arquivo e acompanha o estado enquanto ele passa pelas etapas da tela.
 */
@Service
public class ImportacaoService {

    private final ImportacaoRepository repository;

    public ImportacaoService(ImportacaoRepository repository) {
        this.repository = repository;
    }

    /** Guarda o arquivo e para por aqui: nada e parseado nem gravado ainda. */
    public Importacao receber(String arquivoNome, byte[] conteudo, String usuarioResponsavel) {
        Importacao importacao = new Importacao();
        importacao.setArquivoNome(arquivoNome);
        importacao.setArquivoConteudo(conteudo);
        importacao.setUsuarioResponsavel(usuarioResponsavel);
        importacao.setDataImportacao(LocalDateTime.now());
        importacao.setStatus(StatusImportacaoEnum.AGUARDANDO);

        return repository.save(importacao);
    }

    public Importacao buscarPorId(Long id) {
        return repository.findById(id).orElse(null);
    }

    public List<Importacao> listar() {
        return repository.findAllByOrderByDataImportacaoDesc();
    }

    /** Registra o que o parse encontrou. Continua sem gravar viagem nenhuma. */
    @Transactional
    public void marcarValidado(Long id, int total, int validas, int invalidas) {
        repository.findById(id).ifPresent(importacao -> {
            importacao.setStatus(StatusImportacaoEnum.VALIDADO);
            importacao.setTotalLinhas(total);
            importacao.setLinhasValidas(validas);
            importacao.setLinhasInvalidas(invalidas);
            repository.save(importacao);
        });
    }

    @Transactional
    public void marcarProcessando(Long id) {
        atualizarStatus(id, StatusImportacaoEnum.EM_PROCESSAMENTO);
    }

    /**
     * Transacao propria: o status final precisa sobreviver mesmo que a transacao
     * de gravacao das viagens tenha sido revertida.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void marcarConcluido(Long id, int linhasGravadas) {
        repository.findById(id).ifPresent(importacao -> {
            importacao.setStatus(StatusImportacaoEnum.CONCLUIDO);
            importacao.setLinhasGravadas(linhasGravadas);
            repository.save(importacao);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void marcarErro(Long id) {
        atualizarStatus(id, StatusImportacaoEnum.ERRO);
    }

    private void atualizarStatus(Long id, StatusImportacaoEnum status) {
        repository.findById(id).ifPresent(importacao -> {
            importacao.setStatus(status);
            repository.save(importacao);
        });
    }

    /**
     * Quantas importacoes existem em cada estado.
     *
     * Deixou de receber mes de referencia: a planilha e trimestral, entao o mes
     * nunca descreveu o arquivo. Numero por mes vem de viagens, que guarda o mes
     * derivado da data de cada linha.
     */
    public ResumoImportacao gerarResumo() {
        Map<StatusImportacaoEnum, Long> porStatus = new EnumMap<>(StatusImportacaoEnum.class);
        long total = 0;

        for (ContagemPorStatus contagem : repository.contarPorStatus()) {
            porStatus.put(contagem.getStatus(), contagem.getTotal());
            total += contagem.getTotal();
        }

        return new ResumoImportacao(total, porStatus);
    }
}
