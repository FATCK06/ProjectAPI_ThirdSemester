package br.com.newe.ms_frota.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.newe.ms_frota.models.Veiculo;

public interface VeiculoRepository extends JpaRepository<Veiculo, UUID> {

    Optional<Veiculo> findByPlaca(String placa);

    @Query("select v.placa as placa, v.idVeiculo as id from Veiculo v where v.placa in :placas")
    List<VeiculoPlacaId> buscarIdsPorPlaca(@Param("placas") Collection<String> placas);

    interface VeiculoPlacaId {
        String getPlaca();
        UUID getId();
    }
}
