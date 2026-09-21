package br.com.newe.ms_operacoes.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.newe.ms_operacoes.models.Viagem;

public interface ViagemRepository extends JpaRepository<Viagem, Integer> {

    List<Viagem> findByArquivoImportacaoId(Long arquivoImportacaoId);

    List<Viagem> findByMesReferencia(String mesReferencia);

    /**
     * So os ids: nome e CPF do motorista pertencem ao ms-frota. Quem precisar do
     * dado completo consulta la com estes ids.
     */
    @Query("select distinct v.idMotorista from Viagem v where v.arquivoImportacaoId = :arquivoImportacaoId")
    List<UUID> buscarIdsMotoristasPorArquivo(@Param("arquivoImportacaoId") Long arquivoImportacaoId);

    /**
     * Manifestos ja gravados, entre os informados. Usado para nao reimportar o que
     * ja entrou: id_manifesto e UNIQUE, e sem esse filtro a segunda importacao do
     * mesmo arquivo estouraria a constraint.
     */
    @Query("select v.idManifesto from Viagem v where v.idManifesto in :manifestos")
    List<Integer> buscarManifestosExistentes(@Param("manifestos") Collection<Integer> manifestos);
}
