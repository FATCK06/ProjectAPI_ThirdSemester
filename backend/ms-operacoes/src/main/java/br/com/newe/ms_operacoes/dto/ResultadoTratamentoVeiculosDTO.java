package br.com.newe.ms_operacoes.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Resultado do cruzamento entre manifestos e planilha de veiculos, sobre o estado
 * atual do banco. Considera so os veiculos que aparecem como o veiculo de alguma
 * viagem: semi-reboque e veiculo parado nao entram.
 *
 * @param veiculosEmManifestos  veiculos distintos usados em viagens
 * @param veiculosComCategoria  dos acima, os que tem categoria
 * @param tiposSemCategoria     tipos cadastrados sem categoria ("Não classificado")
 * @param manifestosSemVeiculo  viagens cujo veiculo nao veio na planilha ("Não identificado")
 */
public record ResultadoTratamentoVeiculosDTO(
        int veiculosEmManifestos,
        int veiculosComCategoria,
        List<TipoSemCategoria> tiposSemCategoria,
        List<ManifestoSemVeiculo> manifestosSemVeiculo) {

    public record TipoSemCategoria(String tipo, int quantidade, List<String> placas) {
    }

    public record ManifestoSemVeiculo(Integer manifesto, String placa, LocalDate data) {
    }
}
