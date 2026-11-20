package br.com.newe.ms_operacoes.dto;

import java.util.UUID;

/**
 * Resposta do ms-frota em /api/veiculos/classificacao.
 *
 * @param tipo      tipo cru da planilha de veiculos; nulo quando o veiculo so veio por manifesto
 * @param categoria nula quando nao ha tipo ou o tipo nao tem categoria
 */
public record VeiculoClassificacao(UUID idVeiculo, String placa, String tipo, String categoria) {
}
