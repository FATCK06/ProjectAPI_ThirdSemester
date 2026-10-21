package br.com.newe.ms_operacoes.service.frota;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.newe.ms_operacoes.dto.frota.RankingMensalMotoristaDTO;

class RankingServiceTest {

    private static final List<RankingMensalMotoristaDTO> LINHAS = List.of(
            linha("A", "100.00"),
            linha("SEM_MEDIA", null),
            linha("B", "300.00"));

    @Test
    void mediaNulaFicaNoFimNaOrdemDecrescente() {
        assertThat(ordenar("desc")).containsExactly("B", "A", "SEM_MEDIA");
    }

    @Test
    void mediaNulaFicaNoFimNaOrdemCrescente() {
        assertThat(ordenar("asc")).containsExactly("A", "B", "SEM_MEDIA");
    }

    private static List<String> ordenar(String direcao) {
        return LINHAS.stream()
                .sorted(RankingService.comparador("rentabilidadeMediaViagem", direcao))
                .map(RankingMensalMotoristaDTO::motorista)
                .toList();
    }

    private static RankingMensalMotoristaDTO linha(String motorista, String media) {
        return new RankingMensalMotoristaDTO(UUID.randomUUID(), motorista, null, 1,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                media == null ? null : new BigDecimal(media));
    }
}
