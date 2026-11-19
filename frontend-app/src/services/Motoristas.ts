import api from "./api";
import type { IndicadoresMotorista } from "./Dashboard";

export interface MotoristaMes {
  motoristaId: string;
  veiculoId: string;
  nomeMotorista: string;
  placa: string;
  tipoVeiculo: string;
  modelo: string;
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

/** Categoria de veículo (Fiorino, Van, VUC, 3/4, Toco, Truck, Carreta): é o que a coluna "Tipo de veículo" mostra. */
export interface TipoVeiculo {
  idCategoria: number;
  descricao: string;
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
  nomeCidade: string | null;
  criadoEm: string | null;
}

export async function buscarMotoristasMes(
  mesReferencia: string,
  pagina: number,
  opcoes: { busca?: string; tipoVeiculo?: string; tamanho?: number } = {},
): Promise<MotoristasMesPage> {
  const { busca = "", tipoVeiculo = "", tamanho = 20 } = opcoes;
  const { data } = await api.get<MotoristasMesPage>("/dashboard/motoristas", {
    params: { mesReferencia, page: pagina - 1, size: tamanho, busca, tipoVeiculo },
  });
  return data;
}

export async function baixarOperacaoMensalCsv(
  mesReferencia: string,
  busca: string,
  tipoVeiculo: string,
  aoProgredir: (progresso?: number) => void,
): Promise<Blob> {
  const { data } = await api.get<Blob>("/dashboard/motoristas/csv", {
    params: { mesReferencia, busca, tipoVeiculo },
    responseType: "blob",
    onDownloadProgress: (evento) => aoProgredir(evento.progress),
  });
  return data;
}

export async function buscarMotoristaNoMes(motoristaId: string, mesReferencia: string): Promise<MotoristaMes | null> {
  try {
    const { data } = await api.get<MotoristaMes>(`/dashboard/motoristas/${motoristaId}`, {
      params: { mesReferencia },
    });
    return data;
  } catch (erro) {
    if (typeof erro === "object" && erro !== null && "response" in erro) {
      const resposta = (erro as { response?: { status?: number } }).response;
      if (resposta?.status === 404) return null;
    }
    throw erro;
  }
}

export interface IndicadorMotoristaMes {
  mesReferencia: string;
  indicadores: IndicadoresMotorista | null;
}

export async function buscarHistoricoMotorista(
  motoristaId: string,
  inicio: string,
  fim: string,
): Promise<IndicadorMotoristaMes[]> {
  const { data } = await api.get<IndicadorMotoristaMes[]>(
    `/dashboard/motoristas/${motoristaId}/historico`,
    { params: { inicio, fim } },
  );
  return data;
}

export async function buscarTiposVeiculo(): Promise<TipoVeiculo[] | null> {
  const { data } = await api.get<TipoVeiculo[] | null>("/veiculos/categorias");
  return data ?? null;
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

// Linha de /dashboard/motoristas/situacao. A situacao e calculada no backend
// na consulta; aqui so exibimos. utilizacao e faixaUtilizacao vem
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
