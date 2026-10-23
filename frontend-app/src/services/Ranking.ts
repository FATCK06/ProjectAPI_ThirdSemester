import api from './api';
import type { IndicadoresMotorista } from './Dashboard';

// Mesmo cálculo e mesmos nomes de /dashboard/indicadores, mais o tipo de veículo.
export type RankingMensalMotorista = Pick<IndicadoresMotorista,
  | 'motoristaId'
  | 'nome'
  | 'numeroViagens'
  | 'disponibilidade'
  | 'utilizacao'
  | 'valorFrete'
  | 'custoTotal'
  | 'rentabilidade'
  | 'rentabilidadeMediaViagem'
> & { tipoVeiculo: string | null };

export async function buscarRankingMensal(mesReferencia: string, ordenarPor?: string) {
  const { data } = await api.get<RankingMensalMotorista[]>('/ranking/mensal', {
    params: { mesReferencia, ordenarPor },
  });
  return data;
}
