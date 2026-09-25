package br.com.newe.ms_operacoes.repository;

import java.math.BigDecimal;
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

    /**
     * Somas por motorista no mes; as formulas ficam em CalculoIndicadores.
     *
     * PENDENTE (cliente): frete = valor_frete e custo = total_despesas sao
     * provisorios. O CSV tambem traz valor_fretes, e viagem_custos mistura custo
     * com desconto, adiantamento e retencao - somar tudo daria numero errado.
     */
    @Query("select v.idMotorista as motoristaId, "
            + "count(v) as numeroViagens, "
            + "count(distinct v.dataViagem) as diasOperacao, "
            + "sum(v.valorFrete) as valorFrete, "
            + "sum(v.totalDespesas) as custoTotal "
            + "from Viagem v "
            + "where v.mesReferencia = :mesReferencia "
            + "group by v.idMotorista")
    List<SomaMotoristaView> somasPorMotorista(@Param("mesReferencia") String mesReferencia);

    @Query("select v.idVeiculo as veiculoId, "
            + "count(v) as numeroViagens, "
            + "count(distinct v.dataViagem) as diasOperacao, "
            + "sum(v.valorFrete) as valorFrete, "
            + "sum(v.totalDespesas) as custoTotal "
            + "from Viagem v "
            + "where v.mesReferencia = :mesReferencia "
            + "group by v.idVeiculo")
    List<SomaVeiculoView> somasPorVeiculo(@Param("mesReferencia") String mesReferencia);

    interface SomaVeiculoView {
        UUID getVeiculoId();

        Long getNumeroViagens();

        Long getDiasOperacao();

        BigDecimal getValorFrete();

        BigDecimal getCustoTotal();
    }

    interface SomaMotoristaView {
        UUID getMotoristaId();

        Long getNumeroViagens();

        Long getDiasOperacao();

        BigDecimal getValorFrete();

        BigDecimal getCustoTotal();
    }

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
