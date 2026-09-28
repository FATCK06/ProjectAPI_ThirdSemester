package br.com.newe.ms_frota.dto;

import java.math.BigDecimal;

/**
 * Um veiculo vindo da importacao. O CSV de manifestos traz so a placa (e, as
 * vezes, capacidade e nome do agregado), entao o cadastro nasce minimo e e
 * completado depois pelo CRUD de frota.
 */
public record VeiculoLoteItem(
        String placa,
        BigDecimal capacidade,
        String nomeAgregado
) {
}
