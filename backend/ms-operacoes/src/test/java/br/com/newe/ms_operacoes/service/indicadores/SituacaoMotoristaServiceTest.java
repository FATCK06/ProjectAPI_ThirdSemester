package br.com.newe.ms_operacoes.service.indicadores;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.newe.ms_operacoes.client.FrotaClient;
import br.com.newe.ms_operacoes.dto.MotoristaResumo;
import br.com.newe.ms_operacoes.dto.SituacaoMotoristaDTO;
import br.com.newe.ms_operacoes.models.ControleDisponibilidade;
import br.com.newe.ms_operacoes.models.enums.FaixaUtilizacao;
import br.com.newe.ms_operacoes.models.enums.SituacaoMotorista;
import br.com.newe.ms_operacoes.repository.ControleDisponibilidadeRepository;
import br.com.newe.ms_operacoes.repository.ViagemRepository;
import br.com.newe.ms_operacoes.repository.ViagemRepository.SomaMotoristaView;

class SituacaoMotoristaServiceTest {

    private static final String MES = "2026-09"; // 30 dias

    private final UUID motorista = UUID.randomUUID();

    private ViagemRepository viagemRepository;
    private ControleDisponibilidadeRepository controleRepository;
    private FrotaClient frotaClient;
    private SituacaoMotoristaService service;

    // Faz o papel da tabela: o mock le e grava nesta lista.
    private final List<ControleDisponibilidade> tabela = new ArrayList<>();

    @BeforeEach
    void setUp() {
        viagemRepository = mock(ViagemRepository.class);
        controleRepository = mock(ControleDisponibilidadeRepository.class);
        frotaClient = mock(FrotaClient.class);

        when(controleRepository.findByMesReferencia(anyString())).thenAnswer(inv -> tabela.stream()
                .filter(c -> c.getMesReferencia().equals(inv.getArgument(0)))
                .toList());
        when(controleRepository.saveAll(any())).thenAnswer(inv -> {
            Iterable<ControleDisponibilidade> itens = inv.getArgument(0);
            for (ControleDisponibilidade c : itens) {
                if (c.getId() == null) {
                    c.setId(UUID.randomUUID());
                    tabela.add(c);
                }
            }
            return itens;
        });

        service = new SituacaoMotoristaService(viagemRepository, controleRepository, frotaClient,
                new BigDecimal("40"), new BigDecimal("70"));
    }

    @Test
    void insereNaPrimeiraVez() {
        when(viagemRepository.somasPorMotorista(MES)).thenReturn(List.of(soma(motorista, 9)));

        service.recalcularMeses(List.of(MES));

        assertThat(tabela).hasSize(1);
        ControleDisponibilidade c = tabela.get(0);
        assertThat(c.getIdMotorista()).isEqualTo(motorista);
        assertThat(c.getMesReferencia()).isEqualTo(MES);
        assertThat(c.getDiasDisponiveis()).isEqualTo(30);
        assertThat(c.getDiasOperacao()).isEqualTo(9);
    }

    // Segunda importacao do mesmo mes: outro arquivo trouxe mais dias do motorista.
    @Test
    void atualizaNaSegundaSemDuplicar() {
        when(viagemRepository.somasPorMotorista(MES))
                .thenReturn(List.of(soma(motorista, 9)))
                .thenReturn(List.of(soma(motorista, 24)));

        service.recalcularMeses(List.of(MES));
        UUID idOriginal = tabela.get(0).getId();
        service.recalcularMeses(List.of(MES));

        assertThat(tabela).hasSize(1);
        ControleDisponibilidade c = tabela.get(0);
        assertThat(c.getId()).isEqualTo(idOriginal);
        assertThat(c.getDiasOperacao()).isEqualTo(24);
    }

    // A classificacao sai dos dias gravados: 24 de 30 = 80% -> alta.
    @Test
    void listarClassificaPelosDiasGravados() {
        tabela.add(controle(motorista, 24, 30));
        when(frotaClient.buscarMotoristas(anyCollection()))
                .thenReturn(List.of(new MotoristaResumo(motorista, "JOAO", null)));

        List<SituacaoMotoristaDTO> lista = service.listar(MES, null);

        assertThat(lista).singleElement().satisfies(d -> {
            assertThat(d.nome()).isEqualTo("JOAO");
            assertThat(d.utilizacao()).isEqualByComparingTo("80.00");
            assertThat(d.faixaUtilizacao()).isEqualTo(FaixaUtilizacao.ALTA);
            assertThat(d.situacao()).isEqualTo(SituacaoMotorista.INDISPONIVEL);
            assertThat(d.aviso()).isNull();
        });
    }

    @Test
    void listaDisponiveisPrimeiroDepoisMenorUtilizacaoDepoisNome() {
        UUID alta = UUID.randomUUID();
        UUID baixaB = UUID.randomUUID();
        UUID baixaA = UUID.randomUUID();
        UUID intermediaria = UUID.randomUUID();
        tabela.addAll(List.of(
                controle(alta, 27, 30),
                controle(intermediaria, 15, 30),
                controle(baixaB, 3, 30),
                controle(baixaA, 3, 30)));
        when(frotaClient.buscarMotoristas(anyCollection())).thenReturn(List.of(
                new MotoristaResumo(alta, "ANA", null),
                new MotoristaResumo(intermediaria, "BETO", null),
                new MotoristaResumo(baixaB, "BRUNO", null),
                new MotoristaResumo(baixaA, "ALICE", null)));

        List<SituacaoMotoristaDTO> lista = service.listar(MES, null);

        assertThat(lista).extracting(SituacaoMotoristaDTO::nome)
                .containsExactly("ALICE", "BRUNO", "BETO", "ANA");
    }

    @Test
    void filtroDeSituacaoAplicadoDepoisDeClassificar() {
        UUID disponivel = UUID.randomUUID();
        tabela.addAll(List.of(controle(disponivel, 3, 30), controle(motorista, 27, 30)));
        when(frotaClient.buscarMotoristas(anyCollection())).thenReturn(List.of());

        List<SituacaoMotoristaDTO> lista = service.listar(MES, SituacaoMotorista.INDISPONIVEL);

        assertThat(lista).extracting(SituacaoMotoristaDTO::motoristaId).containsExactly(motorista);
    }

    @Test
    void semDiasDisponiveisTrazAviso() {
        tabela.add(controle(motorista, 5, 0));
        when(frotaClient.buscarMotoristas(anyCollection())).thenReturn(List.of());

        List<SituacaoMotoristaDTO> lista = service.listar(MES, SituacaoMotorista.SEM_DIAS_DISPONIVEIS);

        assertThat(lista).singleElement().satisfies(d -> {
            assertThat(d.utilizacao()).isNull();
            assertThat(d.faixaUtilizacao()).isNull();
            assertThat(d.aviso()).isEqualTo("Motorista sem dias disponíveis registrados no período.");
        });
    }

    @Test
    void mesSemLinhasNaoChamaOMsFrota() {
        assertThat(service.listar(MES, null)).isEmpty();
        verifyNoInteractions(frotaClient);
    }

    private static ControleDisponibilidade controle(UUID id, int diasOperacao, int diasDisponiveis) {
        ControleDisponibilidade c = new ControleDisponibilidade();
        c.setIdMotorista(id);
        c.setMesReferencia(MES);
        c.setDiasOperacao(diasOperacao);
        c.setDiasDisponiveis(diasDisponiveis);
        return c;
    }

    private static SomaMotoristaView soma(UUID id, long diasOperacao) {
        return new SomaMotoristaView() {
            public UUID getMotoristaId() { return id; }
            public Long getNumeroViagens() { return diasOperacao; }
            public Long getDiasOperacao() { return diasOperacao; }
            public BigDecimal getValorFrete() { return null; }
            public BigDecimal getCustoTotal() { return null; }
        };
    }
}
