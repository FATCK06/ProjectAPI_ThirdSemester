import { useEffect, useRef, useState } from "react";
import { Card } from "../../../../components/Card/Card";
import { type IndicadoresMes } from "../../../../services/Dashboard";
import "./IndicadoresCard.css";

interface PropsIndicadoresCard {
  indicadores: IndicadoresMes;
}

type IndicadorId =
  | "numeroViagens"
  | "diasDisponiveis"
  | "utilizacaoMedia"
  | "valorFrete"
  | "custoTotal"
  | "rentabilidade"
  | "rentabilidadeMediaViagem"
  | "margem";

type Categoria = "operacional" | "financeiro";

interface DefinicaoIndicador {
  id: IndicadorId;
  titulo: string;
  categoria: Categoria;
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

function formatarReais(valor: number): string {
  return valor.toLocaleString("pt-BR", {
    style: "currency",
    currency: "BRL",
  });
}

// Fora do componente: não muda entre renders. O valor só é calculado na hora de exibir.
const DEFINICOES: DefinicaoIndicador[] = [
  {
    id: "numeroViagens",
    titulo: "Nº de viagens",
    categoria: "operacional",
    obterValor: (i) => i.totais.numeroViagens,
  },
  {
    id: "diasDisponiveis",
    titulo: "Dias disponíveis",
    categoria: "operacional",
    obterValor: (i) => i.diasDisponiveis,
  },
  {
    id: "utilizacaoMedia",
    titulo: "% de utilização",
    categoria: "operacional",
    obterValor: (i) => `${i.totais.utilizacaoMedia}%`,
  },
  {
    id: "valorFrete",
    titulo: "Valor total dos fretes",
    categoria: "financeiro",
    obterValor: (i) => formatarReais(i.totais.valorFrete),
  },
  {
    id: "custoTotal",
    titulo: "Custo total",
    categoria: "financeiro",
    obterValor: (i) => formatarReais(i.totais.custoTotal),
  },
  {
    id: "rentabilidade",
    titulo: "Rentabilidade total",
    categoria: "financeiro",
    obterValor: (i) => formatarReais(i.totais.rentabilidade),
  },
  {
    id: "rentabilidadeMediaViagem",
    titulo: "Rentabilidade média/viagem",
    categoria: "financeiro",
    obterValor: (i) => formatarReais(i.totais.rentabilidadeMediaViagem),
  },
  {
    id: "margem",
    titulo: "Margem",
    categoria: "financeiro",
    obterValor: (i) => `${i.totais.margem}%`,
  },
];

export function IndicadoresCard({ indicadores }: PropsIndicadoresCard) {
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

  const indicadoresVisiveis = DEFINICOES.filter((d) =>
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
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
          <path
            d="M4 6h16M7 12h10M10 18h4"
            stroke="#64748b"
            strokeWidth="2"
            strokeLinecap="round"
          />
        </svg>
        Filtros
        <span className="indicadores-filtro-badge">{selecionados.length}</span>
      </button>

      {mostrarFiltro && (
        <div className="indicadores-menu">
          <div className="indicadores-menu-lista">
            {CATEGORIAS.map((categoria) => (
              <div key={categoria}>
                <div className="indicadores-menu-categoria">
                  {LABEL_CATEGORIA[categoria]}
                </div>

                {DEFINICOES.filter((d) => d.categoria === categoria).map(
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
              onClick={() => setSelecionados(DEFINICOES.map((d) => d.id))}
            >
              Selecionar todos
            </button>
          </div>
        </div>
      )}
    </div>
  );

  return (
    <Card titulo={`Indicadores de ${indicadores.mesReferencia}`} acoes={filtro}>
      {indicadoresVisiveis.length === 0 ? (
        <p className="indicadores-vazio">
          Nenhum indicador selecionado. Use os filtros para escolher o que
          exibir.
        </p>
      ) : (
        <div className="indicadores-grid">
          {indicadoresVisiveis.map((d) => (
            <div key={d.id} className="indicador-item">
              <div className="indicador-titulo">{d.titulo}</div>
              <div className="indicador-valor">{d.obterValor(indicadores)}</div>
            </div>
          ))}
        </div>
      )}
    </Card>
  );
}

export default IndicadoresCard;