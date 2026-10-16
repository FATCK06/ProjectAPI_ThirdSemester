package br.com.newe.ms_operacoes.service.indicadores;

import java.math.BigDecimal;
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
 * Situacao de cada motorista por mes, a partir de controle_disponibilidade.
 *
 * A importacao grava os dias (disponiveis e em operacao) de cada motorista no
 * mes; a consulta so le a tabela, sem tocar em viagens. A tabela existente nao
 * tem coluna para utilizacao, faixa ou situacao e nao pode ser alterada, entao a
 * classificacao e feita na consulta, sobre os dias gravados. Efeito colateral:
 * trocar os cortes no application.properties vale na hora, sem reimportar.
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
     * Recalcula e grava (upsert por motorista + mes) os dias dos meses
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

        Map<UUID, ControleDisponibilidade> existentes = new HashMap<>();
        for (ControleDisponibilidade c : controleRepository.findByMesReferencia(mesReferencia)) {
            existentes.put(c.getIdMotorista(), c);
        }

        List<ControleDisponibilidade> gravar = new ArrayList<>();
        for (SomaMotoristaView soma : viagemRepository.somasPorMotorista(mesReferencia)) {
            ControleDisponibilidade controle = existentes.remove(soma.getMotoristaId());
            if (controle == null) {
                controle = new ControleDisponibilidade();
                controle.setIdMotorista(soma.getMotoristaId());
                controle.setMesReferencia(mesReferencia);
            }
            controle.setDiasDisponiveis(Math.toIntExact(diasDisponiveis));
            controle.setDiasOperacao(Math.toIntExact(soma.getDiasOperacao()));
            gravar.add(controle);
        }
        controleRepository.saveAll(gravar);

        // Sobrou quem nao tem mais viagem no mes: a tabela segue o banco.
        if (!existentes.isEmpty()) {
            controleRepository.deleteAll(existentes.values());
        }
    }

    /**
     * Le os dias gravados na importacao e classifica cada motorista.
     *
     * Ordem: DISPONIVEL primeiro, depois menor utilizacao, depois nome.
     *
     * Sem @Transactional: a chamada HTTP ao ms-frota nao pode segurar conexao do banco.
     *
     * @param situacao null traz todas
     */
    public List<SituacaoMotoristaDTO> listar(String mesReferencia, SituacaoMotorista situacao) {
        // Situacao nao e coluna da tabela: o filtro e aplicado depois de classificar.
        Map<ControleDisponibilidade, Classificacao> classificados = new HashMap<>();
        for (ControleDisponibilidade c : controleRepository.findByMesReferencia(mesReferencia)) {
            Classificacao classificacao = classificador.classificar(c.getDiasOperacao(), c.getDiasDisponiveis());
            if (situacao == null || classificacao.situacao() == situacao) {
                classificados.put(c, classificacao);
            }
        }

        if (classificados.isEmpty()) {
            return List.of();
        }

        // Nome pertence ao ms-frota: uma chamada em lote para o mes inteiro.
        List<UUID> ids = classificados.keySet().stream().map(ControleDisponibilidade::getIdMotorista).toList();
        Map<UUID, MotoristaResumo> cadastro = frotaClient.buscarMotoristas(ids).stream()
                .collect(Collectors.toMap(MotoristaResumo::id, Function.identity()));

        List<SituacaoMotoristaDTO> resultado = new ArrayList<>(classificados.size());
        classificados.forEach((c, classificacao) -> {
            MotoristaResumo resumo = cadastro.get(c.getIdMotorista());
            resultado.add(new SituacaoMotoristaDTO(
                    c.getIdMotorista(),
                    resumo != null ? resumo.nome() : null,
                    c.getDiasDisponiveis(),
                    c.getDiasOperacao(),
                    classificacao.utilizacao(),
                    classificacao.faixa(),
                    classificacao.situacao(),
                    classificacao.situacao() == SituacaoMotorista.SEM_DIAS_DISPONIVEIS ? AVISO_SEM_DIAS : null));
        });
        resultado.sort(ORDEM);
        return resultado;
    }

    static final Comparator<SituacaoMotoristaDTO> ORDEM = Comparator
            .comparing((SituacaoMotoristaDTO d) -> d.situacao() != SituacaoMotorista.DISPONIVEL)
            .thenComparing(SituacaoMotoristaDTO::utilizacao, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(SituacaoMotoristaDTO::nome, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
}
