package br.com.newe.ms_veiculos.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.newe.ms_veiculos.models.Motorista;

public interface MotoristaRepository extends JpaRepository<Motorista, Long> {

    Optional<Motorista> findByCpf(String cpf);

    @Query("select m.cpf as cpf, m.id as id from Motorista m where m.cpf in :cpfs")
    List<MotoristaCpfId> buscarIdsPorCpf(@Param("cpfs") Collection<String> cpfs);

    interface MotoristaCpfId {
        String getCpf();
        Long getId();
    }
}
