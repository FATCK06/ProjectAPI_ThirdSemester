import api from "./api";

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
