import { SkeletonBloco } from "../SkeletonBloco";
import { SkeletonCard } from "../SkeletonCard";
import "../skeleton.css";

export interface ItemIndicadorSkeleton {
  // Indicador com valor e comparação; sem isso só o título e o "-"
  comVariacao: boolean;
  // Linha de tendência à direita (período de comparação maior que 1 mês)
  comGrafico: boolean;
}

interface PropsIndicadoresSkeleton {
  // Um por indicador que o card vai mostrar, na mesma ordem
  itens: ItemIndicadorSkeleton[];
}

/** Card de indicadores: ícone, título, valor, variação e linha de tendência de cada item. */
export function IndicadoresSkeleton({ itens }: PropsIndicadoresSkeleton) {
  return (
    // Ações: seletor "Comparar com" e botão Filtros
    <SkeletonCard rotulo="Carregando indicadores" comSobretitulo acoes={[200, 96]}>
      <div className="skeleton-indicadores-area">
        <div className="skeleton-indicadores-grid">
          {itens.map((item, i) => (
            <div key={i} className="skeleton-indicador">
              <SkeletonBloco largura={44} altura={44} raio={12} />
              <div className="skeleton-indicador-corpo">
                <SkeletonBloco largura="55%" altura={10} />
                <SkeletonBloco largura={item.comVariacao ? "65%" : 24} altura={28} />
                {item.comVariacao && <SkeletonBloco largura="40%" altura={10} />}
              </div>
              {item.comGrafico && (
                <SkeletonBloco className="skeleton-sparkline" largura={120} altura={40} />
              )}
            </div>
          ))}
        </div>
      </div>
    </SkeletonCard>
  );
}
