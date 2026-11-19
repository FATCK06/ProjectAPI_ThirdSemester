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

    List<Veiculo> findByPlacaIn(Collection<String> placas);

    List<Veiculo> findByRenavamIn(Collection<String> renavams);

    List<Veiculo> findByChassiIn(Collection<String> chassis);

    @Query("select v.placa as placa, v.idVeiculo as id from Veiculo v where v.placa in :placas")
    List<VeiculoPlacaId> buscarIdsPorPlaca(@Param("placas") Collection<String> placas);

    /** Tipo cru e categoria de cada veiculo; os dois vem nulos quando o veiculo nao tem tipo. */
    @Query(value = "select v.id_veiculo as \"idVeiculo\", v.placa as placa, "
            + "t.descricao as tipo, c.descricao as categoria "
            + "from veiculos v "
            + "left join tipo_veiculos t on t.id_tipo = v.id_tipo "
            + "left join categorias_veiculo c on c.id_categoria = t.id_categoria "
            + "where v.id_veiculo in :ids", nativeQuery = true)
    List<VeiculoClassificacaoView> buscarClassificacaoPorIds(@Param("ids") Collection<UUID> ids);

    @Query(value = "select id_categoria as \"idCategoria\", descricao "
            + "from categorias_veiculo order by ordem", nativeQuery = true)
    List<CategoriaVeiculoView> buscarCategorias();

    @Query(value = "select id_tipo as \"idTipo\", descricao "
            + "from tipo_veiculos order by descricao", nativeQuery = true)
    List<TipoVeiculoView> buscarTodosTipos();

    interface VeiculoPlacaId {
        String getPlaca();
        UUID getId();
    }

    interface VeiculoClassificacaoView {
        UUID getIdVeiculo();
        String getPlaca();
        String getTipo();
        String getCategoria();
    }

    interface CategoriaVeiculoView {
        Integer getIdCategoria();
        String getDescricao();
    }

    interface TipoVeiculoView {
        Integer getIdTipo();
        String getDescricao();
    }
}
