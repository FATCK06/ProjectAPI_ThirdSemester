package br.com.newe.ms_indicadores.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import br.com.newe.ms_indicadores.entities.IndicadoresMotoristaEntity;
import br.com.newe.ms_indicadores.repository.IIndicadoresMotoristaRepository;

@Service
public class IndicadoresMotoristaService {

        private final IIndicadoresMotoristaRepository repository;

        public IndicadoresMotoristaService(IIndicadoresMotoristaRepository repository) {
                this.repository = repository;
        }

        public IndicadoresMotoristaEntity calcularEGravar(
                        Long motoristaId,
                        Integer diasOperacao,
                        Integer diasDisponiveis,
                        Integer numeroViagens,
                        BigDecimal valorFrete,
                        BigDecimal custosOperacao) {

                BigDecimal frete = (valorFrete == null) ? BigDecimal.ZERO : valorFrete;
                BigDecimal custos = (custosOperacao == null) ? BigDecimal.ZERO : custosOperacao;

                BigDecimal utilizacao = calcularUtilizacao(diasOperacao, diasDisponiveis);
                BigDecimal rentabilidade = frete.subtract(custos);
                BigDecimal rentabilidadeMediaViagem = calcularRentabilidadeMedia(rentabilidade, numeroViagens);

                IndicadoresMotoristaEntity indicador = new IndicadoresMotoristaEntity();
                indicador.setMotoristaId(motoristaId);
                indicador.setDiasOperacao(diasOperacao);
                indicador.setDiasDisponiveis(diasDisponiveis);
                indicador.setNumeroViagens(numeroViagens);
                indicador.setValorFrete(frete);
                indicador.setCustosOperacao(custos);
                indicador.setUtilizacao(utilizacao);
                indicador.setRentabilidade(rentabilidade);
                indicador.setRentabilidadeMediaViagem(rentabilidadeMediaViagem);

                return repository.save(indicador);
        }

        private BigDecimal calcularUtilizacao(Integer diasOperacao, Integer diasDisponiveis) {
                if (diasDisponiveis == null || diasDisponiveis == 0 || diasOperacao == null) {
                        return BigDecimal.ZERO;
                }

                return BigDecimal.valueOf(diasOperacao)
                                .multiply(BigDecimal.valueOf(100))
                                .divide(BigDecimal.valueOf(diasDisponiveis), 2, RoundingMode.HALF_UP);
        }

        private BigDecimal calcularRentabilidadeMedia(BigDecimal rentabilidade, Integer numeroViagens) {
                if (numeroViagens == null || numeroViagens == 0) {
                        return BigDecimal.ZERO;
                }

                return rentabilidade.divide(BigDecimal.valueOf(numeroViagens), 2, RoundingMode.HALF_UP);
        }
}
