package br.com.newe.ms_operacoes.service.importacao;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LinhaManifesto(
        // motorista (quem dirige)
        String nome,
        String cpf,
        String pis,
        LocalDate dataNascimento,
        String endereco,
        String cep,
        String bairro,
        String cidade,
        String estado,
        // equipe de guarnicao
        String chefeGuarnicao,
        String vigilante1,
        String vigilante2,
        // agregado (o contratado; documento com 11 digitos = CPF, 14 = CNPJ)
        String agregadoNome,
        String agregadoDocumento,
        String agregadoPis,
        String regimeFiscal,
        // viagem
        String manifesto,
        String filial,
        LocalDate data,
        String mesReferencia,
        String veiculo,
        String reboque1,
        String reboque2,
        String reboque3,
        String destino,
        Integer kmSaida,
        Integer kmChegada,
        Integer servicos,
        Integer nfs,
        BigDecimal kgReal,
        BigDecimal kgTaxado,
        BigDecimal m3,
        BigDecimal capacidadeVeiculo,
        BigDecimal percentualAprovVeiculo,
        BigDecimal valeFrete,
        BigDecimal valorNf,
        BigDecimal valorFretes,
        BigDecimal valorFrete,
        BigDecimal combustivel,
        BigDecimal pedagio,
        BigDecimal diaria,
        Integer coletas,
        Integer entregas,
        Integer despachos,
        Integer retiradas,
        Integer coletasReversa,
        BigDecimal adicionais,
        BigDecimal descontos,
        BigDecimal adiantamento,
        BigDecimal despesas,
        BigDecimal totalDespesas,
        BigDecimal saldoDespesas,
        BigDecimal inss,
        BigDecimal sestSenat,
        BigDecimal ir,
        BigDecimal saldoAPagar,
        Integer servicosFinalizados,
        BigDecimal percentualEfetividade,
        String classificacao,
        String observacoesOperacionais,
        String statusManifesto,
        String usuarioManifesto,
        int numeroLinha
) {

    /** "PF" para documento de 11 digitos (CPF), "PJ" para 14 (CNPJ). */
    public String tipoPessoaAgregado() {
        if (agregadoDocumento == null) {
            return null;
        }
        return agregadoDocumento.length() == 14 ? "PJ" : "PF";
    }

    /** viagens.id_manifesto e integer; o parser ja garantiu que e numerico. */
    public Integer idManifesto() {
        return Integer.valueOf(manifesto);
    }
}
