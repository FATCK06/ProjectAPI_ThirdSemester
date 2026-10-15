import type { ReactNode } from "react";

interface CardSkeletonProps {
  children?: ReactNode;
  isLoading: boolean;
  altura?: number | string; // cada seção tem uma altura diferente
}

export function CardSkeleton({
  children,
  isLoading,
  altura = 300,
}: CardSkeletonProps) {
  if (!isLoading) return <>{children}</>;

  return (
    <div className={`card-skeleton-frame skeleton-base ${isLoading ? "shimmer" : ""}`}>
      {!isLoading && children}
    </div>
  );
}