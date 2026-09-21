package br.com.newe.ms_veiculos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.newe.ms_veiculos.models.ArquivoImportacao;
import br.com.newe.ms_veiculos.models.enums.StatusImportacaoEnum;

public interface ArquivoImportacaoRepository extends JpaRepository<ArquivoImportacao, Long> {

    @Query("select a.status as status, count(a) as total "
            + "from ArquivoImportacao a "
            + "where a.mesReferencia = :mesReferencia "
            + "group by a.status")
    List<ContagemPorStatus> contarPorStatus(@Param("mesReferencia") String mesReferencia);

    interface ContagemPorStatus {
        StatusImportacaoEnum getStatus();
        Long getTotal();
    }
}
