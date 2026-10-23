import { useEffect, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { ArrowDown, ArrowUp, ArrowUpDown } from "lucide-react";
import { TableSkeleton } from "../../../components/SkeletonLoader/TableSkeleton";
import {
  buscarMotoristasMes,
  buscarTiposVeiculo,
  type MotoristasMesPage,
  type MotoristaMes,
  type TipoVeiculo,
} from "../../../services/Motoristas";
import { mesAtual } from "../../../utils/datas";
import { formatarPercentual } from "../../../utils/formatacao";
import "./motoristas.css";

type Status = "loading" | "success" | "error";
type ResultadoBusca =
  | { chave: string; status: "success"; dados: MotoristasMesPage }
  | { chave: string; status: "error" };
type EstadoTiposVeiculo =
  | { status: "loading" }
  | { status: "success"; tipos: TipoVeiculo[] | null }
  | { status: "error" };
type ColunaOrdenacao = keyof Pick<
  MotoristaMes,
  | "nomeMotorista"
  | "tipoVeiculo"
  | "modelo"
  | "placa"
  | "viagensNoMes"
  | "diasUtilizados"
  | "disponibilidade"
  | "utilizacao"
  | "percentualEfetividade"
>;
type DirecaoOrdenacao = "asc" | "desc";

const COLUNAS_ORDENACAO: { chave: ColunaOrdenacao; rotulo: string }[] = [
  { chave: "nomeMotorista", rotulo: "Motorista" },
  { chave: "tipoVeiculo", rotulo: "Tipo de veículo" },
  { chave: "modelo", rotulo: "Modelo" },
  { chave: "placa", rotulo: "Placa" },
  { chave: "viagensNoMes", rotulo: "Viagens" },
  { chave: "diasUtilizados", rotulo: "Dias em operação" },
  { chave: "disponibilidade", rotulo: "Disponibilidade" },
  { chave: "utilizacao", rotulo: "Utilização" },
  { chave: "percentualEfetividade", rotulo: "Efetividade" },
];

function paginaDaUrl(valor: string | null) {
  if (!valor || !/^\d+$/.test(valor)) return 1;
  const pagina = Number(valor);
  return Number.isSafeInteger(pagina) && pagina > 0 && pagina <= 2_147_483_647
    ? pagina
    : 1;
}

function normalizarBusca(valor: string) {
  return valor
    .normalize("NFD")
    .replace(/\p{Diacritic}/gu, "")
    .toLocaleLowerCase("pt-BR")
    .trim();
}

function compararMotoristas(
  primeiro: MotoristaMes,
  segundo: MotoristaMes,
  coluna: ColunaOrdenacao,
  direcao: DirecaoOrdenacao,
) {
  const valorA = primeiro[coluna];
  const valorB = segundo[coluna];
  if (valorA === null) return valorB === null ? 0 : 1;
  if (valorB === null) return -1;

  const comparacao = typeof valorA === "number" && typeof valorB === "number"
    ? valorA - valorB
    : String(valorA).localeCompare(String(valorB), "pt-BR", {
      numeric: true,
      sensitivity: "base",
    });
  return direcao === "asc" ? comparacao : -comparacao;
}

function valoresMotorista(motorista: MotoristaMes) {
  return [
    { rotulo: "Tipo de veículo", valor: motorista.tipoVeiculo },
    { rotulo: "Modelo", valor: motorista.modelo?.trim() || "Não informado" },
    { rotulo: "Placa", valor: motorista.placa },
    { rotulo: "Viagens no mês", valor: String(motorista.viagensNoMes) },
    { rotulo: "Dias em operação", valor: String(motorista.diasUtilizados) },
    { rotulo: "Disponibilidade", valor: formatarPercentual(motorista.disponibilidade) },
    { rotulo: "Utilização", valor: formatarPercentual(motorista.utilizacao) },
    { rotulo: "Efetividade", valor: formatarPercentual(motorista.percentualEfetividade) },
  ];
}

export function Motoristas() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const mesParametro = searchParams.get("mes");
  const mes = mesParametro && /^\d{4}-(0[1-9]|1[0-2])$/.test(mesParametro)
    ? mesParametro
    : mesAtual();
  const paginaParametro = searchParams.get("page");
  const pagina = paginaDaUrl(paginaParametro);
  const [busca, setBusca] = useState("");
  const [tipoVeiculo, setTipoVeiculo] = useState("");
  const [estadoTiposVeiculo, setEstadoTiposVeiculo] =
    useState<EstadoTiposVeiculo>({ status: "loading" });
  const [ordenacao, setOrdenacao] = useState<{
    coluna: ColunaOrdenacao;
    direcao: DirecaoOrdenacao;
  } | null>(null);
  const [resultado, setResultado] = useState<ResultadoBusca | null>(null);
  const [tentativa, setTentativa] = useState(0);
  const chaveConsulta = `${mes}|${pagina}|${tentativa}`;
  const status: Status = resultado?.chave === chaveConsulta
    ? resultado.status
    : "loading";
  const dados = resultado?.chave === chaveConsulta && resultado.status === "success"
    ? resultado.dados
    : null;
  const termo = normalizarBusca(busca);
  const placaTermo = termo.replace(/[^a-z0-9]/g, "");
  const tiposVeiculo = estadoTiposVeiculo.status === "success"
    ? Array.from(
      new Map((estadoTiposVeiculo.tipos ?? []).map((tipo) => [tipo.descricao, tipo])).values(),
    )
    : [];
  const semTiposVeiculo = estadoTiposVeiculo.status === "success" && tiposVeiculo.length === 0;
  const motoristasFiltrados = dados?.content.filter((motorista) => {
    const nome = normalizarBusca(motorista.nomeMotorista);
    const placa = motorista.placa.toLocaleLowerCase("pt-BR").replace(/[^a-z0-9]/g, "");
    const correspondeBusca = nome.includes(termo) || (placaTermo !== "" && placa.includes(placaTermo));
    return correspondeBusca && (!tipoVeiculo || motorista.tipoVeiculo === tipoVeiculo);
  }) ?? [];
  const motoristasOrdenados = ordenacao
    ? [...motoristasFiltrados].sort((a, b) =>
      compararMotoristas(a, b, ordenacao.coluna, ordenacao.direcao))
    : motoristasFiltrados;

  function alternarOrdenacao(coluna: ColunaOrdenacao) {
    setOrdenacao((atual) => ({
      coluna,
      direcao: atual?.coluna === coluna && atual.direcao === "asc" ? "desc" : "asc",
    }));
  }

  function iconeOrdenacao(coluna: ColunaOrdenacao) {
    if (ordenacao?.coluna !== coluna) return <ArrowUpDown size={14} aria-hidden="true" />;
    return ordenacao.direcao === "asc"
      ? <ArrowUp size={14} aria-hidden="true" />
      : <ArrowDown size={14} aria-hidden="true" />;
  }

  useEffect(() => {
    if (mesParametro !== mes || paginaParametro !== String(pagina)) {
      setSearchParams((atual) => {
        const normalizados = new URLSearchParams(atual);
        normalizados.set("mes", mes);
        normalizados.set("page", String(pagina));
        return normalizados;
      }, { replace: true });
    }
  }, [mes, mesParametro, pagina, paginaParametro, setSearchParams]);

  useEffect(() => {
    let cancelado = false;

    buscarMotoristasMes(mes, pagina)
      .then((resultado) => {
        if (cancelado) return;
        if (pagina > Math.max(resultado.totalPages, 1)) {
          setSearchParams((atual) => {
            const normalizados = new URLSearchParams(atual);
            normalizados.set("page", String(Math.max(resultado.totalPages, 1)));
            return normalizados;
          }, { replace: true });
          return;
        }
        setResultado({ chave: chaveConsulta, status: "success", dados: resultado });
      })
      .catch(() => {
        if (cancelado) return;
        setResultado({ chave: chaveConsulta, status: "error" });
      });

    return () => {
      cancelado = true;
    };
  }, [chaveConsulta, mes, pagina, setSearchParams]);

  useEffect(() => {
    let cancelado = false;

    buscarTiposVeiculo()
      .then((tipos) => {
        if (!cancelado) setEstadoTiposVeiculo({ status: "success", tipos });
      })
      .catch(() => {
        if (!cancelado) setEstadoTiposVeiculo({ status: "error" });
      });

    return () => {
      cancelado = true;
    };
  }, []);

  function atualizarConsulta(proximaMes: string, proximaPagina: number) {
    setSearchParams((atual) => {
      const parametros = new URLSearchParams(atual);
      parametros.set("mes", proximaMes);
      parametros.set("page", String(proximaPagina));
      return parametros;
    });
  }

  return (
    <section className="motoristas-card">
      <div className="motoristas-header">
        <div>
          <h1 className="motoristas-title">Motoristas</h1>
          <p className="motoristas-subtitle">
            Viagens e indicadores operacionais por mês
          </p>
        </div>

        <div className="motoristas-filtros">
          <div className="motoristas-filtro">
            <label htmlFor="motoristas-busca">Buscar motorista ou placa</label>
            <input
              id="motoristas-busca"
              type="search"
              placeholder="Nome ou placa"
              value={busca}
              onChange={(event) => setBusca(event.target.value)}
            />
          </div>
          <div className="motoristas-filtro">
            <label htmlFor="motoristas-tipo-veiculo">Tipo de veículo</label>
            <select
              id="motoristas-tipo-veiculo"
              value={tipoVeiculo}
              disabled={estadoTiposVeiculo.status !== "success" || semTiposVeiculo}
              onChange={(event) => setTipoVeiculo(event.target.value)}
            >
              {semTiposVeiculo ? (
                <option value="" disabled>Nenhum veículo cadastrado</option>
              ) : (
                <option value="">
                  {estadoTiposVeiculo.status === "loading"
                    ? "Carregando tipos..."
                    : estadoTiposVeiculo.status === "error" ? "Tipos indisponíveis" : "Todos"}
                </option>
              )}
              {tiposVeiculo.map((tipo) => (
                <option key={tipo.idTipo} value={tipo.descricao}>{tipo.descricao}</option>
              ))}
            </select>
          </div>
          <div className="motoristas-filtro">
            <label htmlFor="motoristas-mes">Mês</label>
            <input
              id="motoristas-mes"
              type="month"
              value={mes}
              onChange={(event) => {
                if (event.target.value) atualizarConsulta(event.target.value, 1);
              }}
            />
          </div>
        </div>
      </div>

      {status === "loading" && <TableSkeleton />}

      {status === "error" && (
        <div className="motoristas-feedback" role="alert">
          <p>Não foi possível carregar os motoristas.</p>
          <button
            type="button"
            onClick={() => setTentativa((atual) => atual + 1)}
          >
            Tentar novamente
          </button>
        </div>
      )}

      {status === "success" && dados && dados.content.length === 0 && (
        <div className="motoristas-feedback">
          <p>Nenhum motorista encontrado para este mês.</p>
        </div>
      )}

      {status === "success" && dados && dados.content.length > 0 && (
        <>
          {motoristasFiltrados.length === 0 ? (
            <div className="motoristas-feedback" role="status">
              <p>
                Nenhum motorista corresponde aos filtros aplicados
                {busca.trim() ? ` para “${busca}”` : ""}
                {tipoVeiculo ? ` e ao tipo “${tipoVeiculo}”` : ""}.
              </p>
            </div>
          ) : (
            <>
              <div className="motoristas-table-wrap">
                <table className="motoristas-table">
                  <thead>
                    <tr>
                      {COLUNAS_ORDENACAO.map(({ chave, rotulo }) => (
                        <th
                          key={chave}
                          scope="col"
                          aria-sort={ordenacao?.coluna === chave
                            ? ordenacao.direcao === "asc" ? "ascending" : "descending"
                            : "none"}
                        >
                          <button
                            type="button"
                            className="motoristas-ordenar"
                            onClick={() => alternarOrdenacao(chave)}
                            aria-label={`Ordenar por ${rotulo}${
                              ordenacao?.coluna === chave
                                ? ordenacao.direcao === "asc" ? ", crescente" : ", decrescente"
                                : ""
                            }`}
                          >
                            {rotulo}
                            {iconeOrdenacao(chave)}
                          </button>
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {motoristasOrdenados.map((motorista) => {
                      const destino = `/motoristas/${motorista.motoristaId}?mes=${encodeURIComponent(mes)}&page=${pagina}`;
                      return (
                        <tr
                          key={`${motorista.motoristaId}-${motorista.veiculoId}`}
                          className="motoristas-table-row"
                          onClick={() => navigate(destino)}
                        >
                          <td className="motoristas-nome">
                            <Link
                              to={destino}
                              className="motoristas-profile-link"
                              onClick={(event) => event.stopPropagation()}
                            >
                              {motorista.nomeMotorista}
                            </Link>
                          </td>
                          {valoresMotorista(motorista).map((campo) => (
                            <td key={campo.rotulo} data-label={campo.rotulo}>
                              {campo.valor}
                            </td>
                          ))}
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>

              <div className="motoristas-mobile-cards">
                {motoristasOrdenados.map((motorista) => (
                  <Link
                    key={`${motorista.motoristaId}-${motorista.veiculoId}`}
                    to={`/motoristas/${motorista.motoristaId}?mes=${encodeURIComponent(mes)}&page=${pagina}`}
                    className="motorista-mobile-card"
                  >
                    <span className="motorista-mobile-name">
                      {motorista.nomeMotorista}
                      <span aria-hidden="true">›</span>
                    </span>
                    <span className="motorista-mobile-fields">
                      {valoresMotorista(motorista).map((campo) => (
                        <span className="motorista-mobile-field" key={campo.rotulo}>
                          <span>{campo.rotulo}</span>
                          <strong>{campo.valor}</strong>
                        </span>
                      ))}
                    </span>
                  </Link>
                ))}
              </div>
            </>
          )}

          <nav className="motoristas-paginacao" aria-label="Paginação de motoristas">
            <span className="motoristas-contagem">
              {termo || tipoVeiculo
                ? `${motoristasFiltrados.length} de ${dados.content.length} nesta página`
                : `${dados.totalElements.toLocaleString("pt-BR")} motoristas`}
            </span>
            <div className="motoristas-paginacao-controles">
              <button
                type="button"
                aria-label="Página anterior"
                disabled={pagina <= 1}
                onClick={() => atualizarConsulta(mes, pagina - 1)}
              >
                Anterior
              </button>
              <span aria-live="polite">
                Página {pagina} de {Math.max(dados.totalPages, 1)}
              </span>
              <button
                type="button"
                aria-label="Próxima página"
                disabled={pagina >= dados.totalPages}
                onClick={() => atualizarConsulta(mes, pagina + 1)}
              >
                Próxima
              </button>
            </div>
          </nav>
        </>
      )}
    </section>
  );
}
