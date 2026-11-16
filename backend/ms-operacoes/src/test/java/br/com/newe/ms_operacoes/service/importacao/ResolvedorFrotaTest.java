package br.com.newe.ms_operacoes.service.importacao;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import br.com.newe.ms_operacoes.client.FrotaClient;
import br.com.newe.ms_operacoes.service.importacao.ResolvedorFrota.CadastroRecusadoException;

class ResolvedorFrotaTest {

    private final FrotaClient frotaClient = mock(FrotaClient.class);
    private final ResolvedorFrota resolvedor = new ResolvedorFrota(frotaClient);

    /** 400 como o ms-frota devolve: RespostaErro com "campos". */
    private static HttpClientErrorException badRequest(Object corpoLido) {
        HttpClientErrorException e = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST, "Bad Request", HttpHeaders.EMPTY,
                "{}".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        // O RestClient preenche isto sozinho; aqui simula o corpo ja convertido.
        e.setBodyConvertFunction(tipo -> {
            if (corpoLido instanceof RuntimeException falha) {
                throw falha;
            }
            return corpoLido;
        });
        return e;
    }

    @Test
    void recusaDoMsFrotaViraCadastroRecusadoComOMotivo() {
        when(frotaClient.upsertVeiculos(anyList())).thenThrow(badRequest(Map.of(
                "erro", "Dados inválidos. Verifique os campos informados.",
                "campos", Map.of("itens", "deve ser maior ou igual a zero"))));

        assertThatThrownBy(() -> resolvedor.resolver(List.of()))
                .isInstanceOf(CadastroRecusadoException.class)
                .hasMessageContaining("cadastro de veiculos")
                .hasMessageContaining("(deve ser maior ou igual a zero)")
                .hasMessageContaining("nenhuma viagem foi gravada");
    }

    @Test
    void corpoIlegivelAindaViraCadastroRecusadoSemOMotivo() {
        when(frotaClient.upsertMotoristas(anyList()))
                .thenThrow(badRequest(new IllegalStateException("corpo nao e JSON")));

        assertThatThrownBy(() -> resolvedor.resolver(List.of()))
                .isInstanceOf(CadastroRecusadoException.class)
                .hasMessageContaining("cadastro de motoristas")
                .hasMessageNotContaining("(");
    }

    @Test
    void erroQueNaoE400SegueComoVeio() {
        when(frotaClient.upsertAgregados(anyList()))
                .thenThrow(HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Erro",
                        HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8));

        assertThatThrownBy(() -> resolvedor.resolver(List.of()))
                .isInstanceOf(HttpServerErrorException.class);
    }
}
