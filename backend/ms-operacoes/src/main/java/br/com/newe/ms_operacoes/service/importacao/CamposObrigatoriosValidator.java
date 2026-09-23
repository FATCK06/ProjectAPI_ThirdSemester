package br.com.newe.ms_operacoes.service.importacao;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

/** Valida os campos obrigatorios de cada registro antes da conversao. */
@Component
public class CamposObrigatoriosValidator {

    public List<String> camposVazios(CSVRecord registro) {
        return ManifestoCsvConfig.COLUNAS_ESPERADAS.stream()
                .filter(ManifestoCsvConfig.ColunaEsperada::obrigatoria)
                .filter(coluna -> campoVazio(registro, coluna.nome()))
                .map(ManifestoCsvConfig.ColunaEsperada::nome)
                .toList();
    }

    private boolean campoVazio(CSVRecord registro, String nomeColuna) {
        try {
            return ManifestoCsvConfig.textoOuNulo(registro.get(nomeColuna)) == null;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }
}