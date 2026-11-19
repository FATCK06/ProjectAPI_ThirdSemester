package br.com.newe.ms_frota.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record MotoristaFichaDTO(
        UUID idMotorista,
        String nome,
        String cpf,
        String pis,
        LocalDate dataNascimento,
        String cep,
        String logradouro,
        String numero,
        String bairro,
        Integer idCidade,
        String nomeCidade,
        LocalDateTime criadoEm) {
}
