package br.com.newe.ms_operacoes.dto;

/** Payload enviado ao ms-frota. Documento so com digitos: 11 = CPF, 14 = CNPJ. */
public record AgregadoLoteItem(
        String documento,
        String nome,
        String pis,
        String regimeFiscal
) {
}
