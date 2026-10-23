import api from "./api";

// Campos financeiros ausentes para quem não é gestor; média e margem também
// faltam quando não há viagem apurável ou frete.
export interface IndicadoresMotorista {
  motoristaId: string;
  nome: string | null;
  numeroViagens: number;
  diasOperacao: number;
  diasDisponiveis: number;
  utilizacao: number;
  disponibilidade: number;
  valorFrete?: number;
  custoTotal?: number;
  rentabilidade?: number;
  rentabilidadeMediaViagem?: number;
  margem?: number;
}

export interface Totais {
  motoristas: number;
  numeroViagens: number;
  diasOperacao: number;
  // Viagens com frete e custo informados: só elas entram nas somas financeiras
  viagensApuradas: number;
  utilizacaoMedia: number;
  valorFrete?: number;
  custoTotal?: number;
  rentabilidade?: number;
  rentabilidadeMediaViagem?: number;
  margem?: number;
}

export interface IndicadoresMes {
  mesReferencia: string;
  diasNoMes: number;
  diasDisponiveis: number;
  totais: Totais;
  motoristas: IndicadoresMotorista[];
}

// Mês sem viagem vem com tudo zerado (motoristas = 0)
export interface HistoricoMes {
  mesReferencia: string;
  totais: Totais;
}

export interface IndicadoresPorModelo {
  modelo: string;
  numeroVeiculos: number;
  numeroViagens: number;
  diasOperacao: number;
  utilizacao: number;
  valorFrete?: number;
  custoTotal?: number;
  rentabilidade?: number;
  margem?: number;
}

// Linha de /dashboard/ranking-motoristas: top N por viagens, com o tipo do
// veículo da viagem mais longa (mesma regra do ranking mensal).
export interface RankingMotorista {
  posicao: number;
  motoristaId: string;
  nome: string | null;
  cpf: string | null;
  numeroViagens: number;
  tipoVeiculo: string | null;
  distanciaMaximaKm: number | null;
}

export async function buscarRankingMotoristas(
  mesReferencia: string,
  limite: number,
): Promise<RankingMotorista[]> {
  const { data } = await api.get<RankingMotorista[]>(
    "/dashboard/ranking-motoristas",
    { params: { mesReferencia, limite } },
  );
  return data;
}

export interface MesesReferencia {
  mesAtual: string;
  mesAnterior: string;
}

// Vem do servidor para não depender do relógio da máquina de quem acessa
export async function buscarMesesReferencia(): Promise<MesesReferencia> {
  const { data } = await api.get<MesesReferencia>("/dashboard/meses-referencia");
  return data;
}

export async function buscarIndicadores(
  mesReferencia: string,
): Promise<IndicadoresMes> {
  const { data } = await api.get<IndicadoresMes>("/dashboard/indicadores", {
    params: { mesReferencia },
  });
  return data;
}

// Totais dos últimos `meses` meses até mesReferencia, do mais antigo ao mais novo
export async function buscarHistorico(
  mesReferencia: string,
  meses: number,
): Promise<HistoricoMes[]> {
  const { data } = await api.get<HistoricoMes[]>(
    "/dashboard/indicadores/historico",
    {
      params: { mesReferencia, meses },
    },
  );
  return data;
}

export async function buscarIndicadoresPorModelo(
  mesReferencia: string,
): Promise<IndicadoresPorModelo[]> {
  const { data } = await api.get<IndicadoresPorModelo[]>(
    "/dashboard/indicadores-por-modelo",
    {
      params: { mesReferencia },
    },
  );
  return data;
}
