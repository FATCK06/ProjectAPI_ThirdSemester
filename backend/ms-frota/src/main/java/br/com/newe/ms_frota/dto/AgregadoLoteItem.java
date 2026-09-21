package br.com.newe.ms_frota.dto;

/** Um agregado vindo da importacao. Documento so com digitos: 11 = CPF, 14 = CNPJ. */
public record AgregadoLoteItem(
        String documento,
        String nome,
        String pis,
        String regimeFiscal
) {
}
