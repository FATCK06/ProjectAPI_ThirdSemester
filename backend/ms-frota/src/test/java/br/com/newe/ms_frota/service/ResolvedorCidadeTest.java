package br.com.newe.ms_frota.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import br.com.newe.ms_frota.models.Cidade;
import br.com.newe.ms_frota.models.Estado;

class ResolvedorCidadeTest {

    @Test
    void grafiasDiferentesDaMesmaCidadeGeramAMesmaChave() {
        String ibge = ResolvedorCidade.chave("Jacareí", "SP");
        assertEquals(ibge, ResolvedorCidade.chave("JACAREI", "sp"));
        assertEquals(ibge, ResolvedorCidade.chave("  jacareí ", " SP "));

        assertEquals(ResolvedorCidade.chave("Embu-Guaçu", "SP"), ResolvedorCidade.chave("embu guacu", "SP"));
        assertEquals(ResolvedorCidade.chave("Santa Bárbara d'Oeste", "SP"),
                ResolvedorCidade.chave("SANTA BARBARA D OESTE", "SP"));
    }

    @Test
    void mesmaCidadeEmOutraUfNaoCasa() {
        assertEquals("BELEM|PA", ResolvedorCidade.chave("Belém", "PA"));
        assertEquals("BELEM|AL", ResolvedorCidade.chave("Belém", "AL"));
    }

    @Test
    void semNomeOuSemUfNaoGeraChave() {
        assertNull(ResolvedorCidade.chave(null, "SP"));
        assertNull(ResolvedorCidade.chave("Jacareí", " "));
        assertNull(ResolvedorCidade.chave(" - ", "SP"));
    }

    @Test
    void homonimoDesempataPelaRotaDepoisCapitalDepoisSp() {
        Cidade belemPa = cidade(1, "Belém", "PA");
        Cidade belemPb = cidade(2, "Belém", "PB");
        Cidade itapevaSp = cidade(3, "Itapeva", "SP");
        Cidade itapevaMg = cidade(4, "Itapeva", "MG");
        Cidade cascavelPr = cidade(5, "Cascavel", "PR");
        Cidade cascavelCe = cidade(6, "Cascavel", "CE");

        // Outra parada da viagem na PB vence a capital.
        assertEquals(belemPb, ResolvedorCidade.escolher(List.of(belemPa, belemPb), Set.of("PB")));
        assertEquals(belemPa, ResolvedorCidade.escolher(List.of(belemPa, belemPb), Set.of()));
        assertEquals(itapevaSp, ResolvedorCidade.escolher(List.of(itapevaSp, itapevaMg), Set.of()));
        assertEquals(cascavelPr, ResolvedorCidade.escolher(List.of(cascavelPr, cascavelCe), Set.of("PR")));
        // Sem regra que desempate: melhor sem parada que parada errada.
        assertNull(ResolvedorCidade.escolher(List.of(cascavelPr, cascavelCe), Set.of("SP")));
        assertNull(ResolvedorCidade.escolher(List.of(), Set.of()));
    }

    private static Cidade cidade(int id, String nome, String uf) {
        Estado estado = new Estado();
        estado.setSigla(uf);
        Cidade cidade = new Cidade();
        cidade.setIdCidade(id);
        cidade.setNomeCidade(nome);
        cidade.setEstado(estado);
        return cidade;
    }

    @Test
    void placaConvertidaParaMercosulEhAMesma() {
        assertTrue(Placas.mesmaConvertida("FCB9362", "FCB9D62"));
        assertTrue(Placas.mesmaConvertida("EKZ4F16", "EKZ4516"));
        assertFalse(Placas.mesmaConvertida("FFL6F10", "CUA1A88"));
        assertFalse(Placas.mesmaConvertida("FCB9362", "FCB9E62"));
        assertFalse(Placas.mesmaConvertida("FCB9362", "FCB9362"));
    }
}
