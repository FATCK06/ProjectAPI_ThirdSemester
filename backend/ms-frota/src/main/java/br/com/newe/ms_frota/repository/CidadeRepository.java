package br.com.newe.ms_frota.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.newe.ms_frota.models.Cidade;

public interface CidadeRepository extends JpaRepository<Cidade, Integer> {

    /**
     * O CSV traz cidade e UF como texto livre ("Cidade Motorista" / "Estado
     * Motorista"), sem id. Busca sem diferenciar maiusculas nem espacos nas pontas.
     */
    @Query("select c from Cidade c "
            + "where upper(trim(c.nomeCidade)) = upper(trim(:nome)) "
            + "and upper(trim(c.estado.sigla)) = upper(trim(:uf))")
    Optional<Cidade> buscarPorNomeEUf(@Param("nome") String nome, @Param("uf") String uf);
}
