package br.com.newe.ms_operacoes.dto;

import java.math.BigDecimal;

/** Payload enviado ao ms-frota. O manifesto so traz placa e capacidade. */
public record VeiculoLoteItem(
        String placa,
        BigDecimal capacidade,
        String nomeAgregado
) {
}
