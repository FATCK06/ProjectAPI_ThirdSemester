package br.com.newe.ms_operacoes.service.indicadores;

import java.math.BigDecimal;
import java.util.Objects;

import br.com.newe.ms_operacoes.models.enums.FaixaUtilizacao;
import br.com.newe.ms_operacoes.models.enums.SituacaoMotorista;

/**
 * Classifica a utilizacao do motorista no mes em faixa e situacao. Sem Spring e
 * sem banco, para ser testado isolado; os cortes chegam de fora
 * (situacao.utilizacao.* em application.properties).
 *
 * Faixas:
 *   BAIXA          utilizacao < corteBaixa
 *   INTERMEDIARIA  corteBaixa <= utilizacao < corteAlta
 *   ALTA           utilizacao >= corteAlta (acima de 100% tambem, sem truncar)
 *
 * Dias disponiveis <= 0 nao tem utilizacao: sai sem faixa e com situacao
 * SEM_DIAS_DISPONIVEIS, sem dividir por zero.
 */
public final class ClassificadorSituacao {

    private final BigDecimal corteBaixa;
    private final BigDecimal corteAlta;

    public ClassificadorSituacao(BigDecimal corteBaixa, BigDecimal corteAlta) {
        Objects.requireNonNull(corteBaixa, "corteBaixa");
        Objects.requireNonNull(corteAlta, "corteAlta");
        if (corteBaixa.compareTo(corteAlta) > 0) {
            throw new IllegalArgumentException(
                    "corte baixo (" + corteBaixa + ") maior que corte alto (" + corteAlta + ")");
        }
        this.corteBaixa = corteBaixa;
        this.corteAlta = corteAlta;
    }

    public Classificacao classificar(long diasOperacao, long diasDisponiveis) {
        if (diasDisponiveis <= 0) {
            return new Classificacao(null, null, SituacaoMotorista.SEM_DIAS_DISPONIVEIS);
        }
        BigDecimal utilizacao = CalculoIndicadores.utilizacao(diasOperacao, diasDisponiveis);
        FaixaUtilizacao faixa = faixaDa(utilizacao);
        return new Classificacao(utilizacao, faixa, situacaoDa(faixa));
    }

    FaixaUtilizacao faixaDa(BigDecimal utilizacao) {
        if (utilizacao.compareTo(corteAlta) >= 0) {
            return FaixaUtilizacao.ALTA;
        }
        if (utilizacao.compareTo(corteBaixa) >= 0) {
            return FaixaUtilizacao.INTERMEDIARIA;
        }
        return FaixaUtilizacao.BAIXA;
    }

    /** Unico lugar do mapeamento faixa -> situacao. */
    static SituacaoMotorista situacaoDa(FaixaUtilizacao faixa) {
        return switch (faixa) {
            case ALTA -> SituacaoMotorista.INDISPONIVEL;
            case INTERMEDIARIA, BAIXA -> SituacaoMotorista.DISPONIVEL;
        };
    }

    /** utilizacao e faixa ficam nulas quando nao ha dia disponivel. */
    public record Classificacao(BigDecimal utilizacao, FaixaUtilizacao faixa, SituacaoMotorista situacao) {
    }
}
