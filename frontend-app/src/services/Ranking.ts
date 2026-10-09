import api from './api';

export interface RankingMensalMotorista {
  motoristaId: string;
  motorista: string | null;
  tipoVeiculo: string | null;
  viagens: number;
  disponibilidade: number;
  utilizacao: number;
  valorFretes: number;
  custos: number;
  rentabilidade: number;
  rentabilidadeMediaViagem: number;
}

export async function buscarRankingMensal(mesReferencia: string) {
  const { data } = await api.get<RankingMensalMotorista[]>('/ranking/mensal', {
    params: { mesReferencia },
  });
  return data;
}
