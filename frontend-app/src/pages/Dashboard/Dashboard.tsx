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
import "./dashboard.css";

// TEMPORÁRIO: dados falsos só para conferir o visual. Apagar quando o backend fornecer o histórico.
const COMPARATIVO_TESTE: Partial<Record<IndicadorId, ComparativoIndicador>> = {
  numeroViagens: {
    variacaoPercentual: 12.4,
    rotuloAnterior: "vs. Maio/2026 (430)",
    serie: [390, 410, 400, 430, 460, 483],
  },
  diasDisponiveis: {
    variacaoPercentual: 5.7,
    rotuloAnterior: "vs. Maio/2026 (28)",
    serie: [26, 27, 26, 28, 29, 30],
  },
  utilizacaoMedia: {
    variacaoPercentual: -2.8,
    rotuloAnterior: "vs. Maio/2026 (16,78%)",
    serie: [17.2, 16.9, 17.1, 16.78, 16.5, 16.31],
  },
};

export function Dashboard() {
  const [mesReferencia] = useState("2026-06");
  const { dados, isLoading, erro } = useDashboard(mesReferencia);

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
        </div>
      )}

      <RankingMotoristas />
    </div>
  );
}