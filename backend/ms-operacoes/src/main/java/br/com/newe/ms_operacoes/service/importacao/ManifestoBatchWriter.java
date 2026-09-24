package br.com.newe.ms_operacoes.service.importacao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.newe.ms_operacoes.models.TipoCusto;
import br.com.newe.ms_operacoes.models.Viagem;
import br.com.newe.ms_operacoes.models.ViagemCusto;
import br.com.newe.ms_operacoes.repository.TipoCustoRepository;
import br.com.newe.ms_operacoes.repository.ViagemRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Grava as viagens e seus custos. So persistencia: as referencias de motorista, veiculo e
 * agregado ja chegam resolvidas pelo {@link ResolvedorFrota}, para que nenhuma
 * chamada HTTP aconteca com a transacao aberta.
 */
@Service
public class ManifestoBatchWriter {

    private static final Logger log = LoggerFactory.getLogger(ManifestoBatchWriter.class);

    private final ViagemRepository viagemRepository;
    private final TipoCustoRepository tipoCustoRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public ManifestoBatchWriter(ViagemRepository viagemRepository, TipoCustoRepository tipoCustoRepository) {
        this.viagemRepository = viagemRepository;
        this.tipoCustoRepository = tipoCustoRepository;
    }

    /**
     * @return quantas viagens foram realmente gravadas (menor que linhas.size()
     *         quando o arquivo repete manifesto ja importado antes).
     */
    @Transactional
    public int gravarEmLote(Long importacaoId, List<LinhaManifesto> linhas, ReferenciasFrota referencias) {
        Set<Integer> jaGravados = manifestosJaGravados(linhas);
        Map<String, Integer> tiposCusto = carregarTiposCusto();

        int contador = 0;
        for (LinhaManifesto linha : linhas) {
            if (jaGravados.contains(linha.idManifesto())) {
                continue;
            }

            Viagem viagem = montarViagem(linha, importacaoId, referencias);
            entityManager.persist(viagem);

            // id_viagem e IDENTITY: o persist acima ja trouxe a chave.
            gravarCustos(viagem, linha, tiposCusto);

            // id_viagem e IDENTITY, entao o Hibernate nao consegue agrupar os inserts
            // (precisa da chave de volta a cada um). O flush periodico serve para
            // limitar memoria, nao para ganhar batching.
            if (++contador % ManifestoCsvConfig.TAMANHO_CHUNK == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();
        entityManager.clear();

        int ignorados = linhas.size() - contador;
        if (ignorados > 0) {
            log.info("Importacao {}: {} linhas ignoradas por manifesto ja existente", importacaoId, ignorados);
        }

        return contador;
    }

    /**
     * id_manifesto e UNIQUE. Sem este filtro, reimportar o mesmo arquivo estouraria
     * a constraint e derrubaria a importacao inteira em vez de ser inofensivo.
     */
    private Set<Integer> manifestosJaGravados(List<LinhaManifesto> linhas) {
        List<Integer> manifestos = new ArrayList<>(linhas.size());
        for (LinhaManifesto linha : linhas) {
            manifestos.add(linha.idManifesto());
        }
        return manifestos.isEmpty()
                ? Set.of()
                : new HashSet<>(viagemRepository.buscarManifestosExistentes(manifestos));
    }

    private Viagem montarViagem(LinhaManifesto linha, Long importacaoId, ReferenciasFrota referencias) {
        UUID idMotorista = referencias.motorista(linha.cpf());
        UUID idVeiculo = referencias.veiculo(linha.veiculo());

        // Ambos sao NOT NULL em viagens. Se o ms-frota acabou de criar e ainda assim
        // nao voltou id, e bug de integracao - falhar alto e melhor que gravar torto.
        if (idMotorista == null) {
            throw new LinhaManifestoInvalidaException(linha.numeroLinha(),
                    "ms-frota nao devolveu id para o CPF " + linha.cpf());
        }
        if (idVeiculo == null) {
            throw new LinhaManifestoInvalidaException(linha.numeroLinha(),
                    "ms-frota nao devolveu id para a placa " + linha.veiculo());
        }

        Viagem viagem = new Viagem();
        viagem.setIdManifesto(linha.idManifesto());
        viagem.setIdMotorista(idMotorista);
        viagem.setIdVeiculo(idVeiculo);
        viagem.setIdAgregado(referencias.agregado(linha.agregadoDocumento()));
        viagem.setImportacaoId(importacaoId);

        viagem.setDataViagem(linha.data());
        viagem.setMesReferencia(linha.mesReferencia());
        viagem.setFilial(linha.filial());
        viagem.setDestino(linha.destino());
        viagem.setReboque1(linha.reboque1());
        viagem.setReboque2(linha.reboque2());
        viagem.setReboque3(linha.reboque3());

        viagem.setKgReal(linha.kgReal());
        viagem.setKgTaxado(linha.kgTaxado());
        viagem.setM3(linha.m3());

        viagem.setKmSaida(linha.kmSaida());
        viagem.setKmChegada(linha.kmChegada());
        viagem.setServicos(linha.servicos());
        viagem.setServicosFinalizados(linha.servicosFinalizados());
        viagem.setNfs(linha.nfs());
        viagem.setColetas(linha.coletas());
        viagem.setEntregas(linha.entregas());
        viagem.setDespachos(linha.despachos());
        viagem.setRetiradas(linha.retiradas());
        viagem.setColetasReversa(linha.coletasReversa());
        viagem.setPercentualEfetividade(linha.percentualEfetividade());
        viagem.setPercentualAprovVeiculo(linha.percentualAprovVeiculo());

        viagem.setValorFrete(linha.valorFrete());
        viagem.setValorFretes(linha.valorFretes());
        viagem.setValorNf(linha.valorNf());
        viagem.setTotalDespesas(linha.totalDespesas());
        viagem.setSaldoDespesas(linha.saldoDespesas());
        viagem.setSaldoAPagar(linha.saldoAPagar());

        viagem.setStatus(linha.statusManifesto());
        viagem.setClassificacao(linha.classificacao());
        viagem.setObservacoesOperacionais(linha.observacoesOperacionais());
        viagem.setUsuarioManifesto(linha.usuarioManifesto());

        return viagem;
    }

    /** descricao -> id_tipo_custo, lido uma vez por importacao. */
    private Map<String, Integer> carregarTiposCusto() {
        Map<String, Integer> porDescricao = new HashMap<>();
        for (TipoCusto tipo : tipoCustoRepository.findAll()) {
            porDescricao.put(tipo.getDescricao(), tipo.getIdTipoCusto());
        }

        List<String> ausentes = new ArrayList<>();
        for (CustoDoManifesto custo : CustoDoManifesto.values()) {
            if (!porDescricao.containsKey(custo.descricao())) {
                ausentes.add(custo.descricao());
            }
        }
        if (!ausentes.isEmpty()) {
            throw new IllegalStateException(
                    "tipos_custo sem as descricoes " + ausentes + ". Rode a migration V3 antes de importar.");
        }

        return porDescricao;
    }

    /** Uma linha por desembolso com valor. Ausente e zero nao viram linha. */
    private void gravarCustos(Viagem viagem, LinhaManifesto linha, Map<String, Integer> tiposCusto) {
        for (CustoDoManifesto custo : CustoDoManifesto.values()) {
            BigDecimal valor = custo.valorEm(linha);

            if (valor == null || valor.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            entityManager.persist(new ViagemCusto(
                    viagem.getIdViagem(),
                    tiposCusto.get(custo.descricao()),
                    valor));
        }
    }
}
