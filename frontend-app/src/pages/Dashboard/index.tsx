import { CardSkeleton } from '../../components/SkeletonLoader/CardSkeleton';
import { TableSkeleton } from '../../components/SkeletonLoader/TableSkeleton';

export function Dashboard() {
  const isLoading = true; // Simulação do estado de carregamento

  if (isLoading) {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
        {/* Topo: Dois gráficos lado a lado */}
        <div style={{ display: 'flex', gap: '24px' }}>
          <CardSkeleton />
          <CardSkeleton />
        </div>
        
        {/* Base: A tabela de dados */}
        <TableSkeleton />
      </div>
    );
  }

  return (
    <div>
      {/* Aqui entrará o seu conteúdo real quando isLoading for false */}
      <h1>Dashboard Carregado!</h1>
    </div>
  );
}