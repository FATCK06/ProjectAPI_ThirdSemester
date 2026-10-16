package br.com.newe.ms_operacoes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.com.newe.ms_operacoes.models.Importacao;
import br.com.newe.ms_operacoes.models.enums.StatusImportacaoEnum;
import br.com.newe.ms_operacoes.repository.ImportacaoRepository;
import br.com.newe.ms_operacoes.service.ImportacaoService.ExecucaoRecusadaException;

@SpringBootTest
@Transactional
class ImportacaoServiceTest {

    @Autowired
    private ImportacaoService service;

    @Autowired
    private ImportacaoRepository repository;

    @Test
    void reservaImportacaoValidada() {
        Long id = criar("manifestos_jul-ago.csv", StatusImportacaoEnum.VALIDADO, null);

        service.reservarProcessamento(id);

        Importacao reservada = repository.findById(id).orElseThrow();
        assertThat(reservada.getStatus()).isEqualTo(StatusImportacaoEnum.EM_PROCESSAMENTO);
        assertThat(reservada.getProcessamentoIniciadoEm()).isNotNull();
    }

    // O caso de 15/10: o mesmo arquivo executado de novo enquanto o primeiro gravava.
    @Test
    void recusaEnquantoOutraGrava() {
        criar("manifestos_jul-ago.csv", StatusImportacaoEnum.EM_PROCESSAMENTO, LocalDateTime.now());
        Long segunda = criar("manifestos_jul-ago.csv", StatusImportacaoEnum.VALIDADO, null);

        assertThatThrownBy(() -> service.reservarProcessamento(segunda))
                .isInstanceOf(ExecucaoRecusadaException.class)
                .hasMessageContaining("Ja existe uma importacao sendo gravada")
                .hasMessageContaining("manifestos_jul-ago.csv");

        assertThat(repository.findById(segunda).orElseThrow().getStatus())
                .isEqualTo(StatusImportacaoEnum.VALIDADO);
    }

    @Test
    void recusaExecutarDuasVezesAMesma() {
        Long id = criar("manifestos_jul-ago.csv", StatusImportacaoEnum.EM_PROCESSAMENTO, LocalDateTime.now());

        assertThatThrownBy(() -> service.reservarProcessamento(id))
                .isInstanceOf(ExecucaoRecusadaException.class)
                .hasMessageContaining("ja esta sendo gravada");
    }

    // A aplicacao caiu no meio: sem isto, ninguem mais importaria.
    @Test
    void liberaReservaAbandonada() {
        Long abandonada = criar("antigo.csv", StatusImportacaoEnum.EM_PROCESSAMENTO,
                LocalDateTime.now().minusMinutes(31));
        Long nova = criar("manifestos_jul-ago.csv", StatusImportacaoEnum.VALIDADO, null);

        service.reservarProcessamento(nova);

        assertThat(repository.findById(abandonada).orElseThrow().getStatus())
                .isEqualTo(StatusImportacaoEnum.ERRO);
        assertThat(repository.findById(nova).orElseThrow().getStatus())
                .isEqualTo(StatusImportacaoEnum.EM_PROCESSAMENTO);
    }

    @Test
    void recusaImportacaoJaConcluida() {
        Long id = criar("manifestos_abr-jun.csv", StatusImportacaoEnum.CONCLUIDO, null);

        assertThatThrownBy(() -> service.reservarProcessamento(id))
                .isInstanceOf(ExecucaoRecusadaException.class)
                .hasMessage("Esta importacao ja foi executada");
    }

    @Test
    void emAndamentoIgnoraReservaAbandonada() {
        assertThat(service.emAndamento()).isEmpty();

        criar("antigo.csv", StatusImportacaoEnum.EM_PROCESSAMENTO, LocalDateTime.now().minusMinutes(31));
        assertThat(service.emAndamento()).isEmpty();
    }

    @Test
    void emAndamentoTrazAQueEstaGravando() {
        Long id = criar("manifestos_jul-ago.csv", StatusImportacaoEnum.EM_PROCESSAMENTO, LocalDateTime.now());

        assertThat(service.emAndamento()).hasValueSatisfying(i -> {
            assertThat(i.getId()).isEqualTo(id);
            assertThat(i.getArquivoNome()).isEqualTo("manifestos_jul-ago.csv");
        });
    }

    // Validar de novo no meio da gravacao nao pode soltar a reserva.
    @Test
    void revalidarNaoTiraDeEmProcessamento() {
        Long id = criar("manifestos_jul-ago.csv", StatusImportacaoEnum.EM_PROCESSAMENTO, LocalDateTime.now());

        service.marcarValidado(id, 1323, 1323, 0);

        assertThat(repository.findById(id).orElseThrow().getStatus())
                .isEqualTo(StatusImportacaoEnum.EM_PROCESSAMENTO);
    }

    private Long criar(String arquivo, StatusImportacaoEnum status, LocalDateTime iniciadoEm) {
        Importacao i = new Importacao();
        i.setArquivoNome(arquivo);
        i.setUsuarioResponsavel("teste@newe.com");
        i.setDataImportacao(LocalDateTime.now());
        i.setStatus(status);
        i.setProcessamentoIniciadoEm(iniciadoEm);
        return repository.saveAndFlush(i).getId();
    }
}
