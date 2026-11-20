// Ordem, categoria e escolhas do usuário no card de indicadores. Fica fora do
// card porque o Dashboard guarda a escolha (ela sobrevive à troca de mês, quando
// o card dá lugar ao skeleton) e o skeleton desenha exatamente os mesmos itens.

import type { HistoricoMes } from "../../../../services/Dashboard";
import { lerPreferencia } from "../../../../utils/preferencias";

export type IndicadorId =
  | "numeroViagens"
  | "diasOperacao"
  | "utilizacaoMedia"
  | "valorFrete"
  | "custoTotal"
  | "rentabilidade"
  | "rentabilidadeMediaViagem"
  | "margem";

// Também usado pelo Comparativo mensal, que mostra o mesmo indicador
export const AJUDA_UTILIZACAO =
  "Dias em operação ÷ (dias do mês × motoristas com viagem) × 100. Aproximado: conta " +
  "todos os dias do mês como disponíveis, pois o CSV não informa férias, afastamentos " +
  "nem manutenção (aguardando definição da Newe).";

export type Categoria = "operacional" | "financeiro";

interface MetaIndicador {
  id: IndicadorId;
  categoria: Categoria;
  // Ainda sem dado no CSV: mostra "-" e não tem comparação nem gráfico
  semDado?: boolean;
}

/** Na ordem em que aparecem no card. */
export const INDICADORES: MetaIndicador[] = [
  { id: "numeroViagens", categoria: "operacional" },
  { id: "diasOperacao", categoria: "operacional" },
  { id: "utilizacaoMedia", categoria: "operacional" },
  { id: "valorFrete", categoria: "financeiro" },
  { id: "custoTotal", categoria: "financeiro" },
  { id: "rentabilidade", categoria: "financeiro" },
  { id: "rentabilidadeMediaViagem", categoria: "financeiro" },
  { id: "margem", categoria: "financeiro" },
];

/**
 * O que o card mostra ao abrir o Dashboard. Quem vê dados financeiros (gestor e
 * administrador) começa com todos os indicadores; o operador, que não recebe a
 * categoria financeira, fica com os operacionais.
 */
export function selecaoInicial(financeiro: boolean): IndicadorId[] {
  return INDICADORES.filter((i) => financeiro || i.categoria !== "financeiro").map((i) => i.id);
}

/** Quantos meses antes do exibido entram na comparação. */
export type MesesComparacao = 1 | 2 | 3;

export const OPCOES_COMPARACAO: { valor: MesesComparacao; rotulo: string }[] = [
  { valor: 1, rotulo: "Mês anterior" },
  { valor: 2, rotulo: "Últimos 2 meses" },
  { valor: 3, rotulo: "Últimos 3 meses" },
];

/** O maior período do seletor: o histórico busca esse tanto de meses antes do exibido. */
export const MAXIMO_MESES_COMPARACAO = 3;

export const COMPARACAO_INICIAL: MesesComparacao = 1;

/** Só o mês anterior é um ponto contra outro: não há tendência para desenhar. */
export function temGrafico(meses: number): boolean {
  return meses > 1;
}

/**
 * Quantos meses seguidos antes do exibido têm viagens importadas (até o máximo
 * do seletor). Uma opção só vale se TODOS os meses dela tiverem dado: junho com
 * abril e maio importados libera 1 e 2 meses, mas não 3 (março vazio). Sem dado
 * no próprio mês exibido não há o que comparar: devolve 0, em vez de mostrar uma
 * queda de 100% que é só falta de importação.
 *
 * @param historico do mais antigo ao mês exibido (o último)
 */
export function mesesComDadosParaComparar(historico: HistoricoMes[]): number {
  const exibido = historico[historico.length - 1];
  if (!exibido || exibido.totais.motoristas === 0) return 0;

  let meses = 0;
  for (let i = historico.length - 2; i >= 0 && meses < MAXIMO_MESES_COMPARACAO; i--) {
    if (historico[i].totais.motoristas === 0) break;
    meses++;
  }
  return meses;
}

/** Os itens que o card mostra, na ordem dele: sem a categoria financeira para quem não pode vê-la. */
export function indicadoresVisiveis(selecionados: IndicadorId[], financeiro: boolean): MetaIndicador[] {
  return INDICADORES.filter(
    (i) => (financeiro || i.categoria !== "financeiro") && selecionados.includes(i.id),
  );
}

/** Nomes das preferências do card (ver utils/preferencias). */
export const PREFERENCIA_SELECIONADOS = "dashboard.indicadores";
export const PREFERENCIA_COMPARACAO = "dashboard.comparacao";

/**
 * A seleção que o usuário deixou da última vez, ou o padrão do perfil. Ids
 * desconhecidos (indicador removido numa versão nova) ficam de fora.
 */
export function selecaoSalva(financeiro: boolean): IndicadorId[] {
  const salvo = lerPreferencia(PREFERENCIA_SELECIONADOS);
  if (!Array.isArray(salvo)) return selecaoInicial(financeiro);
  const conhecidos = new Set<string>(INDICADORES.map((i) => i.id));
  return salvo.filter((id): id is IndicadorId => typeof id === "string" && conhecidos.has(id));
}

export function comparacaoSalva(): MesesComparacao {
  const salvo = lerPreferencia(PREFERENCIA_COMPARACAO);
  return OPCOES_COMPARACAO.some((o) => o.valor === salvo) ? (salvo as MesesComparacao) : COMPARACAO_INICIAL;
}
