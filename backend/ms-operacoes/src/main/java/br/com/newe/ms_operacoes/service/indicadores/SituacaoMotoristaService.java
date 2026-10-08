package br.com.newe.ms_operacoes.service.indicadores;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.newe.ms_operacoes.client.FrotaClient;
import br.com.newe.ms_operacoes.dto.MotoristaResumo;
import br.com.newe.ms_operacoes.dto.SituacaoMotoristaDTO;
import br.com.newe.ms_operacoes.models.ControleDisponibilidade;
import br.com.newe.ms_operacoes.models.enums.SituacaoMotorista;
import br.com.newe.ms_operacoes.repository.ControleDisponibilidadeRepository;
import br.com.newe.ms_operacoes.repository.ViagemRepository;
import br.com.newe.ms_operacoes.repository.ViagemRepository.SomaMotoristaView;
import br.com.newe.ms_operacoes.service.indicadores.ClassificadorSituacao.Classificacao;

/**
 * Situacao de cada motorista por mes (controle_disponibilidade).
 *
 * Ao contrario do ranking do IndicadoresService, aqui o resultado e gravado: o
 * recalculo roda no fim da importacao e a consulta so le a tabela, sem
 * reclassificar.
 *
 * Escopo: so motoristas com viagem no mes. Quem nao rodou nenhum dia nao
 * aparece em viagens, e o cadastro completo de motoristas e do ms-frota.
 */
@Service
public class SituacaoMotoristaService {

    static final String AVISO_SEM_DIAS = "Motorista sem dias disponíveis registrados no período.";

    private final ViagemRepository viagemRepository;
    private final ControleDisponibilidadeRepository controleRepository;
    private final FrotaClient frotaClient;
    private final ClassificadorSituacao classificador;

    public SituacaoMotoristaService(
            ViagemRepository viagemRepository,
            ControleDisponibilidadeRepository controleRepository,
            FrotaClient frotaClient,
            @Value("${situacao.utilizacao.corte-baixa}") BigDecimal corteBaixa,
            @Value("${situacao.utilizacao.corte-alta}") BigDecimal corteAlta) {
        this.viagemRepository = viagemRepository;
        this.controleRepository = controleRepository;
        this.frotaClient = frotaClient;
        this.classificador = new ClassificadorSituacao(corteBaixa, corteAlta);
    }

    /**
     * Recalcula e grava (upsert por motorista + mes) a situacao dos meses
     * informados.
     *
     * Usa o total de viagens do mes no banco, e nao so as do arquivo: outro
     * arquivo pode ter viagens do mesmo mes, e a reimportacao ignora manifestos
     * ja gravados.
     *
     * Transacao propria: as viagens ja foram commitadas pelo
     * ManifestoBatchWriter, e uma falha aqui nao deve desfaze-las.
     *
     * @param meses "yyyy-MM"
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recalcularMeses(Collection<String> meses) {
        for (String mes : meses) {
            recalcularMes(mes);
        }
    }

    private void recalcularMes(String mesReferencia) {
        long diasDisponiveis = IndicadoresService.diasDisponiveis(YearMonth.parse(mesReferencia));
        LocalDateTime agora = LocalDateTime.now();

        Map<UUID, ControleDisponibilidade> existentes = new HashMap<>();
        for (ControleDisponibilidade c : controleRepository.findByMesReferencia(mesReferencia)) {
            existentes.put(c.getIdMotorista(), c);
        }

        List<ControleDisponibilidade> gravar = new ArrayList<>();
        for (SomaMotoristaView soma : viagemRepository.somasPorMotorista(mesReferencia)) {
            long diasOperacao = soma.getDiasOperacao();
            Classificacao classificacao = classificador.classificar(diasOperacao, diasDisponiveis);

            ControleDisponibilidade controle = existentes.remove(soma.getMotoristaId());
            if (controle == null) {
                controle = new ControleDisponibilidade();
                controle.setIdMotorista(soma.getMotoristaId());
                controle.setMesReferencia(mesReferencia);
            }
            controle.setDiasDisponiveis(Math.toIntExact(diasDisponiveis));
            controle.setDiasOperacao(Math.toIntExact(diasOperacao));
            controle.setUtilizacao(classificacao.utilizacao());
            controle.setFaixaUtilizacao(classificacao.faixa());
            controle.setSituacao(classificacao.situacao());
            controle.setAtualizadoEm(agora);
            gravar.add(controle);
        }
        controleRepository.saveAll(gravar);

        // Sobrou quem nao tem mais viagem no mes: a tabela segue o banco.
        if (!existentes.isEmpty()) {
            controleRepository.deleteAll(existentes.values());
        }
    }

    /**
     * Le o que foi gravado no tratamento; nao reclassifica.
     *
     * Ordem: DISPONIVEL primeiro, depois menor utilizacao, depois nome.
     *
     * Sem @Transactional: a chamada HTTP ao ms-frota nao pode segurar conexao do banco.
     *
     * @param situacao null traz todas
     */
    public List<SituacaoMotoristaDTO> listar(String mesReferencia, SituacaoMotorista situacao) {
        List<ControleDisponibilidade> linhas = situacao == null
                ? controleRepository.findByMesReferencia(mesReferencia)
                : controleRepository.findByMesReferenciaAndSituacao(mesReferencia, situacao);

        if (linhas.isEmpty()) {
            return List.of();
        }

        // Nome pertence ao ms-frota: uma chamada em lote para o mes inteiro.
        List<UUID> ids = linhas.stream().map(ControleDisponibilidade::getIdMotorista).toList();
        Map<UUID, MotoristaResumo> cadastro = frotaClient.buscarMotoristas(ids).stream()
                .collect(Collectors.toMap(MotoristaResumo::id, Function.identity()));

        List<SituacaoMotoristaDTO> resultado = new ArrayList<>(linhas.size());
        for (ControleDisponibilidade c : linhas) {
            MotoristaResumo resumo = cadastro.get(c.getIdMotorista());
            resultado.add(new SituacaoMotoristaDTO(
                    c.getIdMotorista(),
                    resumo != null ? resumo.nome() : null,
                    c.getDiasDisponiveis(),
                    c.getDiasOperacao(),
                    c.getUtilizacao(),
                    c.getFaixaUtilizacao(),
                    c.getSituacao(),
                    c.getSituacao() == SituacaoMotorista.SEM_DIAS_DISPONIVEIS ? AVISO_SEM_DIAS : null));
        }
        resultado.sort(ORDEM);
        return resultado;
    }

    static final Comparator<SituacaoMotoristaDTO> ORDEM = Comparator
            .comparing((SituacaoMotoristaDTO d) -> d.situacao() != SituacaoMotorista.DISPONIVEL)
            .thenComparing(SituacaoMotoristaDTO::utilizacao, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(SituacaoMotoristaDTO::nome, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
}
