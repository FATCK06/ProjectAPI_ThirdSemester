package br.com.newe.ms_frota.dto;

import java.util.UUID;

/**
 * Placa, tipo cru (grafia da planilha de veiculos) e categoria de um veiculo.
 *
 * @param tipo      nulo quando o veiculo nunca veio na planilha de veiculos
 * @param categoria nula quando nao ha tipo ou o tipo nao aponta para nenhuma categoria
 */
public record VeiculoClassificacaoDTO(UUID idVeiculo, String placa, String tipo, String categoria) {
}
