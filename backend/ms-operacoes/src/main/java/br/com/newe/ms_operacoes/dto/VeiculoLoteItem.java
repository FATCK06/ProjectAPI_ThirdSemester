package br.com.newe.ms_operacoes.dto;

import java.math.BigDecimal;

/**
 * Payload enviado ao ms-frota. O manifesto so traz placa e capacidade. A
 * "Capacidade Veículo" do manifesto e peso (bate com a Capacidade Lotação do
 * cadastro de veiculos), nao o volume em m³.
 */
public record VeiculoLoteItem(
        String placa,
        BigDecimal capacidadeLotacao,
        String nomeAgregado
) {
}
