package br.com.newe.ms_operacoes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Uma linha do CSV de veiculos, convertida e conferida. Vai para o ms-frota, que
 * resolve tipo, cidade, agregado e motorista e grava. Mesmo formato do record de la.
 *
 * Campo null = nao informado (ou valor de enchimento do sistema de origem, como 0,00).
 */
public record VeiculoImportacaoItem(
        int numeroLinha,
        String placa,
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
