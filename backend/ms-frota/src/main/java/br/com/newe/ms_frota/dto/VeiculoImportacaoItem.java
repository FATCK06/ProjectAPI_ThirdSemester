package br.com.newe.ms_frota.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;

/**
 * Uma linha do CSV de veiculos, ja convertida e validada pelo ms-operacoes
 * (formato, enums, duplicados no arquivo). Aqui so falta o que depende do banco:
 * tipo, cidade, agregado, motorista e Renavam/chassi ja cadastrados.
 *
 * Campo null = nao informado: na atualizacao, mantem o que ja esta no banco.
 */
public record VeiculoImportacaoItem(
        int numeroLinha,
        @NotBlank String placa,
        String marca,
        String modelo,
        String cor,
        String combustivel,
        String numeroFrota,
        Integer anoFabricacao,
        Integer anoModelo,
        String renavam,
        String chassi,
        String cidadeLicenciamento,
        String ufLicenciamento,
        String tipoVeiculo,
        String tipoCarroceria,
        String tipoProprietario,
        String proprietario,
        String agregado,
        String agregadoDocumento,
        String tabelaAgregado,
        String motoristaPadrao,
        LocalDate dataCompra,
        BigDecimal valorCompra,
        LocalDate dataVenda,
        BigDecimal valorVenda,
        BigDecimal pbt,
        BigDecimal tara,
        BigDecimal capacidade,
        BigDecimal capacidadeLotacao,
        String numeroAutorizacao,
        LocalDate validadeGuarda,
        String status,
        String loteFechamentoCiot
) {
}
