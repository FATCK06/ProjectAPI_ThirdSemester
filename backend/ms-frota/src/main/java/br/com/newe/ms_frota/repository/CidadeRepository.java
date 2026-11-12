package br.com.newe.ms_frota.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.newe.ms_frota.models.Cidade;

public interface CidadeRepository extends JpaRepository<Cidade, Integer> {

    /**
     * Todas as cidades das UFs do lote. A comparação do nome (sem acento, sem
     * maiúsculas) fica no ResolvedorCidade: o Postgres só faria isso com a
     * extensão unaccent, que não está habilitada.
     */
    @Query("select c from Cidade c join fetch c.estado e where e.sigla in :ufs")
    List<Cidade> buscarPorUfs(@Param("ufs") Collection<String> ufs);

    /** As 5.571: destino de viagem vem sem UF, então o nome pode estar em qualquer estado. */
    @Query("select c from Cidade c join fetch c.estado")
    List<Cidade> buscarTodas();
}
