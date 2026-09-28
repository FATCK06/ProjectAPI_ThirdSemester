import { useEffect, useRef, useState } from "react";
import { type IndicadoresMes } from "../../../services/Dashboard";

interface PropsCard {
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

interface Indicador {
  id: IndicadorId;
  titulo: string;
  categoria: Categoria;
  valor: string | number;
}

const LABEL_CATEGORIA: Record<Categoria, string> = {
  operacional: "Operacional",
  financeiro: "Financeiro",
};

function formatarReais(valor: number): string {
  return valor.toLocaleString("pt-BR", {
    style: "currency",
    currency: "BRL",
  });
}

export function Card({ indicadores }: PropsCard) {
  const [mostrarFiltro, setMostrarFiltro] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  const [selecionados, setSelecionados] = useState<IndicadorId[]>([
    "numeroViagens",
    "diasDisponiveis",
    "utilizacaoMedia",
  ]);

  const listaIndicadores: Indicador[] = [
    {
      id: "numeroViagens",
      titulo: "Nº de viagens",
      categoria: "operacional",
      valor: indicadores.totais.numeroViagens,
    },
    {
      id: "diasDisponiveis",
      titulo: "Dias disponíveis",
      categoria: "operacional",
      valor: indicadores.diasDisponiveis,
    },
    {
      id: "utilizacaoMedia",
      titulo: "% de utilização",
      categoria: "operacional",
      valor: `${indicadores.totais.utilizacaoMedia}%`,
    },
    {
      id: "valorFrete",
      titulo: "Valor total dos fretes",
      categoria: "financeiro",
      valor: formatarReais(indicadores.totais.valorFrete),
    },
    {
      id: "custoTotal",
      titulo: "Custo total",
      categoria: "financeiro",
      valor: formatarReais(indicadores.totais.custoTotal),
    },
    {
      id: "rentabilidade",
      titulo: "Rentabilidade total",
      categoria: "financeiro",
      valor: formatarReais(indicadores.totais.rentabilidade),
    },
    {
      id: "rentabilidadeMediaViagem",
      titulo: "Rentabilidade média/viagem",
      categoria: "financeiro",
      valor: formatarReais(indicadores.totais.rentabilidadeMediaViagem),
    },
    {
      id: "margem",
      titulo: "Margem",
      categoria: "financeiro",
      valor: `${indicadores.totais.margem}%`,
    },
  ];

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
    setSelecionados((atual) => {
      if (atual.includes(id)) {
        return atual.filter((item) => item !== id);
      }

      return [...atual, id];
    });
  }

  function selecionarTodos() {
    setSelecionados(listaIndicadores.map((indicador) => indicador.id));
  }

  function limparSelecao() {
    setSelecionados([]);
  }

  const indicadoresVisiveis = listaIndicadores.filter((indicador) =>
    selecionados.includes(indicador.id),
  );

  const categorias: Categoria[] = ["operacional", "financeiro"];

  return (
    <div className="card-indicadores">
      <div className="card-header">
        <h2>Indicadores de {indicadores.mesReferencia}</h2>

        <div ref={containerRef} style={{ position: "relative" }}>
          <button
            type="button"
            onClick={() => setMostrarFiltro((atual) => !atual)}
            aria-expanded={mostrarFiltro}
            style={{
              display: "flex",
              alignItems: "center",
              gap: "6px",
              border: "1px solid #d6d6d6",
              borderRadius: "8px",
              padding: "8px 12px",
              backgroundColor: mostrarFiltro ? "#f5f5f5" : "#fff",
              color: "#333",
              fontSize: "14px",
              cursor: "pointer",
              transition: "background-color 0.15s ease",
            }}
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <path
                d="M4 6h16M7 12h10M10 18h4"
                stroke="#555"
                strokeWidth="2"
                strokeLinecap="round"
              />
            </svg>
            Filtros
            <span
              style={{
                minWidth: "18px",
                height: "18px",
                padding: "0 5px",
                borderRadius: "9px",
                backgroundColor: "#333",
                color: "#fff",
                fontSize: "11px",
                lineHeight: "18px",
                textAlign: "center",
              }}
            >
              {selecionados.length}
            </span>
          </button>

          {mostrarFiltro && (
            <div
              role="menu"
              style={{
                position: "absolute",
                top: "44px",
                right: 0,
                zIndex: 10,
                width: "270px",
                backgroundColor: "#fff",
                border: "1px solid #e2e2e2",
                borderRadius: "10px",
                boxShadow: "0 8px 24px rgba(0,0,0,0.12)",
                overflow: "hidden",
              }}
            >
              <div
                style={{
                  maxHeight: "320px",
                  overflowY: "auto",
                  padding: "8px",
                }}
              >
                {categorias.map((categoria, indice) => (
                  <div key={categoria}>
                    <div
                      style={{
                        padding: "8px 8px 4px",
                        fontSize: "12px",
                        color: "#8a8a8a",
                        marginTop: indice > 0 ? "4px" : 0,
                      }}
                    >
                      {LABEL_CATEGORIA[categoria]}
                    </div>

                    {listaIndicadores
                      .filter((indicador) => indicador.categoria === categoria)
                      .map((indicador) => {
                        const ativo = selecionados.includes(indicador.id);

                        return (
                          <label
                            key={indicador.id}
                            style={{
                              display: "flex",
                              alignItems: "center",
                              gap: "10px",
                              padding: "8px",
                              borderRadius: "6px",
                              cursor: "pointer",
                              backgroundColor: "transparent",
                            }}
                            onMouseEnter={(e) =>
                              (e.currentTarget.style.backgroundColor =
                                "#f7f7f7")
                            }
                            onMouseLeave={(e) =>
                              (e.currentTarget.style.backgroundColor =
                                "transparent")
                            }
                          >
                            <span
                              style={{
                                width: "16px",
                                height: "16px",
                                flexShrink: 0,
                                borderRadius: "4px",
                                border: ativo ? "none" : "1px solid #c7c7c7",
                                backgroundColor: ativo ? "#333" : "#fff",
                                display: "flex",
                                alignItems: "center",
                                justifyContent: "center",
                                transition: "background-color 0.1s ease",
                              }}
                            >
                              {ativo && (
                                <svg
                                  width="10"
                                  height="10"
                                  viewBox="0 0 24 24"
                                  fill="none"
                                >
                                  <path
                                    d="M20 6L9 17l-5-5"
                                    stroke="#fff"
                                    strokeWidth="3"
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                  />
                                </svg>
                              )}
                            </span>

                            <input
                              type="checkbox"
                              checked={ativo}
                              onChange={() => alternarIndicador(indicador.id)}
                              style={{
                                position: "absolute",
                                opacity: 0,
                                width: 0,
                                height: 0,
                              }}
                            />

                            <span style={{ fontSize: "14px", color: "#333" }}>
                              {indicador.titulo}
                            </span>
                          </label>
                        );
                      })}
                  </div>
                ))}
              </div>

              <div
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  padding: "8px 12px",
                  borderTop: "1px solid #eee",
                }}
              >
                <button
                  type="button"
                  onClick={limparSelecao}
                  style={{
                    border: "none",
                    background: "none",
                    color: "#777",
                    fontSize: "13px",
                    cursor: "pointer",
                    padding: "4px",
                  }}
                >
                  Limpar
                </button>

                <button
                  type="button"
                  onClick={selecionarTodos}
                  style={{
                    border: "none",
                    background: "none",
                    color: "#333",
                    fontWeight: 600,
                    fontSize: "13px",
                    cursor: "pointer",
                    padding: "4px",
                  }}
                >
                  Selecionar todos
                </button>
              </div>
            </div>
          )}
        </div>
      </div>

      <div className="card-indicadores-grid">
        {indicadoresVisiveis.length === 0 ? (
          <p style={{ color: "#999", fontSize: "14px" }}>
            Nenhum indicador selecionado. Use os filtros para escolher o que
            exibir.
          </p>
        ) : (
          indicadoresVisiveis.map((indicador) => (
            <div key={indicador.id}>
              <strong>{indicador.titulo}</strong>
              <p>{indicador.valor}</p>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default Card;
