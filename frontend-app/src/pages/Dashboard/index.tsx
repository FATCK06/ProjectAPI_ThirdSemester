import { useEffect, useState } from "react";
import { CardSkeleton } from "../../components/SkeletonLoader/CardSkeleton";
import { RankingMotoristas } from "../../components/RankingMotoristas";
import {
  buscarIndicadores,
  buscarIndicadoresPorModelo,
  type IndicadoresMes,
  type IndicadoresPorModelo,
} from "../../services/Dashboard";
import Grafico from "./components/Grafico";
import Grafico2 from "./components/Grafico2";
import Card from "./components/Card";

function mesAtual(): string {
  const hoje = new Date();
  const mes = String(hoje.getMonth() + 1).padStart(2, "0");
  return `${hoje.getFullYear()}-${mes}`;
}

export function Dashboard() {
  const [mesReferencia, setMesReferencia] = useState("2026-06");
  const [indicadores, setIndicadores] = useState<IndicadoresMes | null>(null);
  const [porModelo, setPorModelo] = useState<IndicadoresPorModelo[] | null>(
    null,
  );
  const [isLoading, setIsLoading] = useState(true);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    let cancelado = false;
    setIsLoading(true);
    setErro(null);

    // As duas chamadas saem juntas; so desliga o loading quando AMBAS terminam,
    // assim o skeleton fica ate o card e os dois graficos estarem prontos.
    Promise.all([
      buscarIndicadores(mesReferencia),
      buscarIndicadoresPorModelo(mesReferencia),
    ])
      .then(([resIndicadores, resPorModelo]) => {
        if (!cancelado) {
          setIndicadores(resIndicadores);
          setPorModelo(resPorModelo);
          setIsLoading(false);
        }
      })
      .catch(() => {
        if (!cancelado) {
          setErro("Não foi possível carregar os indicadores deste mês.");
          setIsLoading(false);
        }
      });

    return () => {
      cancelado = true;
    };
  }, [mesReferencia]);

  return (
    <div
      style={{
        display: "flex",
        flexDirection: "column",
        gap: "24px",
        width: "100%",
      }}
    >
      <div
        style={{
          display: "flex",
          flexWrap: "wrap",
          gap: "24px",
          width: "100%",
        }}
      >
        <div style={{ width: "100%", minHeight: "300px" }}>
          <CardSkeleton isLoading={isLoading}>
            {!isLoading && erro && <p>{erro}</p>}
            {!isLoading && !erro && indicadores && (
              <Card indicadores={indicadores} />
            )}
          </CardSkeleton>
        </div>

        <div style={{ flex: "1 1 calc(50% - 12px)" }}>
          <CardSkeleton isLoading={isLoading}>
            {!isLoading && !erro && indicadores && (
              <Grafico motoristas={indicadores.motoristas} />
            )}
          </CardSkeleton>
        </div>

        <div style={{ flex: "1 1 calc(50% - 12px)" }}>
          <CardSkeleton isLoading={isLoading}>
            {!isLoading && !erro && porModelo && (
              <Grafico2 porModelo={porModelo} />
            )}
          </CardSkeleton>
        </div>
      </div>

      <RankingMotoristas />
    </div>
  );
}
