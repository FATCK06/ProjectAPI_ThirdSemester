package br.com.newe.ms_operacoes.dto;

import java.util.List;

/** Resposta do ms-frota ao gravar. Com problema que bloqueia, nada foi gravado. */
public record ResultadoImportacaoVeiculos(List<ProblemaImportacao> problemas, int criados, int atualizados) {

    public boolean bloqueado() {
        return problemas.stream().anyMatch(ProblemaImportacao::bloqueia);
    }
}
