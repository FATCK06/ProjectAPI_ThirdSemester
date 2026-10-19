package br.com.newe.ms_operacoes.dto;

import java.time.LocalDateTime;

import br.com.newe.ms_operacoes.models.enums.StatusImportacaoEnum;

public record ImportacaoResumoDTO(
    Long id,
    String nomeArquivo,
    LocalDateTime dataImportacao,
    String usuario,
    StatusImportacaoEnum status
) {}
