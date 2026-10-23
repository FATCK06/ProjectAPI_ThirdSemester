package br.com.newe.ms_operacoes.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.com.newe.ms_operacoes.models.Viagem;
import br.com.newe.ms_operacoes.repository.ViagemRepository.SomaMotoristaMesView;
import br.com.newe.ms_operacoes.repository.ViagemRepository.SomaMotoristaView;
import br.com.newe.ms_operacoes.repository.ViagemRepository.SomaVeiculoView;

@SpringBootTest
@Transactional
class ViagemRepositoryTest {

    private static final String MES = "2026-04";

    @Autowired
    private ViagemRepository repository;

    private final UUID motorista = UUID.randomUUID();
    private final UUID veiculo = UUID.randomUUID();

    // Um manifesto real e os tres casos que ficam fora das somas financeiras.
    @Test
    void somaSoReceitaECustoDasViagensApuraveis() {
        gravar(16882, 1, "1226.21", "430.75", UUID.randomUUID()); // apuravel
        gravar(16928, 2, "15154.53", "0", null);                  // frota propria
        gravar(16921, 3, "0", "1502.45", UUID.randomUUID());      // sem CT-e vinculado
        gravar(17206, 3, "800.00", null, UUID.randomUUID());      // agregado sem contrato

        List<SomaMotoristaView> somas = repository.somasPorMotorista(MES);

        assertThat(somas).hasSize(1);
        SomaMotoristaView soma = somas.get(0);
        assertThat(soma.getNumeroViagens()).isEqualTo(4);
        assertThat(soma.getDiasOperacao()).isEqualTo(3);
        assertThat(soma.getViagensApuradas()).isEqualTo(1);
        assertThat(soma.getValorFrete()).isEqualByComparingTo("1226.21");
        assertThat(soma.getCustoTotal()).isEqualByComparingTo("430.75");
    }

    @Test
    void porVeiculoUsaAMesmaRegra() {
        gravar(16882, 1, "1226.21", "430.75", UUID.randomUUID());
        gravar(16928, 2, "15154.53", "0", null);

        List<SomaVeiculoView> somas = repository.somasPorVeiculo(MES);

        assertThat(somas).hasSize(1);
        assertThat(somas.get(0).getNumeroViagens()).isEqualTo(2);
        assertThat(somas.get(0).getValorFrete()).isEqualByComparingTo("1226.21");
        assertThat(somas.get(0).getCustoTotal()).isEqualByComparingTo("430.75");
    }

    @Test
    void mesSemViagemApuravelDevolveSomasNulas() {
        gravar(16928, 2, "15154.53", "0", null);

        SomaMotoristaView soma = repository.somasPorMotorista(MES).get(0);

        assertThat(soma.getViagensApuradas()).isZero();
        assertThat(soma.getValorFrete()).isNull();
        assertThat(soma.getCustoTotal()).isNull();
    }

    @Test
    void somasNosMesesSeparamPorMesEIgnoramMesForaDaLista() {
        gravar(16882, 1, "1226.21", "430.75", UUID.randomUUID());
        gravar(16883, 2, "500.00", "200.00", UUID.randomUUID());
        gravar(17001, 5, 5, "300.00", "100.00", UUID.randomUUID());
        gravar(17500, 6, 3, "999.00", "1.00", UUID.randomUUID()); // fora da lista

        List<SomaMotoristaMesView> somas = repository.somasPorMotoristaNosMeses(List.of("2026-04", "2026-05"));

        assertThat(somas).extracting(SomaMotoristaMesView::getMesReferencia)
                .containsExactlyInAnyOrder("2026-04", "2026-05");
        SomaMotoristaMesView abril = somas.stream().filter(s -> s.getMesReferencia().equals("2026-04")).findFirst()
                .orElseThrow();
        assertThat(abril.getNumeroViagens()).isEqualTo(2);
        assertThat(abril.getDiasOperacao()).isEqualTo(2);
        assertThat(abril.getValorFrete()).isEqualByComparingTo("1726.21");
        assertThat(abril.getCustoTotal()).isEqualByComparingTo("630.75");
    }

    private void gravar(int manifesto, int dia, String valorFretes, String valeFrete, UUID agregado) {
        gravar(manifesto, 4, dia, valorFretes, valeFrete, agregado);
    }

    private void gravar(int manifesto, int mes, int dia, String valorFretes, String valeFrete, UUID agregado) {
        Viagem v = new Viagem();
        v.setIdManifesto(manifesto);
        v.setIdMotorista(motorista);
        v.setIdVeiculo(veiculo);
        v.setIdAgregado(agregado);
        v.setDataViagem(LocalDate.of(2026, mes, dia));
        v.setMesReferencia(String.format("2026-%02d", mes));
        v.setValorFretes(new BigDecimal(valorFretes));
        v.setValeFrete(valeFrete == null ? null : new BigDecimal(valeFrete));
        repository.save(v);
    }
}
