import { useState, type MouseEvent, type ReactNode } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ArrowDown, ArrowUp, ArrowUpDown } from "lucide-react";
import { Paginacao } from "../Paginacao";
import "./DataTable.css";

export type DirecaoOrdenacao = "asc" | "desc";

export interface OrdenacaoTabela {
  chave: string;
  direcao: DirecaoOrdenacao;
}

export interface EstadoNavegacaoTabela {
  pagina: number;
  ordenacao: OrdenacaoTabela | null;
}

export interface ColunaTabela<T> {
  chave: string;
  titulo: string;
  render: (linha: T) => ReactNode;
  className?: string; // aplicada no <td> da coluna
  alinhamento?: "esquerda" | "direita";
  largura?: number | string;
  // Com esta função a coluna passa a ser ordenável pelo valor que ela devolve
  ordenarPor?: (linha: T) => string | number | null | undefined;
}

interface PropsDataTable<T> {
  colunas: ColunaTabela<T>[];
  linhas: T[];
  chaveLinha: (linha: T) => string;
  alturaMaxima?: number | string;
  // Larguras fixas por coluna e textos longos cortados com reticências
  layoutFixo?: boolean;
  larguraMinima?: number | string;
  // A primeira célula (o nome) vira um link para este endereço; o resto da linha não navega
  linkLinha?: (linha: T) => string;
  estadoNavegacaoLinha?: (linha: T, estado: EstadoNavegacaoTabela) => unknown;
  // Abaixo de 900px troca a tabela por cards (título = primeira coluna)
  cardsNoMobile?: boolean;
  mensagemVazia?: ReactNode;
  ordenacaoInicial?: OrdenacaoTabela | null;
  // Modo controlado: use quando a ordenação precisa sobreviver à tabela ser desmontada
  ordenacao?: OrdenacaoTabela | null;
  aoOrdenar?: (ordenacao: OrdenacaoTabela) => void;
  // Paginação no front, depois da ordenação; para listas que já vêm paginadas do backend use <Paginacao />
  itensPorPagina?: number;
  paginaInicial?: number;
}

// Nulos vão sempre para o fim, independente da direção
function compararValores(
  valorA: string | number | null | undefined,
  valorB: string | number | null | undefined,
  direcao: DirecaoOrdenacao,
) {
  if (valorA === null || valorA === undefined) {
    return valorB === null || valorB === undefined ? 0 : 1;
  }
  if (valorB === null || valorB === undefined) return -1;

  const comparacao = typeof valorA === "number" && typeof valorB === "number"
    ? valorA - valorB
    : String(valorA).localeCompare(String(valorB), "pt-BR", {
      numeric: true,
      sensitivity: "base",
    });
  return direcao === "asc" ? comparacao : -comparacao;
}

export function DataTable<T>({
  colunas,
  linhas,
  chaveLinha,
  alturaMaxima,
  layoutFixo = false,
  larguraMinima,
  linkLinha,
  estadoNavegacaoLinha,
  cardsNoMobile = false,
  mensagemVazia,
  ordenacaoInicial = null,
  ordenacao: ordenacaoControlada,
  aoOrdenar,
  itensPorPagina,
  paginaInicial = 1,
}: PropsDataTable<T>) {
  const navigate = useNavigate();
  const [ordenacaoInterna, setOrdenacaoInterna] = useState(ordenacaoInicial);
  const [paginaEscolhida, setPaginaEscolhida] = useState(paginaInicial);
  const ordenacao = ordenacaoControlada !== undefined ? ordenacaoControlada : ordenacaoInterna;
  const colunaOrdenada = colunas.find((coluna) => coluna.chave === ordenacao?.chave);

  const linhasOrdenadas = ordenacao && colunaOrdenada?.ordenarPor
    ? [...linhas].sort((a, b) => compararValores(
      colunaOrdenada.ordenarPor!(a),
      colunaOrdenada.ordenarPor!(b),
      ordenacao.direcao,
    ))
    : linhas;

  // Se a lista diminuir, a página escolhida pode deixar de existir: fica na última
  const totalPaginas = itensPorPagina ? Math.max(Math.ceil(linhas.length / itensPorPagina), 1) : 1;
  const pagina = Math.min(paginaEscolhida, totalPaginas);
  const inicio = itensPorPagina ? (pagina - 1) * itensPorPagina : 0;
  const linhasVisiveis = itensPorPagina
    ? linhasOrdenadas.slice(inicio, inicio + itensPorPagina)
    : linhasOrdenadas;

  function alternarOrdenacao(chave: string) {
    const proxima: OrdenacaoTabela = {
      chave,
      direcao: ordenacao?.chave === chave && ordenacao.direcao === "asc" ? "desc" : "asc",
    };
    if (ordenacaoControlada === undefined) setOrdenacaoInterna(proxima);
    aoOrdenar?.(proxima);
    setPaginaEscolhida(1);
  }

  function iconeOrdenacao(chave: string) {
    if (ordenacao?.chave !== chave) return <ArrowUpDown size={14} aria-hidden="true" />;
    return ordenacao.direcao === "asc"
      ? <ArrowUp size={14} aria-hidden="true" />
      : <ArrowDown size={14} aria-hidden="true" />;
  }

  // O estado vai junto da navegação e é montado na hora do clique, não no render:
  // assim leva a rolagem (window.scrollY) do momento em que a pessoa saiu.
  // Ctrl/Cmd/Shift/botão do meio seguem o padrão do navegador (outra aba).
  function aoClicarLink(evento: MouseEvent<HTMLAnchorElement>, linha: T, destino: string) {
    if (evento.button !== 0 || evento.metaKey || evento.ctrlKey || evento.shiftKey || evento.altKey) return;
    evento.preventDefault();
    navigate(destino, { state: estadoNavegacaoLinha?.(linha, { pagina, ordenacao }) });
  }

  function classeAlinhamento(coluna: ColunaTabela<T>) {
    return coluna.alinhamento === "direita" ? "data-table-direita" : undefined;
  }

  if (linhas.length === 0 && mensagemVazia) {
    return <div className="data-table-vazio" role="status">{mensagemVazia}</div>;
  }

  return (
    <>
      <div
        className={`data-table-wrap${cardsNoMobile ? " data-table-wrap--cards-mobile" : ""}`}
        style={{ maxHeight: alturaMaxima }}
      >
        <table
          className={`data-table${layoutFixo ? " data-table--fixo" : ""}`}
          style={{ minWidth: larguraMinima }}
        >
          {colunas.some((coluna) => coluna.largura !== undefined) && (
            <colgroup>
              {colunas.map((coluna) => (
                <col key={coluna.chave} style={{ width: coluna.largura }} />
              ))}
            </colgroup>
          )}
          <thead>
            <tr>
              {colunas.map((coluna) => {
                if (!coluna.ordenarPor) {
                  return (
                    <th key={coluna.chave} scope="col" className={classeAlinhamento(coluna)}>
                      <span className="data-table-titulo">{coluna.titulo}</span>
                    </th>
                  );
                }
                const ativa = ordenacao?.chave === coluna.chave;
                return (
                  <th
                    key={coluna.chave}
                    scope="col"
                    className={classeAlinhamento(coluna)}
                    aria-sort={ativa
                      ? ordenacao.direcao === "asc" ? "ascending" : "descending"
                      : "none"}
                  >
                    <button
                      type="button"
                      className="data-table-titulo data-table-ordenar"
                      onClick={() => alternarOrdenacao(coluna.chave)}
                      aria-label={`Ordenar por ${coluna.titulo}${
                        ativa ? ordenacao.direcao === "asc" ? ", crescente" : ", decrescente" : ""
                      }`}
                    >
                      {coluna.titulo}
                      {iconeOrdenacao(coluna.chave)}
                    </button>
                  </th>
                );
              })}
            </tr>
          </thead>
          <tbody>
            {linhasVisiveis.map((linha) => {
              const destino = linkLinha?.(linha);
              return (
                <tr key={chaveLinha(linha)}>
                  {colunas.map((coluna, indice) => (
                    <td
                      key={coluna.chave}
                      className={[coluna.className, classeAlinhamento(coluna)].filter(Boolean).join(" ") || undefined}
                    >
                      {destino && indice === 0 ? (
                        <Link
                          to={destino}
                          className="data-table-link"
                          onClick={(evento) => aoClicarLink(evento, linha, destino)}
                        >
                          {coluna.render(linha)}
                        </Link>
                      ) : coluna.render(linha)}
                    </td>
                  ))}
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {cardsNoMobile && (
        <div className="data-table-cards">
          {linhasVisiveis.map((linha) => {
            const destino = linkLinha?.(linha);
            const [primeira, ...demais] = colunas;
            // Só o nome é link, como na tabela; o card em si não navega
            return (
              <div key={chaveLinha(linha)} className="data-table-card">
                <span className="data-table-card-titulo">
                  {destino ? (
                    <Link
                      to={destino}
                      className="data-table-link"
                      onClick={(evento) => aoClicarLink(evento, linha, destino)}
                    >
                      {primeira.render(linha)}
                      <span aria-hidden="true"> ›</span>
                    </Link>
                  ) : primeira.render(linha)}
                </span>
                <span className="data-table-card-campos">
                  {demais.map((coluna) => (
                    <span className="data-table-card-campo" key={coluna.chave}>
                      <span>{coluna.titulo}</span>
                      <strong>{coluna.render(linha)}</strong>
                    </span>
                  ))}
                </span>
              </div>
            );
          })}
        </div>
      )}

      {itensPorPagina ? (
        <Paginacao
          pagina={pagina}
          totalPaginas={totalPaginas}
          aoMudar={setPaginaEscolhida}
          contagem={`${inicio + 1}–${inicio + linhasVisiveis.length} de ${linhas.length.toLocaleString("pt-BR")}`}
        />
      ) : null}
    </>
  );
}

export default DataTable;
