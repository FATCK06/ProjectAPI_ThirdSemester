package br.com.newe.ms_frota.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.newe.ms_frota.dto.ProblemaImportacao;
import br.com.newe.ms_frota.dto.ResultadoImportacaoVeiculos;
import br.com.newe.ms_frota.dto.VeiculoImportacaoItem;
import br.com.newe.ms_frota.models.Agregado;
import br.com.newe.ms_frota.models.Cidade;
import br.com.newe.ms_frota.models.Motorista;
import br.com.newe.ms_frota.models.Veiculo;
import br.com.newe.ms_frota.repository.AgregadoRepository;
import br.com.newe.ms_frota.repository.MotoristaRepository;
import br.com.newe.ms_frota.repository.VeiculoRepository;

/**
 * Parte da importacao de veiculos que depende do banco. O ms-operacoes ja
 * conferiu formato e duplicados dentro do arquivo.
 *
 * Bloqueia: tipo ou cidade que nao existem, Renavam/chassi que ja pertencem a
 * outra placa, placa cadastrada com outro Renavam. So avisa: agregado e motorista
 * nao encontrados - eles so existem depois que um manifesto os traz com documento,
 * entao a falta e cadastro pendente, nao dado errado.
 */
@Service
public class ImportacaoVeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final AgregadoRepository agregadoRepository;
    private final MotoristaRepository motoristaRepository;
    private final ResolvedorCidade resolvedorCidade;

    public ImportacaoVeiculoService(
            VeiculoRepository veiculoRepository,
            AgregadoRepository agregadoRepository,
            MotoristaRepository motoristaRepository,
            ResolvedorCidade resolvedorCidade) {
        this.veiculoRepository = veiculoRepository;
        this.agregadoRepository = agregadoRepository;
        this.motoristaRepository = motoristaRepository;
        this.resolvedorCidade = resolvedorCidade;
    }

    @Transactional(readOnly = true)
    public List<ProblemaImportacao> conferir(List<VeiculoImportacaoItem> itens) {
        return new Conferencia(itens).problemas;
    }

    /**
     * Tudo ou nada, como o manifesto: confere de novo (o banco pode ter mudado desde
     * a validacao) e, com qualquer problema que bloqueia, nao grava nenhuma linha.
     * Placa existente e atualizada so nos campos que o arquivo traz preenchidos.
     */
    @Transactional
    public ResultadoImportacaoVeiculos gravar(List<VeiculoImportacaoItem> itens) {
        Conferencia c = new Conferencia(itens);
        if (c.problemas.stream().anyMatch(ProblemaImportacao::bloqueia)) {
            return new ResultadoImportacaoVeiculos(c.problemas, 0, 0);
        }

        int criados = 0;
        List<Veiculo> salvar = new ArrayList<>();
        for (VeiculoImportacaoItem item : itens) {
            Veiculo veiculo = c.porPlaca.get(item.placa());
            if (veiculo == null) {
                veiculo = new Veiculo();
                veiculo.setPlaca(item.placa());
                veiculo.setStatusVeiculo(Veiculo.STATUS_ATIVO);
                criados++;
            }
            aplicar(veiculo, item, c.resolvidos.get(item.numeroLinha()));
            salvar.add(veiculo);
        }
        veiculoRepository.saveAll(salvar);

        return new ResultadoImportacaoVeiculos(c.problemas, criados, itens.size() - criados);
    }

    private static void aplicar(Veiculo v, VeiculoImportacaoItem i, Resolvido r) {
        se(r.idTipo(), v::setIdTipo);
        se(r.idCidade(), v::setIdCidadeLicenciamento);
        se(r.idAgregado(), v::setIdAgregado);
        se(r.idMotorista(), v::setIdMotoristaPadrao);
        se(i.marca(), v::setMarca);
        se(i.modelo(), v::setModelo);
        se(i.cor(), v::setCor);
        se(i.combustivel(), v::setCombustivel);
        se(i.numeroFrota(), v::setNumeroFrota);
        se(i.anoFabricacao(), v::setAnoFabricacao);
        se(i.anoModelo(), v::setAnoModelo);
        se(i.renavam(), v::setRenavam);
        se(i.chassi(), v::setChassi);
        se(i.tipoCarroceria(), v::setTipoCarroceria);
        se(i.tipoProprietario(), v::setTipoProprietario);
        se(i.proprietario(), v::setNomeProprietario);
        se(i.agregado(), v::setNomeAgregado);
        se(i.tabelaAgregado(), v::setTabelaAgregado);
        se(i.dataCompra(), v::setDataCompra);
        se(i.valorCompra(), v::setValorCompra);
        se(i.dataVenda(), v::setDataVenda);
        se(i.valorVenda(), v::setValorVenda);
        se(i.pbt(), v::setPbt);
        se(i.tara(), v::setTara);
        se(i.capacidade(), v::setCapacidade);
        se(i.capacidadeLotacao(), v::setCapacidadeLotacao);
        se(i.numeroAutorizacao(), v::setNumeroAutorizacao);
        se(i.validadeGuarda(), v::setValidadeGuarda);
        se(i.status(), v::setStatusVeiculo);
        se(i.loteFechamentoCiot(), v::setLoteFechamentoCiot);
    }

    /** Vazio no arquivo nao apaga o que ja esta no banco. */
    private static <T> void se(T valor, Consumer<T> setter) {
        if (valor != null) {
            setter.accept(valor);
        }
    }

    private record Resolvido(Integer idTipo, Integer idCidade, UUID idAgregado, UUID idMotorista) {
    }

    /** Carrega o que o lote precisa do banco numa leva so e confere linha a linha. */
    private final class Conferencia {

        final List<ProblemaImportacao> problemas = new ArrayList<>();
        final Map<Integer, Resolvido> resolvidos = new HashMap<>();
        final Map<String, Veiculo> porPlaca;
        private final Map<String, Veiculo> porRenavam;
        private final Map<String, Veiculo> porChassi;
        private final Map<String, Integer> tipos;
        private final Map<String, Cidade> cidades;
        private final Map<String, UUID> agregadoPorDocumento;
        private final Map<String, List<UUID>> agregadosPorNome;
        private final Map<String, List<UUID>> motoristasPorNome;

        Conferencia(List<VeiculoImportacaoItem> itens) {
            porPlaca = indexar(buscar(itens, VeiculoImportacaoItem::placa, veiculoRepository::findByPlacaIn), Veiculo::getPlaca);
            porRenavam = indexar(buscar(itens, VeiculoImportacaoItem::renavam, veiculoRepository::findByRenavamIn), Veiculo::getRenavam);
            porChassi = indexar(buscar(itens, VeiculoImportacaoItem::chassi, veiculoRepository::findByChassiIn), Veiculo::getChassi);
            tipos = veiculoRepository.buscarTodosTipos().stream()
                    .collect(Collectors.toMap(t -> TextoNormalizado.de(t.getDescricao()), t -> t.getIdTipo(), (a, b) -> a));
            cidades = resolvedorCidade.carregar(
                    itens.stream().map(VeiculoImportacaoItem::ufLicenciamento).filter(Objects::nonNull).toList());

            List<String> documentos = itens.stream().map(VeiculoImportacaoItem::agregadoDocumento).filter(Objects::nonNull).toList();
            agregadoPorDocumento = documentos.isEmpty() ? Map.of() : agregadoRepository.buscarIdsPorDocumento(documentos).stream()
                    .collect(Collectors.toMap(a -> a.getDocumento(), a -> a.getId()));
            // shortcut: carrega todos os agregados e motoristas para comparar o nome sem acento;
            // sao centenas hoje. Com dezenas de milhares, trocar por coluna de nome normalizado indexada.
            agregadosPorNome = agrupar(agregadoRepository.findAll(), Agregado::getNome, Agregado::getIdAgregado);
            motoristasPorNome = agrupar(motoristaRepository.findAll(), Motorista::getNome, Motorista::getIdMotorista);

            itens.forEach(this::conferir);
        }

        private void conferir(VeiculoImportacaoItem i) {
            int linha = i.numeroLinha();

            Integer idTipo = tipos.get(TextoNormalizado.de(i.tipoVeiculo()));
            if (i.tipoVeiculo() != null && idTipo == null) {
                erro(linha, "Tipo Veículo", i.tipoVeiculo(), "Tipo de veículo '" + i.tipoVeiculo() + "' não está cadastrado");
            }

            Cidade cidade = cidades.get(ResolvedorCidade.chave(i.cidadeLicenciamento(), i.ufLicenciamento()));
            if (i.cidadeLicenciamento() != null && cidade == null) {
                erro(linha, "Cidade Licenciamento", i.cidadeLicenciamento() + " - " + i.ufLicenciamento(),
                        "Cidade '" + i.cidadeLicenciamento() + " - " + i.ufLicenciamento() + "' não existe no cadastro do IBGE");
            }

            Veiculo mesmaPlaca = porPlaca.get(i.placa());
            if (mesmaPlaca != null && mesmaPlaca.getRenavam() != null && i.renavam() != null
                    && !mesmaPlaca.getRenavam().equals(i.renavam())) {
                erro(linha, "Renavam", i.renavam(), "A placa " + i.placa() + " já está cadastrada com o Renavam "
                        + mesmaPlaca.getRenavam() + ". Confira se é o mesmo veículo");
            }
            outraPlaca(linha, "Renavam", i.renavam(), porRenavam.get(i.renavam()), i.placa());
            outraPlaca(linha, "Chassi", i.chassi(), porChassi.get(i.chassi()), i.placa());

            resolvidos.put(linha, new Resolvido(
                    idTipo,
                    cidade == null ? null : cidade.getIdCidade(),
                    agregado(i),
                    unico(linha, "Motorista Padrão", i.motoristaPadrao(), motoristasPorNome, "Motorista")));
        }

        private UUID agregado(VeiculoImportacaoItem i) {
            // Map.of() vazio estoura com chave null: confere antes.
            UUID porDocumento = i.agregadoDocumento() == null ? null : agregadoPorDocumento.get(i.agregadoDocumento());
            return porDocumento != null
                    ? porDocumento
                    : unico(i.numeroLinha(), "Agregado", i.agregado(), agregadosPorNome, "Agregado");
        }

        /** Liga so quando o nome aponta para exatamente um cadastro: vinculo errado e pior que nenhum. */
        private UUID unico(int linha, String coluna, String nome, Map<String, List<UUID>> porNome, String quem) {
            if (nome == null) {
                return null;
            }
            List<UUID> ids = porNome.getOrDefault(TextoNormalizado.de(nome), List.of());
            if (ids.size() == 1) {
                return ids.get(0);
            }
            problemas.add(new ProblemaImportacao(linha, coluna, nome, false, ids.isEmpty()
                    ? quem + " '" + nome + "' não está cadastrado: o veículo fica sem vínculo até ele vir num manifesto"
                    : "Há " + ids.size() + " cadastros com o nome '" + nome + "': o veículo fica sem vínculo"));
            return null;
        }

        private void outraPlaca(int linha, String coluna, String valor, Veiculo dono, String placa) {
            if (valor == null || dono == null || dono.getPlaca().equals(placa)) {
                return;
            }
            erro(linha, coluna, valor, (Placas.mesmaConvertida(placa, dono.getPlaca())
                    ? "O " + coluna + " " + valor + " já está cadastrado na placa " + dono.getPlaca()
                            + ", que é a mesma placa antes da conversão para Mercosul"
                    : "O " + coluna + " " + valor + " já está cadastrado na placa " + dono.getPlaca()));
        }

        private void erro(int linha, String coluna, String valor, String mensagem) {
            problemas.add(new ProblemaImportacao(linha, coluna, valor, true, mensagem));
        }
    }

    private static List<Veiculo> buscar(List<VeiculoImportacaoItem> itens, Function<VeiculoImportacaoItem, String> campo,
            Function<Collection<String>, List<Veiculo>> consulta) {
        List<String> valores = itens.stream().map(campo).filter(Objects::nonNull).distinct().toList();
        return valores.isEmpty() ? List.of() : consulta.apply(valores);
    }

    private static Map<String, Veiculo> indexar(List<Veiculo> veiculos, Function<Veiculo, String> chave) {
        Map<String, Veiculo> mapa = new HashMap<>();
        veiculos.forEach(v -> mapa.put(chave.apply(v), v));
        return mapa;
    }

    private static <T> Map<String, List<UUID>> agrupar(List<T> todos, Function<T, String> nome, Function<T, UUID> id) {
        return todos.stream().collect(Collectors.groupingBy(
                t -> TextoNormalizado.de(nome.apply(t)), Collectors.mapping(id, Collectors.toList())));
    }
}
