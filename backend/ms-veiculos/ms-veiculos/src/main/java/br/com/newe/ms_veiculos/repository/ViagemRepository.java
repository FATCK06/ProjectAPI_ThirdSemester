package br.com.newe.ms_veiculos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.newe.ms_veiculos.models.Motorista;
import br.com.newe.ms_veiculos.models.Viagem;

public interface ViagemRepository extends JpaRepository<Viagem, Long> {

    // @Query explicito (em vez de query derivada por nome) porque o getter "achatado"
    // Viagem.getArquivoImportacaoId() (usado no JSON) colide com o nome que o Spring Data
    // tentaria inferir aqui, fazendo-o tratar "arquivoImportacaoId" como uma unica propriedade
    // em vez de navegar arquivoImportacao.id.
    @Query("select v from Viagem v where v.arquivoImportacao.id = :arquivoImportacaoId")
    List<Viagem> findByArquivoImportacaoId(@Param("arquivoImportacaoId") Long arquivoImportacaoId);

    List<Viagem> findByMesReferencia(String mesReferencia);

    @Query("select distinct v.motorista from Viagem v where v.arquivoImportacao.id = :arquivoImportacaoId")
    List<Motorista> buscarMotoristasDistintosPorArquivo(@Param("arquivoImportacaoId") Long arquivoImportacaoId);
}
