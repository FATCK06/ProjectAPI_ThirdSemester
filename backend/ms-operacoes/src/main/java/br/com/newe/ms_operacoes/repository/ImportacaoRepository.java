package br.com.newe.ms_operacoes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}
