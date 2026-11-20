package br.com.newe.ms_operacoes.service.importacao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import br.com.newe.ms_operacoes.client.FrotaClient;

/**
 * Preenche viagem_destinos a partir do texto de viagens.destino, que o manifesto
 * traz como paradas separadas por virgula e sem UF ("Varginha, Divinópolis,
 * Belo Horizonte"). Cada parada vira uma linha, com ordem_parada = posicao no texto.
 *
 * Quem decide a cidade (e desempata homonimos) e o ms-frota, dono de cidades.
 * Parada sem cidade (nome inexistente, como "MF - MCP", ou homonimo sem desempate)
 * fica de fora; o texto original continua em viagens.destino.
 */
@Service
public class DestinosViagemService {

    private final JdbcTemplate jdbc;
    private final FrotaClient frotaClient;

    public DestinosViagemService(JdbcTemplate jdbc, FrotaClient frotaClient) {
        this.jdbc = jdbc;
        this.frotaClient = frotaClient;
    }

    /**
     * Toda viagem com destino e sem nenhuma parada gravada: as do arquivo que
     * acabou de entrar e as importadas antes de existir este preenchimento.
     *
     * shortcut: viagem cujo destino nao resolve nenhuma cidade e reprocessada a cada
     * importacao (poucas: so as que tem apenas paradas ambiguas ou inexistentes).
     * Se crescer, marcar a viagem como ja processada.
     *
     * @return paradas gravadas
     */
    public int preencherPendentes() {
        Map<String, List<Long>> viagensPorDestino = jdbc.queryForList("""
                select v.id_viagem, v.destino from viagens v
                where v.destino is not null and trim(v.destino) <> ''
                  and not exists (select 1 from viagem_destinos d where d.id_viagem = v.id_viagem)
                """).stream().collect(Collectors.groupingBy(
                        r -> (String) r.get("destino"),
                        Collectors.mapping(r -> ((Number) r.get("id_viagem")).longValue(), Collectors.toList())));
        if (viagensPorDestino.isEmpty()) {
            return 0;
        }

        // Uma rota por texto distinto: 5 mil viagens viram ~1 mil rotas numa chamada so.
        List<String> destinos = new ArrayList<>(viagensPorDestino.keySet());
        List<List<String>> rotas = destinos.stream().map(DestinosViagemService::paradas).toList();
        List<List<Integer>> cidades = frotaClient.resolverRotas(rotas);

        List<Object[]> linhas = new ArrayList<>();
        for (int i = 0; i < destinos.size(); i++) {
            List<Integer> idsCidade = cidades.get(i);
            for (Long idViagem : viagensPorDestino.get(destinos.get(i))) {
                for (int ordem = 0; ordem < idsCidade.size(); ordem++) {
                    if (idsCidade.get(ordem) != null) {
                        linhas.add(new Object[] {idViagem, idsCidade.get(ordem), ordem + 1});
                    }
                }
            }
        }
        jdbc.batchUpdate("insert into viagem_destinos (id_viagem, id_cidade, ordem_parada) values (?, ?, ?) "
                + "on conflict do nothing", linhas);
        return linhas.size();
    }

    static List<String> paradas(String destino) {
        return Arrays.stream(destino.split(",")).map(String::trim).filter(p -> !p.isEmpty()).toList();
    }
}
