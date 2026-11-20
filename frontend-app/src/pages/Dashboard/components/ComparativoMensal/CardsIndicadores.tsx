import {
  ArrowDown,
  ArrowUp,
  CircleDollarSign,
  CreditCard,
  Info,
  Percent,
  TrendingUp,
  Truck,
  type LucideIcon,
} from "lucide-react";
import type { SerieMes, TotaisSerie, VariacaoIndicador, VariacoesSerie } from "../../../../services/Dashboard";
import { podeVerDadosFinanceiros } from "../../../../services/auth";
import { formatarMesExtenso, formatarMesNumerico } from "../../../../utils/datas";
import { formatarMoeda, formatarMoedaCompacta, formatarPercentual, SEM_VALOR } from "../../../../utils/formatacao";
import { AJUDA_UTILIZACAO } from "../IndicadoresCard/selecao";
import { ValorAjustavel } from "../IndicadoresCard/ValorAjustavel";
// Mesmo visual dos cards do topo (indicador-item, indicador-variacao/data-sentido)
import "../IndicadoresCard/IndicadoresCard.css";

type Chave = keyof TotaisSerie & keyof VariacoesSerie;

interface DefinicaoCard {
  chave: Chave;
  titulo: string;
  tom: "verde" | "roxo" | "ciano" | "rosa";
  Icone: LucideIcon;
  maiorEhMelhor: boolean;
  financeiro?: boolean;
  formatar: (valor: number) => string;
  // Versão curta para quando o valor não cabe no card
  formatarCompacto?: (valor: number) => string;
  // Valor aproximado: ganha o * com este texto no tooltip
  aproximado?: string;
  // Diferença absoluta, mostrada quando não há percentual (anterior = 0)
  formatarDiferenca: (valor: number) => string;
}

function sinal(valor: number): string {
  return valor > 0 ? "+" : "";
}

function formatarNumero(valor: number): string {
  return valor.toLocaleString("pt-BR", { maximumFractionDigits: 1 });
}

// Cópia local do formatador do IndicadoresCard
function formatarVariacao(valor: number): string {
  const numero = valor.toLocaleString("pt-BR", {
    minimumFractionDigits: 1,
    maximumFractionDigits: 1,
  });
  return `${sinal(valor)}${numero}%`;
}

const CARDS: DefinicaoCard[] = [
  {
    chave: "numeroViagens",
    titulo: "Nº de viagens",
    tom: "verde",
    Icone: Truck,
    maiorEhMelhor: true,
    formatar: formatarNumero,
    formatarDiferenca: (v) => `${sinal(v)}${formatarNumero(v)}`,
  },
  {
    chave: "utilizacaoMedia",
    titulo: "% de utilização",
    tom: "roxo",
    Icone: Percent,
    maiorEhMelhor: true,
    formatar: formatarPercentual,
    aproximado: AJUDA_UTILIZACAO,
    // Diferença entre percentuais: pontos percentuais
    formatarDiferenca: (v) => `${sinal(v)}${v.toLocaleString("pt-BR", { maximumFractionDigits: 2 })} p.p.`,
  },
  {
    chave: "valorFrete",
    titulo: "Valor total dos fretes",
    tom: "ciano",
    Icone: CircleDollarSign,
    maiorEhMelhor: true,
    financeiro: true,
    formatar: formatarMoeda,
    formatarCompacto: formatarMoedaCompacta,
    formatarDiferenca: (v) => `${sinal(v)}${formatarMoeda(v)}`,
  },
  {
    chave: "custoTotal",
    titulo: "Custo total",
    tom: "rosa",
    Icone: CreditCard,
    maiorEhMelhor: false, // custo maior é ruim
    financeiro: true,
    formatar: formatarMoeda,
    formatarCompacto: formatarMoedaCompacta,
    formatarDiferenca: (v) => `${sinal(v)}${formatarMoeda(v)}`,
  },
  {
    chave: "rentabilidade",
    titulo: "Rentabilidade total",
    tom: "verde",
    Icone: TrendingUp,
    maiorEhMelhor: true,
    financeiro: true,
    formatar: formatarMoeda,
    formatarCompacto: formatarMoedaCompacta,
    formatarDiferenca: (v) => `${sinal(v)}${formatarMoeda(v)}`,
  },
];

function mensagemSemDados(mes: string): string {
  return `Sem dados para ${formatarMesNumerico(mes)}.`;
}

interface PropsVariacao {
  d: DefinicaoCard;
  variacao: VariacaoIndicador;
  mesAnterior: string;
}

function Variacao({ d, variacao, mesAnterior }: PropsVariacao) {
  // Sem percentual (anterior = 0): só a diferença absoluta, nunca 0%
  const temPercentual = variacao.percentual !== undefined && variacao.percentual !== null;
  const valor = temPercentual ? variacao.percentual! : variacao.diferenca;
  const sentido = valor === 0 ? "neutro" : valor > 0 === d.maiorEhMelhor ? "bom" : "ruim";

  return (
    <div className="indicador-comparacao">
      <span className="indicador-variacao" data-sentido={sentido}>
        {valor > 0 && <ArrowUp size={14} />}
        {valor < 0 && <ArrowDown size={14} />}
        {temPercentual ? formatarVariacao(valor) : d.formatarDiferenca(valor)}
      </span>
      <span className="indicador-anterior">
        vs. {formatarMesExtenso(mesAnterior)}
        {temPercentual && ` (${d.formatarDiferenca(variacao.diferenca)})`}
      </span>
    </div>
  );
}

interface PropsCardsIndicadores {
  // Em ordem crescente: o card usa o mês mais recente (o último)
  serie: SerieMes[];
}

export function CardsIndicadores({ serie }: PropsCardsIndicadores) {
  const mes = serie[serie.length - 1];
  if (!mes) return null;

  const financeiro = podeVerDadosFinanceiros();
  const cards = CARDS.filter((d) => !d.financeiro || financeiro);
  const mesAnterior = mes.mesAnterior;

  return (
    <div className="comparativo-cards">
      <p className="comparativo-cards-titulo">
        {formatarMesExtenso(mes.mesReferencia)}
        {mesAnterior && ` vs. ${formatarMesExtenso(mesAnterior)} (mês anterior)`}
      </p>
      <div className="comparativo-cards-grid">
        {cards.map((d) => {
          const valor = mes.totais?.[d.chave];
          const variacao = mes.variacoes?.[d.chave];

          let corpo;
          if (mes.semDados || !mes.totais) {
            // Mês selecionado sem importação: sem valor e sem seta
            corpo = <div className="indicador-anterior">{mensagemSemDados(mes.mesReferencia)}</div>;
          } else {
            let comparacao = null;
            if (valor !== undefined && valor !== null && mesAnterior) {
              comparacao =
                mes.mesAnteriorSemDados || !variacao ? (
                  <div className="indicador-anterior">{mensagemSemDados(mesAnterior)}</div>
                ) : (
                  <Variacao d={d} variacao={variacao} mesAnterior={mesAnterior} />
                );
            }
            corpo = (
              <>
                {valor === undefined || valor === null ? (
                  <div className="indicador-valor">{SEM_VALOR}</div>
                ) : (
                  <ValorAjustavel
                    completo={d.formatar(valor)}
                    compacto={d.formatarCompacto?.(valor)}
                    extra={
                      d.aproximado && (
                        <span className="indicador-asterisco" title={d.aproximado}>
                          *
                        </span>
                      )
                    }
                  />
                )}
                {comparacao}
              </>
            );
          }

          return (
            <div key={d.chave} className="indicador-item" data-tom={d.tom}>
              <span className="indicador-icone">
                <d.Icone size={22} />
              </span>
              <div className="indicador-corpo">
                <div className="indicador-titulo">
                  {d.titulo}
                  {d.aproximado && (
                    <span className="indicador-ajuda" title={d.aproximado} aria-label={d.aproximado} tabIndex={0}>
                      <Info size={13} />
                    </span>
                  )}
                </div>
                {corpo}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default CardsIndicadores;
