package br.com.newe.ms_operacoes.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.newe.ms_operacoes.models.ControleDisponibilidade;

public interface ControleDisponibilidadeRepository extends JpaRepository<ControleDisponibilidade, UUID> {

    List<ControleDisponibilidade> findByMesReferencia(String mesReferencia);
}
