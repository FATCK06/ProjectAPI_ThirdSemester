import { useEffect, useRef, useState } from "react";
import {
  Activity,
  ArrowDown,
  ArrowUp,
  BadgePercent,
  CalendarDays,
  CircleDollarSign,
  CreditCard,
  ListFilter,
  Percent,
  TrendingUp,
  Truck,
  type LucideIcon,
} from "lucide-react";
import { Card } from "../../../../components/Card/Card";
import { type IndicadoresMes } from "../../../../services/Dashboard";
import { podeVerDadosFinanceiros } from "../../../../services/auth";
import { formatarMoeda, formatarPercentual } from "../../../../utils/formatacao";
import { Sparkline } from "./Sparkline";
import "./IndicadoresCard.css";

interface PropsIndicadoresCard {
  indicadores: IndicadoresMes;
}

export type IndicadorId =
  | "numeroViagens"
  | "diasDisponiveis"
  | "utilizacaoMedia"
  | "valorFrete"
  | "custoTotal"
  | "rentabilidade"
  | "rentabilidadeMediaViagem"
  | "margem";

type Categoria = "operacional" | "financeiro";
type Tom = "verde" | "azul" | "roxo" | "ambar" | "rosa" | "ciano";

export interface ComparativoIndicador {
  variacaoPercentual: number | null;
  rotuloAnterior: string;
  serie?: number[];
}

interface PropsIndicadoresCard {
  indicadores: IndicadoresMes;
  comparativo?: Partial<Record<IndicadorId, ComparativoIndicador>>;
}

interface DefinicaoIndicador {
  id: IndicadorId;
  titulo: string;
  categoria: Categoria;
  tom: Tom;
  Icone: LucideIcon;
  maiorEhMelhor: boolean;
  obterValor: (indicadores: IndicadoresMes) => string | number;
}

const LABEL_CATEGORIA: Record<Categoria, string> = {
  operacional: "Operacional",
  financeiro: "Financeiro",
};

const CATEGORIAS: Categoria[] = ["operacional", "financeiro"];

const SELECAO_INICIAL: IndicadorId[] = [
  "numeroViagens",
  "diasDisponiveis",
  "utilizacaoMedia",
];

function formatarMes(mesReferencia: string): string {
  const [ano, mes] = mesReferencia.split("-").map(Number);
  if (!ano || !mes) return mesReferencia;
  return new Date(ano, mes - 1, 1).toLocaleDateString("pt-BR", {
    month: "long",
    year: "numeric",
  });
}

function formatarVariacao(valor: number): string {
  const numero = valor.toLocaleString("pt-BR", {
    minimumFractionDigits: 1,
    maximumFractionDigits: 1,
  });
  return `${valor > 0 ? "+" : ""}${numero}%`;
}

// Fora do componente: não muda entre renders. O valor só é calculado na hora de exibir.
const DEFINICOES: DefinicaoIndicador[] = [
  {
    id: "numeroViagens",
    titulo: "Nº de viagens",
    categoria: "operacional",
    tom: "verde",
    Icone: Truck,
    maiorEhMelhor: true,
    obterValor: (i) => i.totais.numeroViagens,
  },
  {
    id: "diasDisponiveis",
    titulo: "Dias disponíveis",
    categoria: "operacional",
    tom: "azul",
    Icone: CalendarDays,
    maiorEhMelhor: true,
    obterValor: (i) => i.diasDisponiveis,
  },
  {
    id: "utilizacaoMedia",
    titulo: "% de utilização",
    categoria: "operacional",
    tom: "roxo",
    Icone: Percent,
    maiorEhMelhor: true,
    obterValor: (i) => formatarPercentual(i.totais.utilizacaoMedia),
  },
  {
    id: "valorFrete",
    titulo: "Valor total dos fretes",
    categoria: "financeiro",
    tom: "ciano",
    Icone: CircleDollarSign,
    maiorEhMelhor: true,
    obterValor: (i) => formatarMoeda(i.totais.valorFrete),
  },
  {
    id: "custoTotal",
    titulo: "Custo total",
    categoria: "financeiro",
    tom: "rosa",
    Icone: CreditCard,
    maiorEhMelhor: false, // custo maior é ruim
    obterValor: (i) => formatarMoeda(i.totais.custoTotal),
  },
  {
    id: "rentabilidade",
    titulo: "Rentabilidade total",
    categoria: "financeiro",
    tom: "verde",
    Icone: TrendingUp,
    maiorEhMelhor: true,
    obterValor: (i) => formatarMoeda(i.totais.rentabilidade),
  },
  {
    id: "rentabilidadeMediaViagem",
    titulo: "Rentabilidade média/viagem",
    categoria: "financeiro",
    tom: "ambar",
    Icone: Activity,
    maiorEhMelhor: true,
    obterValor: (i) => formatarMoeda(i.totais.rentabilidadeMediaViagem),
  },
  {
    id: "margem",
    titulo: "Margem",
    categoria: "financeiro",
    tom: "azul",
    Icone: BadgePercent,
    maiorEhMelhor: true,
    obterValor: (i) => formatarPercentual(i.totais.margem),
  },
];


interface PropsVariacao {
  comparativo: ComparativoIndicador;
  maiorEhMelhor: boolean;
}

function VariacaoIndicador({ comparativo, maiorEhMelhor }: PropsVariacao) {
  const { variacaoPercentual, rotuloAnterior } = comparativo;

  if (variacaoPercentual === null) {
    return <div className="indicador-anterior">{rotuloAnterior}</div>;
  }

  const sentido =
    variacaoPercentual === 0
      ? "neutro"
      : variacaoPercentual > 0 === maiorEhMelhor
        ? "bom"
        : "ruim";

  return (
    <>
      <div className="indicador-variacao" data-sentido={sentido}>
        {variacaoPercentual > 0 && <ArrowUp size={14} />}
        {variacaoPercentual < 0 && <ArrowDown size={14} />}
        {formatarVariacao(variacaoPercentual)}
      </div>
      <div className="indicador-anterior">{rotuloAnterior}</div>
    </>
  );
}

export function IndicadoresCard({
  indicadores,
  comparativo,
}: PropsIndicadoresCard) {
  // Quem não é gestor nem vê a categoria financeira no filtro: a API não manda os valores.
  const categorias = podeVerDadosFinanceiros()
    ? CATEGORIAS
    : CATEGORIAS.filter((c) => c !== "financeiro");
  const definicoes = DEFINICOES.filter((d) => categorias.includes(d.categoria));
  const [mostrarFiltro, setMostrarFiltro] = useState(false);
  const [selecionados, setSelecionados] =
    useState<IndicadorId[]>(SELECAO_INICIAL);
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!mostrarFiltro) return;

    function aoClicarFora(evento: MouseEvent) {
      if (
        containerRef.current &&
        !containerRef.current.contains(evento.target as Node)
      ) {
        setMostrarFiltro(false);
      }
    }

    function aoPressionarTecla(evento: KeyboardEvent) {
      if (evento.key === "Escape") setMostrarFiltro(false);
    }

    document.addEventListener("mousedown", aoClicarFora);
    document.addEventListener("keydown", aoPressionarTecla);

    return () => {
      document.removeEventListener("mousedown", aoClicarFora);
      document.removeEventListener("keydown", aoPressionarTecla);
    };
  }, [mostrarFiltro]);

  function alternarIndicador(id: IndicadorId) {
    setSelecionados((atual) =>
      atual.includes(id) ? atual.filter((item) => item !== id) : [...atual, id],
    );
  }

  const indicadoresVisiveis = definicoes.filter((d) =>
    selecionados.includes(d.id),
  );

  const filtro = (
    <div ref={containerRef} className="indicadores-filtro">
      <button
        type="button"
        className="indicadores-filtro-btn"
        onClick={() => setMostrarFiltro((atual) => !atual)}
        aria-expanded={mostrarFiltro}
      >
        <ListFilter size={14} color="#64748b" />
        Filtros
        <span className="indicadores-filtro-badge">{selecionados.length}</span>
      </button>

      {mostrarFiltro && (
        <div className="indicadores-menu">
          <div className="indicadores-menu-lista">
            {categorias.map((categoria) => (
              <div key={categoria}>
                <div className="indicadores-menu-categoria">
                  {LABEL_CATEGORIA[categoria]}
                </div>

                {definicoes.filter((d) => d.categoria === categoria).map(
                  (d) => (
                    <label key={d.id} className="indicadores-menu-item">
                      <input
                        type="checkbox"
                        checked={selecionados.includes(d.id)}
                        onChange={() => alternarIndicador(d.id)}
                      />
                      {d.titulo}
                    </label>
                  ),
                )}
              </div>
            ))}
          </div>

          <div className="indicadores-menu-rodape">
            <button type="button" onClick={() => setSelecionados([])}>
              Limpar
            </button>
            <button
              type="button"
              onClick={() => setSelecionados(definicoes.map((d) => d.id))}
            >
              Selecionar todos
            </button>
          </div>
        </div>
      )}
    </div>
  );

  return (
    <Card
      sobretitulo="Visão geral"
      titulo={`Indicadores de ${formatarMes(indicadores.mesReferencia)}`}
      subtitulo="Acompanhe o desempenho dos principais indicadores no período selecionado."
      acoes={filtro}
    >
      {indicadoresVisiveis.length === 0 ? (
        <p className="indicadores-vazio">
          Nenhum indicador selecionado. Use os filtros para escolher o que
          exibir.
        </p>
      ) : (
        <div className="indicadores-grid">
          {indicadoresVisiveis.map((d) => {
            const comp = comparativo?.[d.id];

            return (
              <div key={d.id} className="indicador-item" data-tom={d.tom}>
                <span className="indicador-icone">
                  <d.Icone size={22} />
                </span>

                <div className="indicador-corpo">
                  <div className="indicador-titulo">{d.titulo}</div>
                  <div className="indicador-valor">
                    {d.obterValor(indicadores)}
                  </div>
                  {comp && (
                    <VariacaoIndicador
                      comparativo={comp}
                      maiorEhMelhor={d.maiorEhMelhor}
                    />
                  )}
                </div>

                {comp?.serie && <Sparkline valores={comp.serie} />}
              </div>
            );
          })}
        </div>
      )}
    </Card>
  );
}

export default IndicadoresCard;