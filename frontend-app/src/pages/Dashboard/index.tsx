import { useEffect, useState } from "react";
import { CardSkeleton } from "../../components/SkeletonLoader/CardSkeleton";
import { RankingMotoristas } from "../../components/RankingMotoristas";
import Grafico from "../../graficos/Grafico";
import Grafico2 from "../../graficos/Grafico2";

export function Dashboard() {
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const timer = setTimeout(() => {
      setIsLoading(false);
    }, 2000);

    return () => clearTimeout(timer);
  }, []);

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: "24px" }}>
      <div style={{ display: "flex", gap: "24px", width: "100%" }}>
        <CardSkeleton isLoading={isLoading}>
          {!isLoading && <Grafico />}
        </CardSkeleton>
        <CardSkeleton isLoading={isLoading}>
          {!isLoading && <Grafico2 />}
        </CardSkeleton>
      </div>

      {/* Base: Ranking dos 5 motoristas com mais viagens */}
      <RankingMotoristas />
    </div>
  );
}
