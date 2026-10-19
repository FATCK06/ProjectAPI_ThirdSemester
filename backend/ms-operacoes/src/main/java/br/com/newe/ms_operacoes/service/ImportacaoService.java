package br.com.newe.ms_operacoes.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.newe.ms_operacoes.models.Importacao;
import br.com.newe.ms_operacoes.models.ResumoImportacao;
import br.com.newe.ms_operacoes.models.enums.StatusImportacaoEnum;
import br.com.newe.ms_operacoes.repository.ImportacaoRepository;
import br.com.newe.ms_operacoes.repository.ImportacaoRepository.ContagemPorStatus;
import br.com.newe.ms_operacoes.repository.ImportacaoRepository.EmAndamentoView;

import br.com.newe.ms_operacoes.dto.ImportacaoResumoDTO;

/**
 * Ciclo de vida da importacao. Nao parseia nem grava viagens - so guarda o
 * arquivo e acompanha o estado enquanto ele passa pelas etapas da tela.
 */
@Service
public class ImportacaoService {

    /**
     * Reserva mais velha que isto e tratada como abandonada (a aplicacao caiu no
     * meio da gravacao).
     */
    static final Duration RESERVA_ABANDONADA_APOS = Duration.ofMinutes(30);

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

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

    @Transactional(readOnly = true)
    public List<ImportacaoResumoDTO> listarResumo() {
        return repository.listarResumo();
    }

    /**
     * Registra o que o parse encontrou. Continua sem gravar viagem nenhuma.
     *
     * Validar de novo uma importacao em andamento ou concluida nao mexe no status:
     * voltar para VALIDADO soltaria a reserva e permitiria executar outra vez.
     */
    @Transactional
    public void marcarValidado(Long id, int total, int validas, int invalidas) {
        repository.findById(id).ifPresent(importacao -> {
            if (importacao.getStatus() != StatusImportacaoEnum.EM_PROCESSAMENTO
                    && importacao.getStatus() != StatusImportacaoEnum.CONCLUIDO) {
                importacao.setStatus(StatusImportacaoEnum.VALIDADO);
            }
            importacao.setTotalLinhas(total);
            importacao.setLinhasValidas(validas);
            importacao.setLinhasInvalidas(invalidas);
            repository.save(importacao);
        });
    }

    /**
     * Reserva a gravacao: so uma importacao fica EM_PROCESSAMENTO por vez.
     * Sem transacao propria de proposito: cada passo roda na sua.
     * 
     * @throws ExecucaoRecusadaException se outra estiver gravando ou se esta ja
     *                                   tiver sido executada
     */
    public void reservarProcessamento(Long id) {
        LocalDateTime agora = LocalDateTime.now();
        repository.liberarReservasAbandonadas(agora.minus(RESERVA_ABANDONADA_APOS));

        int reservadas;
        try {
            reservadas = repository.reservarProcessamento(id, agora);
        } catch (DataIntegrityViolationException e) {
            // Outra reserva entrou no mesmo instante e o indice unico barrou esta.
            reservadas = 0;
        }

        if (reservadas == 0) {
            throw motivoDaRecusa(id);
        }
    }

    /** A importacao que esta gravando agora, se houver. Reserva abandonada nao conta. */
    @Transactional(readOnly = true)
    public Optional<EmAndamentoView> emAndamento() {
        LocalDateTime limite = LocalDateTime.now().minus(RESERVA_ABANDONADA_APOS);
        return repository.findFirstByStatusOrderByIdAsc(StatusImportacaoEnum.EM_PROCESSAMENTO)
                .filter(i -> i.getProcessamentoIniciadoEm() == null || !i.getProcessamentoIniciadoEm().isBefore(limite));
    }

    private ExecucaoRecusadaException motivoDaRecusa(Long id) {
        Optional<EmAndamentoView> outra = emAndamento();
        if (outra.isPresent()) {
            EmAndamentoView o = outra.get();
            if (o.getId().equals(id)) {
                return new ExecucaoRecusadaException("Esta importacao ja esta sendo gravada. Aguarde o fim.");
            }
            String desde = o.getProcessamentoIniciadoEm() == null
                    ? ""
                    : ", iniciada as " + o.getProcessamentoIniciadoEm().format(HORA);
            return new ExecucaoRecusadaException("Ja existe uma importacao sendo gravada (" + o.getArquivoNome()
                    + ", enviada por " + o.getUsuarioResponsavel() + desde + "). Aguarde ela terminar e tente de novo.");
        }

        Importacao esta = buscarPorId(id);
        if (esta != null && esta.getStatus() == StatusImportacaoEnum.CONCLUIDO) {
            return new ExecucaoRecusadaException("Esta importacao ja foi executada");
        }
        // A outra terminou entre a reserva e esta consulta.
        return new ExecucaoRecusadaException("Outra importacao acabou de ser gravada. Tente de novo.");
    }

    /** Execucao recusada sem culpa do arquivo: vira 409 na API. */
    public static class ExecucaoRecusadaException extends RuntimeException {
        public ExecucaoRecusadaException(String mensagem) {
            super(mensagem);
        }
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
