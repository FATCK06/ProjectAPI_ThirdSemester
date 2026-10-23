import { SkeletonBloco } from "../SkeletonBloco";
import { SkeletonCard } from "../SkeletonCard";
import "../skeleton.css";

interface PropsGraficoSkeleton {
  // Mesma altura da área do gráfico real (.ui-card-chart)
  altura?: number;
  barras?: number;
}

// Alturas fixas e desiguais: lembra um gráfico de barras sem sugerir um valor
const ALTURAS = ["85%", "70%", "60%", "55%", "45%", "40%", "32%", "26%", "20%", "15%"];

/** Card com gráfico de barras: título, eixo Y e barras alinhadas na base. */
export function GraficoSkeleton({ altura = 320, barras = 8 }: PropsGraficoSkeleton) {
  return (
    <SkeletonCard rotulo="Carregando gráfico">
      <div className="skeleton-grafico" style={{ height: altura }}>
        <div className="skeleton-grafico-eixo">
          {Array.from({ length: 5 }, (_, i) => (
            <SkeletonBloco key={i} largura={32} altura={8} />
          ))}
        </div>
        {Array.from({ length: barras }, (_, i) => (
          <SkeletonBloco key={i} className="skeleton-barra" altura={ALTURAS[i % ALTURAS.length]} />
        ))}
      </div>
    </SkeletonCard>
  );
}
