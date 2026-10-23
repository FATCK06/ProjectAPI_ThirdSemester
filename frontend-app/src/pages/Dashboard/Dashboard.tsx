<<<<<<< HEAD
import { useEffect, useState } from "react";
import { IndicadoresSkeleton } from "../../components/SkeletonLoader/IndicadoresSkeleton";
import { GraficoSkeleton } from "../../components/SkeletonLoader/GraficoSkeleton";
import { RankingMotoristas } from "./components/frota/RankingMotoristas/RankingMotoristas";
import IndicadoresCard from "./components/IndicadoresCard/IndicadoresCard";
import {
  COMPARACAO_INICIAL,
  SELECAO_INICIAL,
  indicadoresVisiveis,
  temGrafico,
  type IndicadorId,
  type MesesComparacao,
} from "./components/IndicadoresCard/selecao";
import GraficoMotoristas from "./components/frota/GraficoRentabilidadeMotoristas/GraficoMotoristas";
import GraficoModelos from "./components/frota/GraficoModelos/GraficoModelos";
import { SeletorMes } from "./components/SeletorMes/SeletorMes";
import { useDashboard } from "./useDashboard";
import { Aviso, AVISO_SEM_PERMISSAO_FINANCEIRO } from "../../components/Aviso/Aviso";
import { podeVerDadosFinanceiros } from "../../services/auth";
import { buscarMesesReferencia, type MesesReferencia } from "../../services/Dashboard";
=======
import { useState } from "react";
import { CardSkeleton } from "../../components/SkeletonLoader/CardSkeleton";
import { RankingMotoristas } from "./components/frota/RankingMotoristas/RankingMotoristas";
import IndicadoresCard, {
  type ComparativoIndicador,
  type IndicadorId,
} from "./components/IndicadoresCard/IndicadoresCard";
import GraficoMotoristas from "./components/frota/GraficoRentabilidadeMotoristas/GraficoMotoristas";
import GraficoModelos from "./components/frota/GraficoModelos/GraficoModelos";
import { useDashboard } from "./useDashboard";
import { Aviso, AVISO_SEM_PERMISSAO_FINANCEIRO } from "../../components/Aviso/Aviso";
import { podeVerDadosFinanceiros } from "../../services/auth";
>>>>>>> origin/develop
import "./dashboard.css";

// Enquanto false, o card mostra o aviso de "Em desenvolvimento" no lugar do gráfico
// e o endpoint por modelo nem é chamado. Mudar para true quando os dados por tipo
// de veículo estiverem prontos.
const GRAFICO_MODELOS_DISPONIVEL = false;

export function Dashboard() {
<<<<<<< HEAD
  const [meses, setMeses] = useState<MesesReferencia | null>(null);
  const [erroMeses, setErroMeses] = useState(false);
  const [mesReferencia, setMesReferencia] = useState<string | null>(null);
  const { dados, isLoading, erro } = useDashboard(mesReferencia, {
    buscarPorModelo: GRAFICO_MODELOS_DISPONIVEL,
  });
  // Os dois gráficos são de rentabilidade: sem a permissão não há o que mostrar.
  const financeiro = podeVerDadosFinanceiros();
  // Aqui e não no card: o card dá lugar ao skeleton a cada troca de mês, e a
  // escolha do usuário não pode voltar ao padrão por isso
  const [selecionados, setSelecionados] = useState<IndicadorId[]>(SELECAO_INICIAL);
  const [mesesComparacao, setMesesComparacao] = useState<MesesComparacao>(COMPARACAO_INICIAL);
  const itensSkeleton = indicadoresVisiveis(selecionados, financeiro).map((i) => ({
    comVariacao: !i.semDado,
    comGrafico: !i.semDado && temGrafico(mesesComparacao),
  }));

  useEffect(() => {
    let cancelado = false;
    buscarMesesReferencia()
      .then((resposta) => {
        if (cancelado) return;
        setMeses(resposta);
        setMesReferencia(resposta.mesAtual);
      })
      .catch(() => {
        if (!cancelado) setErroMeses(true);
      });
    return () => {
      cancelado = true;
    };
  }, []);

  const mensagemErro = erroMeses
    ? "Não foi possível carregar o mês de referência."
    : erro;

  return (
    <div className="dashboard">
      {meses && mesReferencia && (
        <div className="dashboard-filtro">
          <SeletorMes
            meses={meses}
            valor={mesReferencia}
            onChange={setMesReferencia}
          />
        </div>
      )}

      {mensagemErro ? (
        <p className="dashboard-erro">{mensagemErro}</p>
      ) : (
        <>
          <div className="dashboard-charts">
            <div className="dashboard-indicators">
              {isLoading || !dados ? (
                // Os mesmos cards, na mesma ordem e com ou sem gráfico, que o card vai mostrar
                <IndicadoresSkeleton itens={itensSkeleton} />
              ) : (
                <IndicadoresCard
                  indicadores={dados.indicadores}
                  historico={dados.historico}
                  selecionados={selecionados}
                  onSelecionadosChange={setSelecionados}
                  mesesComparacao={mesesComparacao}
                  onMesesComparacaoChange={setMesesComparacao}
                />
              )}
            </div>

            {financeiro && (
              <>
                <div className="dashboard-chart dashboard-driver-chart">
                  {isLoading || !dados ? (
                    <GraficoSkeleton altura={320} barras={10} />
                  ) : (
                    <GraficoMotoristas motoristas={dados.indicadores.motoristas} />
                  )}
                </div>

                <div className="dashboard-chart">
                  {/* Desligado, o card só mostra o aviso: não há o que esperar */}
                  {GRAFICO_MODELOS_DISPONIVEL && (isLoading || !dados) ? (
                    <GraficoSkeleton altura={300} barras={6} />
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
          {!financeiro && <Aviso mensagem={AVISO_SEM_PERMISSAO_FINANCEIRO} />}

          {mesReferencia && (
            <RankingMotoristas
              mesReferencia={mesReferencia}
              indicadores={isLoading ? undefined : dados?.indicadores.motoristas}
            />
          )}
        </>
      )}
=======
  const [mesReferencia] = useState("2026-06");
  const { dados, isLoading, erro } = useDashboard(mesReferencia);
  const financeiro = podeVerDadosFinanceiros();

  return (
    <div className="dashboard">
      {erro ? (
        <p className="dashboard-erro">{erro}</p>
      ) : (
        <div className="dashboard-charts">
          <div className="dashboard-indicators">
            <CardSkeleton isLoading={isLoading} altura={260}>
              {dados && (
                <IndicadoresCard
                  indicadores={dados.indicadores}
                  comparativo={COMPARATIVO_TESTE}
                />
              )}
            </CardSkeleton>
          </div>

          {financeiro && (
            <>
              <div className="dashboard-chart dashboard-driver-chart">
                <CardSkeleton isLoading={isLoading} altura={380}>
                  {dados && (
                    <GraficoMotoristas motoristas={dados.indicadores.motoristas} />
                  )}
                </CardSkeleton>
              </div>

              <div className="dashboard-chart">
                <CardSkeleton isLoading={isLoading} altura={380}>
                  {dados && <GraficoModelos porModelo={dados.porModelo} />}
                </CardSkeleton>
              </div>
            </>
          )}
        </div>
      )}

      {/* Os dois gráficos são de rentabilidade: o operador vê o aviso no lugar */}
      {!financeiro && <Aviso mensagem={AVISO_SEM_PERMISSAO_FINANCEIRO} />}

      <RankingMotoristas />
>>>>>>> origin/develop
    </div>
  );
}
