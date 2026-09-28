package br.com.newe.ms_operacoes.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.newe.ms_operacoes.models.TipoCusto;

public interface TipoCustoRepository extends JpaRepository<TipoCusto, Integer> {
}
