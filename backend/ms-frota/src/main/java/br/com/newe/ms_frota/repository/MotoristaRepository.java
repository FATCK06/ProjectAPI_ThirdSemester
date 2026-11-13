package br.com.newe.ms_frota.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.newe.ms_frota.models.Motorista;

public interface MotoristaRepository extends JpaRepository<Motorista, UUID> {

    Optional<Motorista> findByCpf(String cpf);

    @EntityGraph(attributePaths = "cidade")
    @Query("select m from Motorista m where m.idMotorista = :id")
    Optional<Motorista> buscarPorIdComCidade(@Param("id") UUID id);

    /**
     * Resolve varios CPFs de uma vez: a importacao de um manifesto traz centenas
     * de linhas e nao pode fazer um SELECT por motorista.
     */
    @Query("select m.cpf as cpf, m.idMotorista as id from Motorista m where m.cpf in :cpfs")
    List<MotoristaCpfId> buscarIdsPorCpf(@Param("cpfs") Collection<String> cpfs);

    List<Motorista> findByCpfInAndCidadeIsNull(Collection<String> cpfs);

    interface MotoristaCpfId {
        String getCpf();
        UUID getId();
    }
}
