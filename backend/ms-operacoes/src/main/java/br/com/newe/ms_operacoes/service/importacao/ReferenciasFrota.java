package br.com.newe.ms_operacoes.service.importacao;

import java.util.Map;
import java.util.UUID;

/**
 * Ids que o ms-frota devolveu para as chaves naturais do manifesto.
 * Resolvidos antes da transacao de gravacao, para que a chamada HTTP nao
 * segure a conexao do banco aberta.
 */
public record ReferenciasFrota(
        Map<String, UUID> motoristasPorCpf,
        Map<String, UUID> agregadosPorDocumento,
        Map<String, UUID> veiculosPorPlaca
) {

    public UUID motorista(String cpf) {
        return cpf == null ? null : motoristasPorCpf.get(cpf);
    }

    public UUID agregado(String documento) {
        return documento == null ? null : agregadosPorDocumento.get(documento);
    }

    public UUID veiculo(String placa) {
        return placa == null ? null : veiculosPorPlaca.get(ResolvedorFrota.normalizarPlaca(placa));
    }
}
