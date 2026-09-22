package br.com.newe.ms_frota.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.newe.ms_frota.models.Agregado;

public interface AgregadoRepository extends JpaRepository<Agregado, UUID> {

    Optional<Agregado> findByDocumento(String documento);

    @Query("select a.documento as documento, a.idAgregado as id from Agregado a where a.documento in :documentos")
    List<AgregadoDocumentoId> buscarIdsPorDocumento(@Param("documentos") Collection<String> documentos);

    interface AgregadoDocumentoId {
        String getDocumento();
        UUID getId();
    }
}
