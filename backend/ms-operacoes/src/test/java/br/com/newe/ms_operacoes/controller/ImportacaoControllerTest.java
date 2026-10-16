package br.com.newe.ms_operacoes.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import br.com.newe.ms_operacoes.models.Importacao;
import br.com.newe.ms_operacoes.models.enums.StatusImportacaoEnum;
import br.com.newe.ms_operacoes.repository.ImportacaoRepository;
import br.com.newe.ms_operacoes.service.importacao.ManifestoCsvConfig;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ImportacaoControllerTest {

    private static final String BASE = "/api/importacao/arquivos";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ImportacaoRepository repository;

    @Test
    void semImportacaoGravandoDevolve204() throws Exception {
        mvc.perform(get(BASE + "/em-andamento")).andExpect(status().isNoContent());
    }

    @Test
    void devolveAImportacaoGravando() throws Exception {
        // Relativo a agora: um horario fixo passaria dos 30 min e contaria como abandonada.
        LocalDateTime inicio = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        Long id = criar(StatusImportacaoEnum.EM_PROCESSAMENTO, inicio);

        mvc.perform(get(BASE + "/em-andamento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.arquivoNome").value("manifestos_jul-ago.csv"))
                .andExpect(jsonPath("$.usuarioResponsavel").value("teste@newe.com"))
                .andExpect(jsonPath("$.processamentoIniciadoEm").value(inicio.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
    }

    // Recusa antes do parse e sem tocar no status da segunda.
    @Test
    void executarComOutraGravandoDevolve409() throws Exception {
        criar(StatusImportacaoEnum.EM_PROCESSAMENTO, LocalDateTime.now());
        Long segunda = criar(StatusImportacaoEnum.VALIDADO, null);

        mvc.perform(post(BASE + "/" + segunda + "/executar"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro", Matchers.containsString("Ja existe uma importacao sendo gravada")));
    }

    private Long criar(StatusImportacaoEnum status, LocalDateTime iniciadoEm) {
        Importacao i = new Importacao();
        i.setArquivoNome("manifestos_jul-ago.csv");
        i.setUsuarioResponsavel("teste@newe.com");
        i.setDataImportacao(LocalDateTime.now());
        i.setStatus(status);
        i.setProcessamentoIniciadoEm(iniciadoEm);
        // O executar faz o parse antes de reservar: precisa de um arquivo valido.
        i.setArquivoConteudo(CSV_UMA_LINHA);
        return repository.saveAndFlush(i).getId();
    }

    // Mesmo formato minimo do CsvManifestoParserTest: so as colunas obrigatorias.
    private static final byte[] CSV_UMA_LINHA = ("Manifesto;Data;CPF;Veículo;Km saída;Serviços;Kg Real\n"
            + "123;01/07/2026;12345678901;ABC1D23;100;5;10,00\n").getBytes(ManifestoCsvConfig.CHARSET);
}
