package br.com.newe.ms_operacoes.service.importacao;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

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
                frotaClient.upsertMotoristas(motoristasDistintos(linhas)),
                frotaClient.upsertAgregados(agregadosDistintos(linhas)),
                frotaClient.upsertVeiculos(veiculosDistintos(linhas))
        );
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
