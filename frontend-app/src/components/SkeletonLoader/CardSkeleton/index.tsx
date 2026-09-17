// src/components/Skeletons/CardSkeleton/index.tsx
import '../skeleton.css';

export function CardSkeleton() {
  return (
    <div 
      className="skeleton-base shimmer" 
      style={{ height: '300px' }} // Altura padrão do seu gráfico/card
    />
  );
}