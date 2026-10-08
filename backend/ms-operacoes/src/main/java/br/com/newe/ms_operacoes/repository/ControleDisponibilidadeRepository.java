package br.com.newe.ms_operacoes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.newe.ms_operacoes.models.ControleDisponibilidade;
import br.com.newe.ms_operacoes.models.enums.SituacaoMotorista;

public interface ControleDisponibilidadeRepository extends JpaRepository<ControleDisponibilidade, Integer> {

    List<ControleDisponibilidade> findByMesReferencia(String mesReferencia);

    List<ControleDisponibilidade> findByMesReferenciaAndSituacao(String mesReferencia, SituacaoMotorista situacao);
}
