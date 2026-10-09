import { useState } from "react";
import { CardSkeleton } from "../../components/SkeletonLoader/CardSkeleton";
import { RankingMotoristas } from "./components/RankingMotoristas/RankingMotoristas";
import IndicadoresCard from "./components/IndicadoresCard/IndicadoresCard";
import GraficoMotoristas from "./components/GraficoRentabilidadeMotoristas/GraficoMotoristas";
import GraficoModelos from "./components/GraficoModelos/GraficoModelos.tsx";
import { useDashboard } from "./useDashboard";
import "./dashboard.css";

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
            <CardSkeleton isLoading={isLoading} altura={180}>
              {dados && <IndicadoresCard indicadores={dados.indicadores} />}
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