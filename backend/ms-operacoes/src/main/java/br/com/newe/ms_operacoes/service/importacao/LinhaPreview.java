package br.com.newe.ms_operacoes.service.importacao;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Recorte de uma linha para a tela de revisao. Nao e persistido. */
public record LinhaPreview(
        int numeroLinha,
        Integer manifesto,
        LocalDate data,
        String mesReferencia,
        String motorista,
        String cpf,
        String agregado,
        String veiculo,
        String destino,
        BigDecimal valorFrete
) {

    public static LinhaPreview de(LinhaManifesto l) {
        return new LinhaPreview(
                l.numeroLinha(), l.idManifesto(), l.data(), l.mesReferencia(),
                l.nome(), l.cpf(), l.agregadoNome(), l.veiculo(), l.destino(), l.valorFrete());
    }
}
