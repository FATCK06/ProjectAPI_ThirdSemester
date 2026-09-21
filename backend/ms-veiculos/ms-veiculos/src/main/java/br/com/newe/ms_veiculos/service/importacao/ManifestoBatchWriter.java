package br.com.newe.ms_veiculos.service.importacao;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.newe.ms_veiculos.models.ArquivoImportacao;
import br.com.newe.ms_veiculos.models.Motorista;
import br.com.newe.ms_veiculos.models.Viagem;
import br.com.newe.ms_veiculos.repository.MotoristaRepository;
import br.com.newe.ms_veiculos.repository.MotoristaRepository.MotoristaCpfId;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Grava motoristas (upsert por CPF) e viagens em lote via EntityManager, com flush/clear
 * periodico. Motorista e Viagem usam GenerationType.SEQUENCE (nao IDENTITY) justamente para
 * permitir esse batching real no Postgres.
 */
@Service
public class ManifestoBatchWriter {

    private final MotoristaRepository motoristaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public ManifestoBatchWriter(MotoristaRepository motoristaRepository) {
        this.motoristaRepository = motoristaRepository;
    }

    @Transactional
    public int gravarEmLote(Long arquivoImportacaoId, List<LinhaManifesto> linhas, String mesReferencia) {
        Map<String, Long> cpfParaMotoristaId = upsertMotoristas(linhas);

        int contador = 0;
        for (LinhaManifesto linha : linhas) {
            Viagem viagem = montarViagem(linha, arquivoImportacaoId, cpfParaMotoristaId.get(linha.cpf()), mesReferencia);
            entityManager.persist(viagem);

            if (++contador % ManifestoCsvConfig.TAMANHO_CHUNK == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();
        entityManager.clear();

        return linhas.size();
    }

    private Map<String, Long> upsertMotoristas(List<LinhaManifesto> linhas) {
        Set<String> cpfs = new HashSet<>();
        Map<String, LinhaManifesto> primeiraOcorrenciaPorCpf = new HashMap<>();
        for (LinhaManifesto linha : linhas) {
            cpfs.add(linha.cpf());
            primeiraOcorrenciaPorCpf.putIfAbsent(linha.cpf(), linha);
        }

        Map<String, Long> cpfParaId = new HashMap<>();
        for (MotoristaCpfId existente : motoristaRepository.buscarIdsPorCpf(cpfs)) {
            cpfParaId.put(existente.getCpf(), existente.getId());
        }

        int contador = 0;
        for (String cpf : cpfs) {
            if (cpfParaId.containsKey(cpf)) {
                continue;
            }
            Motorista motorista = montarMotorista(primeiraOcorrenciaPorCpf.get(cpf));
            entityManager.persist(motorista);
            cpfParaId.put(cpf, motorista.getId());

            if (++contador % ManifestoCsvConfig.TAMANHO_CHUNK == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();
        entityManager.clear();

        return cpfParaId;
    }

    private Motorista montarMotorista(LinhaManifesto linha) {
        Motorista motorista = new Motorista();
        motorista.setCpf(linha.cpf());
        motorista.setNome(linha.nome());
        motorista.setPis(linha.pis());
        motorista.setDataNascimento(linha.dataNascimento());
        motorista.setEndereco(linha.endereco());
        motorista.setCep(linha.cep());
        motorista.setBairro(linha.bairro());
        motorista.setCidade(linha.cidade());
        motorista.setEstado(linha.estado());
        return motorista;
    }

    private Viagem montarViagem(LinhaManifesto linha, Long arquivoImportacaoId, Long motoristaId, String mesReferencia) {
        if (motoristaId == null) {
            throw new LinhaManifestoInvalidaException(linha.numeroLinha(),
                    "motorista nao resolvido para o CPF " + linha.cpf());
        }

        Viagem viagem = new Viagem();
        viagem.setArquivoImportacao(entityManager.getReference(ArquivoImportacao.class, arquivoImportacaoId));
        viagem.setMotorista(entityManager.getReference(Motorista.class, motoristaId));
        viagem.setMesReferencia(mesReferencia);
        viagem.setManifesto(linha.manifesto());
        viagem.setFilial(linha.filial());
        viagem.setData(linha.data());
        viagem.setVeiculo(linha.veiculo());
        viagem.setReboque1(linha.reboque1());
        viagem.setReboque2(linha.reboque2());
        viagem.setReboque3(linha.reboque3());
        viagem.setDestino(linha.destino());
        viagem.setKmSaida(linha.kmSaida());
        viagem.setKmChegada(linha.kmChegada());
        viagem.setServicos(linha.servicos());
        viagem.setNfs(linha.nfs());
        viagem.setKgReal(linha.kgReal());
        viagem.setKgTaxado(linha.kgTaxado());
        viagem.setM3(linha.m3());
        viagem.setCapacidadeVeiculo(linha.capacidadeVeiculo());
        viagem.setPercentualAprovVeiculo(linha.percentualAprovVeiculo());
        viagem.setValeFrete(linha.valeFrete());
        viagem.setValorNf(linha.valorNf());
        viagem.setValorFretes(linha.valorFretes());
        viagem.setValorFrete(linha.valorFrete());
        viagem.setCombustivel(linha.combustivel());
        viagem.setPedagio(linha.pedagio());
        viagem.setDiaria(linha.diaria());
        viagem.setColetas(linha.coletas());
        viagem.setEntregas(linha.entregas());
        viagem.setDespachos(linha.despachos());
        viagem.setRetiradas(linha.retiradas());
        viagem.setColetasReversa(linha.coletasReversa());
        viagem.setAdicionais(linha.adicionais());
        viagem.setDescontos(linha.descontos());
        viagem.setAdiantamento(linha.adiantamento());
        viagem.setDespesas(linha.despesas());
        viagem.setTotalDespesas(linha.totalDespesas());
        viagem.setSaldoDespesas(linha.saldoDespesas());
        viagem.setInss(linha.inss());
        viagem.setSestSenat(linha.sestSenat());
        viagem.setIr(linha.ir());
        viagem.setSaldoAPagar(linha.saldoAPagar());
        viagem.setServicosFinalizados(linha.servicosFinalizados());
        viagem.setPercentualEfetividade(linha.percentualEfetividade());
        viagem.setClassificacao(linha.classificacao());
        viagem.setObservacoesOperacionais(linha.observacoesOperacionais());
        viagem.setStatusManifesto(linha.statusManifesto());
        viagem.setUsuarioManifesto(linha.usuarioManifesto());
        return viagem;
    }
}
