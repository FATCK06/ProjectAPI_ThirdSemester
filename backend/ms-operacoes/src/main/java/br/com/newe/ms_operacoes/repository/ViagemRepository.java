package br.com.newe.ms_operacoes.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.newe.ms_operacoes.models.Viagem;

public interface ViagemRepository extends JpaRepository<Viagem, Integer> {

    List<Viagem> findByImportacaoId(Long importacaoId);

    List<Viagem> findByMesReferencia(String mesReferencia);

    /**
     * So os ids: nome e CPF do motorista pertencem ao ms-frota. Quem precisar do
     * dado completo consulta la com estes ids.
     */
    @Query("select distinct v.idMotorista from Viagem v where v.importacaoId = :importacaoId")
    List<UUID> buscarIdsMotoristasPorArquivo(@Param("importacaoId") Long importacaoId);

    /**
     * Manifestos ja gravados, entre os informados. Usado para nao reimportar o que
     * ja entrou: id_manifesto e UNIQUE, e sem esse filtro a segunda importacao do
     * mesmo arquivo estouraria a constraint.
     */
    @Query("select v.idManifesto from Viagem v where v.idManifesto in :manifestos")
    List<Integer> buscarManifestosExistentes(@Param("manifestos") Collection<Integer> manifestos);

    @Query("select v.idMotorista as motoristaId, count(v) as totalViagens "
            + "from Viagem v "
            + "where v.mesReferencia = :mesReferencia "
            + "group by v.idMotorista "
            + "order by count(v) desc, v.idMotorista asc")
    List<RankingMotoristaView> rankingMotoristas(@Param("mesReferencia") String mesReferencia, Pageable pageable);

    @Query("select v.idMotorista as motoristaId, v.idVeiculo as veiculoId, v.kmSaida as kmSaida, v.kmChegada as kmChegada "
            + "from Viagem v "
            + "where v.mesReferencia = :mesReferencia and v.idMotorista in :motoristaIds "
            + "order by v.dataViagem desc, v.idManifesto desc")
    List<ViagemKmView> kmViagensPorMotoristas(
            @Param("mesReferencia") String mesReferencia,
            @Param("motoristaIds") Collection<UUID> motoristaIds);

    interface RankingMotoristaView {
        UUID getMotoristaId();
        Long getTotalViagens();
    }

    interface ViagemKmView {
        UUID getMotoristaId();
        UUID getVeiculoId();
        Integer getKmSaida();
        Integer getKmChegada();
    }
}
