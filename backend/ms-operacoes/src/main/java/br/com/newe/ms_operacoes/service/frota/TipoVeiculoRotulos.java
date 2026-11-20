package br.com.newe.ms_operacoes.service.frota;

/**
 * Rotulos de tipo de veiculo que o ms-operacoes usa quando o ms-frota nao
 * devolve nada para o veiculo. Os demais ("Não classificado" e as categorias)
 * vem prontos do ms-frota.
 */
public final class TipoVeiculoRotulos {

    /** Mesmo texto do VeiculoService.NAO_IDENTIFICADO do ms-frota. */
    public static final String NAO_IDENTIFICADO = "Não identificado";

    private TipoVeiculoRotulos() {
    }
}
