package br.com.newe.ms_frota.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import br.com.newe.ms_frota.models.Cidade;
import br.com.newe.ms_frota.repository.CidadeRepository;

/**
 * Acha a cidade a partir do texto que vem nos CSVs ("Jacarei", "JACAREÍ", "São José
 * dos Campos"). Só procura, nunca cria: a tabela vem completa do IBGE (V12), então
 * o que não bate é erro de digitação, não cidade nova.
 */
@Component
public class ResolvedorCidade {

    private final CidadeRepository repository;

    public ResolvedorCidade(CidadeRepository repository) {
        this.repository = repository;
    }

    /** Cidades das UFs pedidas, indexadas por {@link #chave}. Uma consulta para o lote inteiro. */
    public Map<String, Cidade> carregar(Collection<String> ufs) {
        Set<String> siglas = ufs.stream()
                .map(TextoNormalizado::de)
                .filter(uf -> !uf.isEmpty())
                .collect(Collectors.toSet());
        Map<String, Cidade> porChave = new HashMap<>();
        if (siglas.isEmpty()) {
            return porChave;
        }
        for (Cidade cidade : repository.buscarPorUfs(siglas)) {
            porChave.put(chave(cidade.getNomeCidade(), cidade.getEstado().getSigla()), cidade);
        }
        return porChave;
    }

    /** Nome antigo ou grafia alternativa que aparece nos manifestos -> nome do IBGE. */
    private static final Map<String, String> APELIDOS = Map.of("PARATI", "PARATY");

    private static final Set<String> CAPITAIS = Set.of(
            "RIO BRANCO|AC", "MACEIO|AL", "MACAPA|AP", "MANAUS|AM", "SALVADOR|BA", "FORTALEZA|CE", "BRASILIA|DF",
            "VITORIA|ES", "GOIANIA|GO", "SAO LUIS|MA", "CUIABA|MT", "CAMPO GRANDE|MS", "BELO HORIZONTE|MG",
            "BELEM|PA", "JOAO PESSOA|PB", "CURITIBA|PR", "RECIFE|PE", "TERESINA|PI", "RIO DE JANEIRO|RJ",
            "NATAL|RN", "PORTO ALEGRE|RS", "PORTO VELHO|RO", "BOA VISTA|RR", "FLORIANOPOLIS|SC", "SAO PAULO|SP",
            "ARACAJU|SE", "PALMAS|TO");

    /** A operacao e de Sao Jose dos Campos: entre homonimos, o de SP e o mais provavel. */
    private static final String UF_DA_OPERACAO = "SP";

    /**
     * Destinos de viagem: cada rota e a lista de paradas, sem UF ("Varginha,
     * Divinópolis, Belo Horizonte"). Devolve o id da cidade de cada parada, na mesma
     * posicao, ou null quando o nome nao existe ou continua ambiguo.
     */
    public List<List<Integer>> resolverRotas(List<List<String>> rotas) {
        Map<String, List<Cidade>> porNome = repository.buscarTodas().stream()
                .collect(Collectors.groupingBy(c -> TextoNormalizado.de(c.getNomeCidade())));

        List<List<Integer>> resultado = new ArrayList<>();
        for (List<String> rota : rotas) {
            List<List<Cidade>> candidatos = rota.stream()
                    .map(nome -> {
                        String chave = TextoNormalizado.de(nome);
                        return porNome.getOrDefault(APELIDOS.getOrDefault(chave, chave), List.of());
                    })
                    .toList();
            Set<String> ufsDaRota = candidatos.stream()
                    .filter(c -> c.size() == 1)
                    .map(c -> c.get(0).getEstado().getSigla())
                    .collect(Collectors.toSet());
            resultado.add(candidatos.stream()
                    .map(c -> escolher(c, ufsDaRota))
                    .map(c -> c == null ? null : c.getIdCidade())
                    .toList());
        }
        return resultado;
    }

    /**
     * Homonimos ("Belém" existe em PA, AL e PB), em ordem: o estado das outras
     * paradas da mesma viagem, a capital, SP. Sem desempate, null: parada errada
     * no banco e pior que parada faltando.
     */
    static Cidade escolher(List<Cidade> candidatos, Set<String> ufsDaRota) {
        if (candidatos.size() <= 1) {
            return candidatos.isEmpty() ? null : candidatos.get(0);
        }
        List<Predicate<Cidade>> regras = List.of(
                c -> ufsDaRota.contains(c.getEstado().getSigla()),
                c -> CAPITAIS.contains(chave(c.getNomeCidade(), c.getEstado().getSigla())),
                c -> UF_DA_OPERACAO.equals(c.getEstado().getSigla()));
        for (Predicate<Cidade> regra : regras) {
            List<Cidade> sobram = candidatos.stream().filter(regra).toList();
            if (sobram.size() == 1) {
                return sobram.get(0);
            }
        }
        return null;
    }

    /** null quando falta nome ou UF: assim nunca casa com nada no mapa. */
    public static String chave(String nome, String uf) {
        String n = TextoNormalizado.de(nome);
        String u = TextoNormalizado.de(uf);
        return n.isEmpty() || u.isEmpty() ? null : n + "|" + u;
    }
}
