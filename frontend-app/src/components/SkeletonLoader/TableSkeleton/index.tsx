// src/components/Skeletons/TableSkeleton/index.tsx
import '../skeleton.css';

export function TableSkeleton() {
  return (
    <div 
      className="skeleton-base shimmer" 
      style={{ height: '400px' }} // Altura padrão da tabela
    />
  );
}