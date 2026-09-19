package br.com.newe.ms_veiculos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GerencialResponseDTO(
        String manifesto,
        String filial,
        LocalDate data,
        String motorista,
        String cpf,
        String pis,
        LocalDate dataNascimento,
        String enderecoMotorista,
        String cepMotorista,
        String bairroMotorista,
        String cidadeMotorista,
        String estadoMotorista,
        String chefeGuarnicao,
        String vigilante1,
        String vigilante2,
        String agregado,
        String cpfCnpjAgregado,
        String pisAgregado,
        String regimeFiscal,
        String veiculo,
        String reboque1,
        String reboque2,
        String reboque3,
        String destino,
        String servicos,
        String nfs,
        Double kgReal,
        Double kgTaxado,
        Double m3,

        // Financeiro
        BigDecimal valeFrete,
        BigDecimal valorNf,
        BigDecimal valorFretes,

        String servicosFinalizados,
        Double percentualEfetividade,
        Double capacidadeVeiculo,
        Double percentualAprovVeiculo,
        Double kmSaida,
        Double kmChegada,

        // Financeiro
        BigDecimal valorFrete,
        BigDecimal combustivel,
        BigDecimal pedagio,
        BigDecimal diaria,

        Integer coletas,
        Integer entregas,
        Integer despachos,
        Integer retiradas,
        Integer coletasReversa,

        // Financeiro
        BigDecimal adicionais,
        BigDecimal descontos,
        BigDecimal adiantamento,
        BigDecimal despesas,
        BigDecimal totalDespesas,
        BigDecimal saldoDespesas,
        BigDecimal inss,
        BigDecimal sestSenat,
        BigDecimal ir,
        BigDecimal saldoPagar,

        String classificacao,
        String observacoesOperacionais,
        String status,
        String usuario) {
}
