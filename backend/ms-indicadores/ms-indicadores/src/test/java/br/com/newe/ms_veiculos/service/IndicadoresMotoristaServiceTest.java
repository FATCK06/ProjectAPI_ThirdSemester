package br.com.newe.ms_veiculos.service;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.newe.ms_indicadores.entities.IndicadoresMotoristaEntity;
import br.com.newe.ms_indicadores.repository.IIndicadoresMotoristaRepository;
import br.com.newe.ms_indicadores.service.IndicadoresMotoristaService;

@ExtendWith(MockitoExtension.class)
class IndicadoresMotoristaServiceTest {

    @Mock
    private IIndicadoresMotoristaRepository repository;

    @InjectMocks
    private IndicadoresMotoristaService service;

    @Test
    void deveCalcularIndicadoresCorretamente() {
        Long motoristaId = 1L;
        Integer diasOperacao = 20;
        Integer diasDisponiveis = 25;
        Integer numeroViagens = 10;

        BigDecimal valorFrete = new BigDecimal("10000.00");
        BigDecimal custosOperacao = new BigDecimal("6000.00");

        when(repository.save(any(IndicadoresMotoristaEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        IndicadoresMotoristaEntity resultado = service.calcularEGravar(
                motoristaId,
                diasOperacao,
                diasDisponiveis,
                numeroViagens,
                valorFrete,
                custosOperacao);

        assertEquals(new BigDecimal("80.0000"), resultado.getUtilizacao());
        assertEquals(new BigDecimal("4000.00"), resultado.getRentabilidade());
        assertEquals(new BigDecimal("400.00"), resultado.getRentabilidadeMediaViagem());
    }

    @Test
    void deveRetornarUtilizacaoZeroQuandoNaoHaDiasDisponiveis() {
        when(repository.save(any(IndicadoresMotoristaEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        IndicadoresMotoristaEntity resultado = service.calcularEGravar(
                1L,
                0,
                0,
                0,
                new BigDecimal("0.00"),
                new BigDecimal("0.00"));

        assertEquals(BigDecimal.ZERO, resultado.getUtilizacao());
    }

    @Test
    void deveRetornarRentabilidadeMediaZeroQuandoNaoHaViagens() {
        when(repository.save(any(IndicadoresMotoristaEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        IndicadoresMotoristaEntity resultado = service.calcularEGravar(
                1L,
                20,
                25,
                0,
                new BigDecimal("10000.00"),
                new BigDecimal("6000.00"));

        assertEquals(
                BigDecimal.ZERO,
                resultado.getRentabilidadeMediaViagem());
    }
}
