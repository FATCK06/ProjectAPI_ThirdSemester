import type { ReactNode } from "react";

interface CardSkeletonProps {
  children?: ReactNode;
  isLoading: boolean;
}

export function CardSkeleton({ children, isLoading }: CardSkeletonProps) {
  return (
    <div className={`card-skeleton-frame skeleton-base ${isLoading ? "shimmer" : ""}`}>
      {!isLoading && children}
    </div>
  );
}
