package br.com.newe.ms_veiculos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.newe.ms_veiculos.models.entity.ArquivoImport;

public interface ArquivoImportRepository extends JpaRepository<ArquivoImport, Long> {
    
}
