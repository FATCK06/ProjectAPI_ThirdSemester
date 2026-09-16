package br.com.newe.ms_indicadores.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.newe.ms_indicadores.entities.IndicadoresMotoristaEntity;

@Repository
public interface IIndicadoresMotoristaRepository
        extends JpaRepository<IndicadoresMotoristaEntity, Long> {
}
