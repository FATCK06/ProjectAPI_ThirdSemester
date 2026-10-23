import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { TableSkeleton } from "../../../components/SkeletonLoader/TableSkeleton";
import { PageHeader } from "../../../components/PageHeader";
import { Paginacao } from "../../../components/Paginacao";
import {
  DataTable,
  type ColunaTabela,
  type OrdenacaoTabela,
} from "../../../components/DataTable/DataTable";
import {
  buscarMotoristasMes,
  buscarTiposVeiculo,
  type MotoristasMesPage,
  type MotoristaMes,
  type TipoVeiculo,
} from "../../../services/Motoristas";
import { mesAtual } from "../../../utils/datas";
import { formatarPercentual } from "../../../utils/formatacao";
import "./operacaoMensal.css";

type Status = "loading" | "success" | "error";
type ResultadoBusca =
  | { chave: string; status: "success"; dados: MotoristasMesPage }
  | { chave: string; status: "error" };
type EstadoTiposVeiculo =
  | { status: "loading" }
  | { status: "success"; tipos: TipoVeiculo[] | null }
  | { status: "error" };
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

const COLUNAS: ColunaTabela<MotoristaMes>[] = [
  {
    chave: "nomeMotorista",
    titulo: "Motorista",
    render: (m) => m.nomeMotorista,
    ordenarPor: (m) => m.nomeMotorista,
  },
  {
    chave: "tipoVeiculo",
    titulo: "Tipo de veículo",
    render: (m) => m.tipoVeiculo,
    ordenarPor: (m) => m.tipoVeiculo,
  },
  {
    chave: "modelo",
    titulo: "Modelo",
    render: (m) => m.modelo?.trim() || "Não informado",
    ordenarPor: (m) => m.modelo,
  },
  { chave: "placa", titulo: "Placa", render: (m) => m.placa, ordenarPor: (m) => m.placa },
  {
    chave: "viagensNoMes",
    titulo: "Viagens",
    render: (m) => m.viagensNoMes,
    ordenarPor: (m) => m.viagensNoMes,
  },
  {
    chave: "diasUtilizados",
    titulo: "Dias em operação",
    render: (m) => m.diasUtilizados,
    ordenarPor: (m) => m.diasUtilizados,
  },
  {
    chave: "disponibilidade",
    titulo: "Disponibilidade",
    render: (m) => formatarPercentual(m.disponibilidade),
    ordenarPor: (m) => m.disponibilidade,
  },
  {
    chave: "utilizacao",
    titulo: "Utilização",
    render: (m) => formatarPercentual(m.utilizacao),
    ordenarPor: (m) => m.utilizacao,
  },
  {
    chave: "percentualEfetividade",
    titulo: "Efetividade",
    render: (m) => formatarPercentual(m.percentualEfetividade),
    ordenarPor: (m) => m.percentualEfetividade,
  },
];

export function OperacaoMensal() {
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
  // Fica na página para a ordenação continuar ao trocar de página ou de mês
  const [ordenacao, setOrdenacao] = useState<OrdenacaoTabela | null>(null);
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
    <section className="operacao-mensal-page">
      <PageHeader titulo="Operação Mensal" subtitulo="Viagens, dias em operação e indicadores de cada motorista e veículo no mês">
        <div className="operacao-mensal-filtros">
          <div className="operacao-mensal-filtro">
            <label htmlFor="operacao-mensal-busca">Buscar motorista ou placa</label>
            <input
              id="operacao-mensal-busca"
              type="search"
              placeholder="Nome ou placa"
              value={busca}
              onChange={(event) => setBusca(event.target.value)}
            />
          </div>
          <div className="operacao-mensal-filtro">
            <label htmlFor="operacao-mensal-tipo-veiculo">Tipo de veículo</label>
            <select
              id="operacao-mensal-tipo-veiculo"
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
          <div className="operacao-mensal-filtro">
            <label htmlFor="operacao-mensal-mes">Mês</label>
            <input
              id="operacao-mensal-mes"
              type="month"
              value={mes}
              onChange={(event) => {
                if (event.target.value) atualizarConsulta(event.target.value, 1);
              }}
            />
          </div>
        </div>
      </PageHeader>

      {status === "loading" && <TableSkeleton />}

      {status === "error" && (
        <div className="operacao-mensal-feedback" role="alert">
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
        <div className="operacao-mensal-feedback">
          <p>Nenhum motorista encontrado para este mês.</p>
        </div>
      )}

      {status === "success" && dados && dados.content.length > 0 && (
        <>
          {motoristasFiltrados.length === 0 ? (
            <div className="operacao-mensal-feedback" role="status">
              <p>
                Nenhum motorista corresponde aos filtros aplicados
                {busca.trim() ? ` para “${busca}”` : ""}
                {tipoVeiculo ? ` e ao tipo “${tipoVeiculo}”` : ""}.
              </p>
            </div>
          ) : (
            <DataTable
              colunas={COLUNAS}
              linhas={motoristasFiltrados}
              chaveLinha={(m) => `${m.motoristaId}-${m.veiculoId}`}
              linkLinha={(m) =>
                `/operacao-mensal/motoristas/${m.motoristaId}?mes=${encodeURIComponent(mes)}&page=${pagina}`}
              cardsNoMobile
              ordenacao={ordenacao}
              aoOrdenar={setOrdenacao}
            />
          )}

          {/* A lista já vem paginada do backend, por isso a paginação fica fora do DataTable */}
          <Paginacao
            pagina={pagina}
            totalPaginas={dados.totalPages}
            aoMudar={(proxima) => atualizarConsulta(mes, proxima)}
            rotulo="Paginação de motoristas"
            contagem={termo || tipoVeiculo
              ? `${motoristasFiltrados.length} de ${dados.content.length} nesta página`
              : `${dados.totalElements.toLocaleString("pt-BR")} motoristas`}
          />
        </>
      )}
    </section>
  );
}
