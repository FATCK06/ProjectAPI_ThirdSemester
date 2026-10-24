import api from './api';

export interface RankingMensalMotorista {
  motoristaId: string;
  nome?: string | null;
  // Compatibilidade com versões da API que serializavam o nome como "motorista".
  motorista?: string | null;
  tipoVeiculo: string | null;
  numeroViagens: number;
  viagens?: number;
  disponibilidade: number;
  utilizacao: number;
  valorFrete?: number;
  valorFretes?: number;
  custoTotal?: number;
  custos?: number;
  rentabilidade?: number;
  rentabilidadeMediaViagem?: number;
}

export async function buscarRankingMensal(mesReferencia: string) {
  const { data } = await api.get<RankingMensalMotorista[]>('/ranking/mensal', {
    params: { mesReferencia },
  });
  return data.map((linha) => ({
    ...linha,
    numeroViagens: linha.numeroViagens ?? linha.viagens ?? 0,
    valorFrete: linha.valorFrete ?? linha.valorFretes,
    custoTotal: linha.custoTotal ?? linha.custos,
  }));
}
