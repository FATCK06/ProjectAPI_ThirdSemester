package br.com.newe.ms_operacoes.service.importacao;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import br.com.newe.ms_operacoes.client.FrotaClient;
import br.com.newe.ms_operacoes.dto.AgregadoLoteItem;
import br.com.newe.ms_operacoes.dto.MotoristaLoteItem;
import br.com.newe.ms_operacoes.dto.VeiculoLoteItem;

/**
 * Traduz as chaves naturais do manifesto (CPF, documento do agregado, placa) nos
 * ids do ms-frota, criando o que ainda nao existir la.
 *
 * Deduplica antes de enviar: o arquivo de abr-jun tem 1871 linhas, mas so 150
 * motoristas e 193 agregados distintos. Sao 3 requisicoes no total.
 *
 * Fica fora de @Transactional de proposito - chamada HTTP dentro de transacao
 * seguraria a conexao do banco durante a ida e volta pela rede.
 */
@Service
public class ResolvedorFrota {

    private final FrotaClient frotaClient;

    public ResolvedorFrota(FrotaClient frotaClient) {
        this.frotaClient = frotaClient;
    }

    public ReferenciasFrota resolver(List<LinhaManifesto> linhas) {
        return new ReferenciasFrota(
                recusaComo("motoristas", () -> frotaClient.upsertMotoristas(motoristasDistintos(linhas))),
                recusaComo("agregados", () -> frotaClient.upsertAgregados(agregadosDistintos(linhas))),
                recusaComo("veiculos", () -> frotaClient.upsertVeiculos(veiculosDistintos(linhas)))
        );
    }

    /**
     * 400 do ms-frota e dado do arquivo que a conferencia deixou passar, nao falha
     * do servidor: vira CadastroRecusadoException com o motivo, para o usuario
     * saber o que corrigir. Qualquer outro erro (401, 5xx, timeout) segue como esta.
     */
    private static <T> T recusaComo(String cadastro, Supplier<T> chamada) {
        try {
            return chamada.get();
        } catch (HttpClientErrorException.BadRequest e) {
            throw new CadastroRecusadoException(cadastro, motivos(e), e);
        }
    }

    /** Os motivos de "campos" no corpo de erro do ms-frota; null se o corpo veio em outro formato. */
    static String motivos(HttpClientErrorException e) {
        try {
            Map<?, ?> corpo = e.getResponseBodyAs(Map.class);
            if (corpo != null && corpo.get("campos") instanceof Map<?, ?> campos && !campos.isEmpty()) {
                return campos.values().stream()
                        .map(String::valueOf)
                        .distinct()
                        .collect(Collectors.joining("; "));
            }
        } catch (RuntimeException ignorada) {
            // Sem como ler o corpo: a mensagem sai sem o motivo, e o detalhe fica no log.
        }
        return null;
    }

    /** O ms-frota recusou um dos lotes. Nenhuma viagem foi gravada ainda. */
    public static class CadastroRecusadoException extends RuntimeException {
        public CadastroRecusadoException(String cadastro, String motivo, Throwable causa) {
            super("O cadastro de " + cadastro + " da frota recusou dados deste arquivo"
                    + (motivo == null ? "" : " (" + motivo + ")")
                    + ". Corrija o arquivo e envie novamente; nenhuma viagem foi gravada.", causa);
        }
    }

    /** Placa e chave: precisa casar com a normalizacao que o ms-frota aplica. */
    static String normalizarPlaca(String placa) {
        if (placa == null) {
            return null;
        }
        String texto = placa.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        return texto.isEmpty() ? null : texto;
    }

    private List<MotoristaLoteItem> motoristasDistintos(List<LinhaManifesto> linhas) {
        Map<String, MotoristaLoteItem> porCpf = new LinkedHashMap<>();
        for (LinhaManifesto linha : linhas) {
            if (linha.cpf() == null) {
                continue;
            }
            porCpf.putIfAbsent(linha.cpf(), new MotoristaLoteItem(
                    linha.cpf(),
                    linha.nome(),
                    linha.pis(),
                    linha.dataNascimento(),
                    linha.endereco(),
                    linha.cep(),
                    linha.bairro(),
                    linha.cidade(),
                    linha.estado()
            ));
        }
        return new ArrayList<>(porCpf.values());
    }

    private List<AgregadoLoteItem> agregadosDistintos(List<LinhaManifesto> linhas) {
        Map<String, AgregadoLoteItem> porDocumento = new LinkedHashMap<>();
        for (LinhaManifesto linha : linhas) {
            // 84 linhas do arquivo real nao informam agregado; a coluna e opcional.
            if (linha.agregadoDocumento() == null) {
                continue;
            }
            porDocumento.putIfAbsent(linha.agregadoDocumento(), new AgregadoLoteItem(
                    linha.agregadoDocumento(),
                    linha.agregadoNome(),
                    linha.agregadoPis(),
                    linha.regimeFiscal()
            ));
        }
        return new ArrayList<>(porDocumento.values());
    }

    private List<VeiculoLoteItem> veiculosDistintos(List<LinhaManifesto> linhas) {
        Map<String, VeiculoLoteItem> porPlaca = new LinkedHashMap<>();
        for (LinhaManifesto linha : linhas) {
            String placa = normalizarPlaca(linha.veiculo());
            if (placa == null) {
                continue;
            }
            porPlaca.putIfAbsent(placa, new VeiculoLoteItem(
                    placa,
                    linha.capacidadeVeiculo(),
                    linha.agregadoNome()
            ));
        }
        return new ArrayList<>(porPlaca.values());
    }
}
