import api from "./api";

export interface IndicadoresMotorista {
  motoristaId: string;
  nome: string | null;
  numeroViagens: number;
  diasOperacao: number;
  diasDisponiveis: number;
  utilizacao: number;
  disponibilidade: number;
  valorFrete: number;
  custoTotal: number;
  rentabilidade: number;
  rentabilidadeMediaViagem: number;
  margem: number;
}

export interface Totais {
  motoristas: number;
  numeroViagens: number;
  utilizacaoMedia: number;
  valorFrete: number;
  custoTotal: number;
  rentabilidade: number;
  rentabilidadeMediaViagem: number;
  margem: number;
}

export interface IndicadoresMes {
  mesReferencia: string;
  diasNoMes: number;
  diasDisponiveis: number;
  totais: Totais;
  motoristas: IndicadoresMotorista[];
}

export interface IndicadoresPorModelo {
  modelo: string;
  numeroVeiculos: number;
  numeroViagens: number;
  diasOperacao: number;
  utilizacao: number;
  valorFrete: number;
  custoTotal: number;
  rentabilidade: number;
  margem: number;
}

export async function buscarIndicadores(
  mesReferencia: string,
): Promise<IndicadoresMes> {
  const { data } = await api.get<IndicadoresMes>("/dashboard/indicadores", {
    params: { mesReferencia },
  });
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
