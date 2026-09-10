package br.com.newe.ms_veiculos.models.dto;

import lombok.Data;
import java.time.LocalDateTime;

import br.com.newe.ms_veiculos.models.enums.StatusImportacao;

//Não está sendo usado.
//Pode ser usado para retornar os dados dos arquivos importados.
@Data
public class ArquivoImportDTO {
    private Long id;
    private String nomeArquivo;
    private Long tamanhoBytes;
    private String extensao;
    private String mesReferencia;
    private LocalDateTime dataImportacao;
    private StatusImportacao status;


}