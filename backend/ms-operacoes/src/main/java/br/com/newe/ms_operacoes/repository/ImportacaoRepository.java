package br.com.newe.ms_operacoes.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import br.com.newe.ms_operacoes.dto.ImportacaoResumoDTO;
import br.com.newe.ms_operacoes.models.Importacao;
import br.com.newe.ms_operacoes.models.enums.StatusImportacaoEnum;

public interface ImportacaoRepository extends JpaRepository<Importacao, Long> {

    List<Importacao> findAllByOrderByDataImportacaoDesc();

    @Query("select i.status as status, count(i) as total from Importacao i group by i.status")
    List<ContagemPorStatus> contarPorStatus();

    interface ContagemPorStatus {
        StatusImportacaoEnum getStatus();
        Long getTotal();
    }

    /**
     * Passa a importacao para EM_PROCESSAMENTO, desde que nenhuma outra esteja
     * nesse estado. Checagem e escrita no mesmo statement; o indice unico da V9
     * cobre duas reservas simultaneas.
     *
     * @return 1 se reservou, 0 se havia outra em andamento ou o status nao permite
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Importacao i
        set i.status = br.com.newe.ms_operacoes.models.enums.StatusImportacaoEnum.EM_PROCESSAMENTO,
            i.processamentoIniciadoEm = :agora
        where i.id = :id
          and i.status in (br.com.newe.ms_operacoes.models.enums.StatusImportacaoEnum.AGUARDANDO,
                           br.com.newe.ms_operacoes.models.enums.StatusImportacaoEnum.VALIDADO,
                           br.com.newe.ms_operacoes.models.enums.StatusImportacaoEnum.ERRO)
          and not exists (
              select o.id from Importacao o
              where o.status = br.com.newe.ms_operacoes.models.enums.StatusImportacaoEnum.EM_PROCESSAMENTO)
        """)
    int reservarProcessamento(@Param("id") Long id, @Param("agora") LocalDateTime agora);

    /**
     * Reserva iniciada antes de {@code limite} e nunca encerrada: a aplicacao caiu
     * no meio da gravacao. Vira ERRO para nao bloquear as proximas importacoes.
     * Sem horario de inicio (gravada por codigo anterior a V9) continua valendo.
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Importacao i
        set i.status = br.com.newe.ms_operacoes.models.enums.StatusImportacaoEnum.ERRO
        where i.status = br.com.newe.ms_operacoes.models.enums.StatusImportacaoEnum.EM_PROCESSAMENTO
          and i.processamentoIniciadoEm < :limite
        """)
    int liberarReservasAbandonadas(@Param("limite") LocalDateTime limite);

    /** So as colunas leves: findBy na entidade traria o arquivo inteiro (bytea). */
    Optional<EmAndamentoView> findFirstByStatusOrderByIdAsc(StatusImportacaoEnum status);

    interface EmAndamentoView {
        Long getId();
        String getArquivoNome();
        String getUsuarioResponsavel();
        LocalDateTime getProcessamentoIniciadoEm();
    }

    @Query("""
        select new br.com.newe.ms_operacoes.dto.ImportacaoResumoDTO(
            i.id, i.arquivoNome, i.dataImportacao, i.usuarioResponsavel, i.status
        )
        from Importacao i
        order by i.dataImportacao desc    
        """)
    List<ImportacaoResumoDTO> listarResumo();    
}
