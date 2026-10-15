package br.com.newe.ms_operacoes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

    @Query("""
        select new br.com.newe.ms_operacoes.dto.ImportacaoResumoDTO(
            i.id, i.arquivoNome, i.dataImportacao, i.usuarioResponsavel, i.status
        )
        from Importacao i
        order by i.dataImportacao desc    
        """)
    List<ImportacaoResumoDTO> listarResumo();    
}
