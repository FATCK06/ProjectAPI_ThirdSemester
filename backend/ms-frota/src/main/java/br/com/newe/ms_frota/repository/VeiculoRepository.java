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

    @Query(value = "select v.id_veiculo as id, c.descricao as descricao "
            + "from veiculos v left join tipo_veiculos c on c.id_tipo = v.id_tipo "
            + "where v.id_veiculo in :ids", nativeQuery = true)
    List<VeiculoTipoView> buscarTiposPorIds(@Param("ids") Collection<UUID> ids);

    interface VeiculoPlacaId {
        String getPlaca();
        UUID getId();
    }

    interface VeiculoTipoView {
        UUID getId();
        String getDescricao();
    }
}
