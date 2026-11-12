package br.com.newe.ms_frota.dto;

import java.util.List;

/** Com algum problema que bloqueia, nada foi gravado e criados/atualizados vem zerados. */
public record ResultadoImportacaoVeiculos(List<ProblemaImportacao> problemas, int criados, int atualizados) {

    public boolean bloqueado() {
        return problemas.stream().anyMatch(ProblemaImportacao::bloqueia);
    }
}
