package br.com.newe.ms_frota.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class VeiculoServiceRotuloTest {

    @Test
    void semTipoEhNaoIdentificado() {
        // Veiculo criado pela importacao de manifestos, que so traz a placa.
        assertEquals(VeiculoService.NAO_IDENTIFICADO, VeiculoService.rotulo(null, null));
    }

    @Test
    void tipoSemCategoriaEhNaoClassificado() {
        assertEquals(VeiculoService.NAO_CLASSIFICADO, VeiculoService.rotulo("Automóveis", null));
    }

    @Test
    void tipoComCategoriaMostraACategoria() {
        assertEquals("Truck", VeiculoService.rotulo("Bi-Truck ou pesado", "Truck"));
    }
}
