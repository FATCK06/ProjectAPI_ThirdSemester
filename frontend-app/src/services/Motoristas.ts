import api from "./api";

export interface MotoristaMes {
  motoristaId: string;
  veiculoId: string;
  nomeMotorista: string;
  placa: string;
  tipoVeiculo: string;
  viagensNoMes: number;
  diasUtilizados: number;
  diasDisponiveis: number;
  disponibilidade: number;
  utilizacao: number;
  percentualEfetividade: number | null;
}

export interface MotoristasMesPage {
  content: MotoristaMes[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface MotoristaFicha {
  idMotorista: string;
  nome: string;
  cpf: string;
  pis: string | null;
  dataNascimento: string | null;
  cep: string | null;
  logradouro: string | null;
  numero: string | null;
  bairro: string | null;
  idCidade: number | null;
  criadoEm: string | null;
}

export async function buscarMotoristasMes(
  mesReferencia: string,
  pagina: number,
): Promise<MotoristasMesPage> {
  const { data } = await api.get<MotoristasMesPage>("/dashboard/motoristas", {
    params: { mesReferencia, page: pagina - 1, size: 20 },
  });
  return data;
}

export async function buscarFichaMotorista(id: string): Promise<MotoristaFicha> {
  const { data } = await api.get<MotoristaFicha>(`/motoristas/id/${id}`);
  return data;
}

export type FaixaUtilizacao = "ALTA" | "INTERMEDIARIA" | "BAIXA";

export type SituacaoMotorista =
  | "DISPONIVEL"
  | "INDISPONIVEL"
  | "SEM_DIAS_DISPONIVEIS";

// Linha de /dashboard/motoristas/situacao. A situacao e gravada no backend
// durante a importacao; aqui so exibimos. utilizacao e faixaUtilizacao vem
// nulas quando o mes nao tem dias disponiveis (e entao aviso vem preenchido).
export interface SituacaoMotoristaItem {
  motoristaId: string;
  nome: string | null;
  diasDisponiveis: number;
  diasOperacao: number;
  utilizacao: number | null;
  faixaUtilizacao: FaixaUtilizacao | null;
  situacao: SituacaoMotorista;
  aviso: string | null;
}

// "todas" nao vai para o backend: sem o parametro ele devolve todas as situacoes.
export type FiltroSituacao = SituacaoMotorista | "todas";

export async function buscarSituacaoMotoristas(
  mesReferencia: string,
  situacao: FiltroSituacao = "todas",
): Promise<SituacaoMotoristaItem[]> {
  const { data } = await api.get<SituacaoMotoristaItem[]>(
    "/dashboard/motoristas/situacao",
    {
      params:
        situacao === "todas"
          ? { mesReferencia }
          : { mesReferencia, situacao },
    },
  );
  return data;
}
