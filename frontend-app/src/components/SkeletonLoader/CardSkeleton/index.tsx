import type { ReactNode } from "react";
import "../skeleton.css";

interface CardSkeletonProps {
  children?: ReactNode;
  isLoading: boolean;
  altura?: number | string; // cada seção tem uma altura diferente
}

/**
 * Bloco genérico do tamanho de um card. Para indicadores, tabelas e gráficos,
 * prefira IndicadoresSkeleton, TableSkeleton e GraficoSkeleton, que imitam o
 * formato do conteúdo.
 */
export function CardSkeleton({
  children,
  isLoading,
  altura = 300,
}: CardSkeletonProps) {
  if (!isLoading) return <>{children}</>;

  return (
    <div
      className="skeleton-base shimmer"
      style={{ height: altura }}
      role="status"
      aria-busy="true"
      aria-label="Carregando"
    />
  );
}
