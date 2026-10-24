package br.com.newe.ms_operacoes.dto.frota;

import java.math.BigDecimal;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonInclude;

import br.com.newe.ms_operacoes.service.indicadores.IndicadoresMotorista;

/**
 * Linha de /api/ranking/mensal. Mesmos nomes de {@link IndicadoresMotorista} (o
 * que /api/dashboard/indicadores devolve), mais o tipo de veiculo: e o mesmo
 * calculo, e nome diferente faria parecer dado diferente.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RankingMensalMotoristaDTO(
        UUID motoristaId,
        String nome,
        String tipoVeiculo,
        long numeroViagens,
        BigDecimal disponibilidade,
        BigDecimal utilizacao,
        BigDecimal valorFrete,
        BigDecimal custoTotal,
        BigDecimal rentabilidade,
        BigDecimal rentabilidadeMediaViagem) {

    private static final String NAO_INFORMADO = "N\u00e3o informado";

    public static RankingMensalMotoristaDTO de(IndicadoresMotorista i, String tipoVeiculo) {
        String nome = i.nome() == null || i.nome().isBlank() ? NAO_INFORMADO : i.nome();
        String tipo = tipoVeiculo == null || tipoVeiculo.isBlank() ? NAO_INFORMADO : tipoVeiculo;
        return new RankingMensalMotoristaDTO(i.motoristaId(), nome, tipo, i.numeroViagens(),
                i.disponibilidade(), i.utilizacao(), i.valorFrete(), i.custoTotal(), i.rentabilidade(),
                i.rentabilidadeMediaViagem());
    }
}
