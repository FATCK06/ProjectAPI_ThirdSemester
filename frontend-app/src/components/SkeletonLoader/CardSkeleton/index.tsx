import type { ReactNode } from "react";

interface CardSkeletonProps {
  children?: ReactNode;
  isLoading: boolean; 
}

export function CardSkeleton({ children, isLoading }: CardSkeletonProps) {
  return (
    <div className="card">
      <div className={`card-chart skeleton-base ${isLoading ? "shimmer" : ""}`}>
        {children}
      </div>
    </div>
  );
}