import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { IndicadoresSkeleton } from "../../components/SkeletonLoader/IndicadoresSkeleton";
import { GraficoSkeleton } from "../../components/SkeletonLoader/GraficoSkeleton";
import { RankingMotoristas } from "./components/frota/RankingMotoristas/RankingMotoristas";
import IndicadoresCard from "./components/IndicadoresCard/IndicadoresCard";
import {
  PREFERENCIA_COMPARACAO,
  PREFERENCIA_SELECIONADOS,
  comparacaoSalva,
  indicadoresVisiveis,
  selecaoSalva,
  temGrafico,
  type IndicadorId,
  type MesesComparacao,
} from "./components/IndicadoresCard/selecao";
import GraficoMotoristas from "./components/frota/GraficoRentabilidadeMotoristas/GraficoMotoristas";
import GraficoModelos from "./components/frota/GraficoModelos/GraficoModelos";
import { ComparativoMensal } from "./components/ComparativoMensal/ComparativoMensal";
import { SeletorMes } from "./components/SeletorMes/SeletorMes";
import { useDashboard } from "./useDashboard";
import { Aviso, AVISO_SEM_PERMISSAO_FINANCEIRO } from "../../components/Aviso/Aviso";
import { podeVerDadosFinanceiros } from "../../services/auth";
import { buscarMesesImportados, buscarMesesReferencia, type MesesReferencia } from "../../services/Dashboard";
import { ehMesValido, mesAtual } from "../../utils/datas";
import { gravarPreferencia, gravarPreferenciaSessao, lerPreferenciaSessao } from "../../utils/preferencias";
import "./dashboard.css";

// Enquanto false, o card mostra o aviso de "Em desenvolvimento" no lugar do gráfico
// e o endpoint por modelo nem é chamado. Ligado com a categoria de veículo (SCRUM-126):
// as barras são as sete categorias, mais "Não identificado" e "Não classificado".
const GRAFICO_MODELOS_DISPONIVEL = true;

// Último mês escolhido nesta aba: voltar pelo menu (/dashboard, sem ?mes) reabre nele
const PREFERENCIA_MES = "dashboard.mes";

/** Mês aceito no ?mes: formato válido e não depois do mês atual. */
function mesPermitido(valor: unknown, meses: MesesReferencia): valor is string {
  return typeof valor === "string" && ehMesValido(valor) && valor <= meses.mesAtual;
}

/** Sem dados no mês atual (início do mês, importação atrasada): abre no último importado. */
function mesInicial(meses: MesesReferencia, importados: string[]): string {
  const memorizado = lerPreferenciaSessao(PREFERENCIA_MES);
  if (mesPermitido(memorizado, meses)) return memorizado;
  if (importados.length === 0 || importados.includes(meses.mesAtual)) return meses.mesAtual;
  return importados[importados.length - 1];
}

/** Se a API dos meses falhar, calcula atual e anterior pelo relógio do navegador. */
function mesesLocais(): MesesReferencia {
  const atual = mesAtual();
  const [ano, mes] = atual.split("-").map(Number);
  const anterior = new Date(ano, mes - 2, 1);
  return {
    mesAtual: atual,
    mesAnterior: `${anterior.getFullYear()}-${String(anterior.getMonth() + 1).padStart(2, "0")}`,
  };
}

export function Dashboard() {
  const [meses, setMeses] = useState<MesesReferencia | null>(null);
  const [importados, setImportados] = useState<string[]>([]);
  // O mês vive na URL (?mes=): F5, voltar do navegador e link compartilhado mantêm
  const [searchParams, setSearchParams] = useSearchParams();
  const parametroMes = searchParams.get("mes");
  const mesReferencia = meses && mesPermitido(parametroMes, meses) ? parametroMes : null;
  const { dados, isLoading, erro } = useDashboard(mesReferencia, {
    buscarPorModelo: GRAFICO_MODELOS_DISPONIVEL,
  });
  // Os dois gráficos são de rentabilidade: sem a permissão não há o que mostrar.
  const financeiro = podeVerDadosFinanceiros();
  // Aqui e não no card: o card dá lugar ao skeleton a cada troca de mês, e a
  // escolha do usuário não pode voltar ao padrão por isso
  // Começa com o que o usuário deixou da última vez (ou o padrão do perfil)
  const [selecionados, setSelecionados] = useState<IndicadorId[]>(() => selecaoSalva(financeiro));
  const [mesesComparacao, setMesesComparacao] = useState<MesesComparacao>(comparacaoSalva);

  function mudarSelecionados(novos: IndicadorId[]) {
    setSelecionados(novos);
    gravarPreferencia(PREFERENCIA_SELECIONADOS, novos);
  }

  function mudarMesesComparacao(meses: MesesComparacao) {
    setMesesComparacao(meses);
    gravarPreferencia(PREFERENCIA_COMPARACAO, meses);
  }
  const itensSkeleton = indicadoresVisiveis(selecionados, financeiro).map((i) => ({
    comVariacao: !i.semDado,
    comGrafico: !i.semDado && temGrafico(mesesComparacao),
  }));

  function mudarMes(mes: string) {
    gravarPreferenciaSessao(PREFERENCIA_MES, mes);
    setSearchParams((atual) => {
      const parametros = new URLSearchParams(atual);
      parametros.set("mes", mes);
      return parametros;
    });
  }

  // Os meses vêm uma vez só, ao montar
  useEffect(() => {
    let cancelado = false;
    Promise.all([
      // Mantém o dashboard disponível se a API ainda não oferecer essas rotas
      buscarMesesReferencia().catch(mesesLocais),
      buscarMesesImportados().catch(() => []),
    ]).then(([resposta, lista]) => {
      if (cancelado) return;
      setMeses(resposta);
      setImportados(lista);
    });
    return () => {
      cancelado = true;
    };
  }, []);

  // Sem ?mes (ou inválido), completa a URL sem criar um passo a mais no histórico.
  // Não é só na montagem: clicar em "Dashboard" no menu estando no próprio
  // Dashboard leva a /dashboard sem ?mes, com o componente ainda montado.
  useEffect(() => {
    if (!meses || mesPermitido(parametroMes, meses)) return;
    const inicial = mesInicial(meses, importados);
    setSearchParams(
      (atual) => {
        const parametros = new URLSearchParams(atual);
        parametros.set("mes", inicial);
        return parametros;
      },
      { replace: true },
    );
  }, [meses, importados, parametroMes, setSearchParams]);

  const mensagemErro = erro;

  return (
    <div className="dashboard">
      {meses && mesReferencia && (
        <div className="dashboard-filtro" data-tour="dashboard-mes">
          <SeletorMes
            meses={meses}
            valor={mesReferencia}
            onChange={mudarMes}
          />
        </div>
      )}

      {mensagemErro ? (
        <p className="dashboard-erro">{mensagemErro}</p>
      ) : (
        <>
          <div className="dashboard-charts">
            <div className="dashboard-indicators" data-tour="dashboard-indicadores">
              {isLoading || !dados ? (
                // Os mesmos cards, na mesma ordem e com ou sem gráfico, que o card vai mostrar
                <IndicadoresSkeleton itens={itensSkeleton} />
              ) : (
                <IndicadoresCard
                  indicadores={dados.indicadores}
                  historico={dados.historico}
                  selecionados={selecionados}
                  onSelecionadosChange={mudarSelecionados}
                  mesesComparacao={mesesComparacao}
                  onMesesComparacaoChange={mudarMesesComparacao}
                />
              )}
            </div>

            {financeiro && (
              <>
                <div className="dashboard-chart dashboard-driver-chart" data-tour="dashboard-grafico-motoristas">
                  {isLoading || !dados ? (
                    <GraficoSkeleton altura={320} barras={10} />
                  ) : (
                    <GraficoMotoristas motoristas={dados.indicadores.motoristas} />
                  )}
                </div>

                <div className="dashboard-chart" data-tour="dashboard-grafico-tipos">
                  {/* Desligado, o card só mostra o aviso: não há o que esperar */}
                  {GRAFICO_MODELOS_DISPONIVEL && (isLoading || !dados) ? (
                    <GraficoSkeleton altura={320} barras={6} />
                  ) : (
                    <GraficoModelos
                      porModelo={dados?.porModelo ?? []}
                      disponivel={GRAFICO_MODELOS_DISPONIVEL}
                    />
                  )}
                </div>
              </>
            )}
          </div>

          {/* Os dois gráficos são de rentabilidade: o operador vê o aviso no lugar */}
          {!financeiro && (
            <div data-tour="dashboard-aviso-financeiro">
              <Aviso mensagem={AVISO_SEM_PERMISSAO_FINANCEIRO} />
            </div>
          )}
        </>
      )}

      {/* Fora do bloco acima: não depende do mês da página e mantém a seleção se ele falhar */}
      {/* Os data-tour abaixo só marcam o alvo do tutorial; os blocos não mudam */}
      <div data-tour="dashboard-comparativo">
        <ComparativoMensal />
      </div>

      {!mensagemErro && mesReferencia && (
        <div data-tour="dashboard-ranking">
          <RankingMotoristas
            mesReferencia={mesReferencia}
            indicadores={isLoading ? undefined : dados?.indicadores.motoristas}
          />
        </div>
      )}
    </div>
  );
}
