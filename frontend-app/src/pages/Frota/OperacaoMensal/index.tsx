import { useEffect, useRef, useState } from "react";
import { useLocation, useSearchParams } from "react-router-dom";
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
  baixarOperacaoMensalCsv,
  type MotoristasMesPage,
  type MotoristaMes,
  type TipoVeiculo,
} from "../../../services/Motoristas";
import { CampoMes } from "../../../components/CampoMes/CampoMes";
import { Loading } from "../../../components/Loading";
import { SeloMes } from "../../../components/SeloMes/SeloMes";
import { useMesDaUrl } from "../../../hooks/useMesDaUrl";
import { Select, type OpcaoSelect } from "../../../components/Select/Select";
import { mesAtual } from "../../../utils/datas";
import { formatarPercentual } from "../../../utils/formatacao";
import { ExportarCsvButton } from "../../../components/ExportarCsvButton/ExportarCsvButton";
import "./operacaoMensal.css";

type Status = "loading" | "success" | "error";
type ResultadoBusca =
  | { chave: string; status: "success"; dados: MotoristasMesPage }
  | { chave: string; status: "error" };
type EstadoTiposVeiculo =
  | { status: "loading" }
  | { status: "success"; tipos: TipoVeiculo[] | null }
  | { status: "error" };
type RestauracaoTabela = {
  busca?: string;
  tipoVeiculo?: string;
  ordenacao?: OrdenacaoTabela | null;
  scrollY?: number;
};
function paginaDaUrl(valor: string | null) {
  if (!valor || !/^\d+$/.test(valor)) return 1;
  const pagina = Number(valor);
  return Number.isSafeInteger(pagina) && pagina > 0 && pagina <= 2_147_483_647
    ? pagina
    : 1;
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

// Sem ?mes na URL, espera o mês mais recente com dados antes de montar a tela
export function OperacaoMensal() {
  const mes = useMesDaUrl();
  return mes ? <OperacaoMensalDoMes mes={mes} /> : <Loading texto="Carregando mês" />;
}

function OperacaoMensalDoMes({ mes }: { mes: string }) {
  const location = useLocation();
  const restauracao = (location.state as { restauracao?: RestauracaoTabela } | null)?.restauracao;
  const [searchParams, setSearchParams] = useSearchParams();
  const paginaParametro = searchParams.get("page");
  const pagina = paginaDaUrl(paginaParametro);
  const [busca, setBusca] = useState(restauracao?.busca ?? "");
  const [tipoVeiculo, setTipoVeiculo] = useState(restauracao?.tipoVeiculo ?? "");
  const [estadoTiposVeiculo, setEstadoTiposVeiculo] =
    useState<EstadoTiposVeiculo>({ status: "loading" });
  // Fica na página para a ordenação continuar ao trocar de página ou de mês
  const [ordenacao, setOrdenacao] = useState<OrdenacaoTabela | null>(restauracao?.ordenacao ?? null);
  const [resultado, setResultado] = useState<ResultadoBusca | null>(null);
  const [tentativa, setTentativa] = useState(0);
  const scrollRestaurado = useRef(false);
  const chaveConsulta = `${mes}|${pagina}|${busca}|${tipoVeiculo}|${tentativa}`;
  const status: Status = resultado?.chave === chaveConsulta
    ? resultado.status
    : "loading";
  const dados = resultado?.chave === chaveConsulta && resultado.status === "success"
    ? resultado.dados
    : null;
  const filtrosAtivos = Boolean(busca.trim() || tipoVeiculo);
  const tiposVeiculo = estadoTiposVeiculo.status === "success"
    ? Array.from(
      new Map((estadoTiposVeiculo.tipos ?? []).map((tipo) => [tipo.descricao, tipo])).values(),
    )
    : [];
  const semTiposVeiculo = estadoTiposVeiculo.status === "success" && tiposVeiculo.length === 0;
  // Sem tipos cadastrados a lista fica vazia e o campo mostra o placeholder
  const opcoesTipoVeiculo: OpcaoSelect<string>[] = semTiposVeiculo
    ? []
    : [
      {
        valor: "",
        rotulo: estadoTiposVeiculo.status === "loading"
          ? "Carregando tipos..."
          : estadoTiposVeiculo.status === "error" ? "Tipos indisponíveis" : "Todos",
      },
      ...tiposVeiculo.map((tipo) => ({ valor: tipo.descricao, rotulo: tipo.descricao })),
    ];
  const motoristasFiltrados = dados?.content ?? [];

  useEffect(() => {
    if (paginaParametro !== String(pagina)) {
      setSearchParams((atual) => {
        const normalizados = new URLSearchParams(atual);
        normalizados.set("page", String(pagina));
        return normalizados;
      }, { replace: true });
    }
  }, [pagina, paginaParametro, setSearchParams]);

  useEffect(() => {
    let cancelado = false;

    buscarMotoristasMes(mes, pagina, { busca, tipoVeiculo })
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
  }, [busca, chaveConsulta, mes, pagina, setSearchParams, tipoVeiculo]);

  useEffect(() => {
    if (status !== "success" || restauracao?.scrollY === undefined || scrollRestaurado.current) return;
    scrollRestaurado.current = true;
    requestAnimationFrame(() => window.scrollTo(0, restauracao.scrollY ?? 0));
  }, [status, restauracao]);

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
    <section className="operacao-mensal-page" data-tour="operacao-pagina">
      <PageHeader titulo="Operação Mensal" selo={<SeloMes mes={mes} />} subtitulo="Viagens, dias em operação e indicadores de cada motorista e veículo no mês">
        <div className="operacao-mensal-filtros">
          <div className="operacao-mensal-filtro" data-tour="operacao-busca">
            <label htmlFor="operacao-mensal-busca">Buscar motorista ou placa</label>
            <input
              id="operacao-mensal-busca"
              type="search"
              placeholder="Nome ou placa"
              value={busca}
              onChange={(event) => {
                setBusca(event.target.value);
                atualizarConsulta(mes, 1);
              }}
            />
          </div>
          <div className="operacao-mensal-filtro" data-tour="operacao-tipo">
            <label htmlFor="operacao-mensal-tipo-veiculo">Tipo de veículo</label>
            <Select
              id="operacao-mensal-tipo-veiculo"
              valor={tipoVeiculo}
              opcoes={opcoesTipoVeiculo}
              placeholder="Nenhum veículo cadastrado"
              disabled={estadoTiposVeiculo.status !== "success" || semTiposVeiculo}
              onChange={(novoTipo) => {
                setTipoVeiculo(novoTipo);
                atualizarConsulta(mes, 1);
              }}
            />
          </div>
          <div className="operacao-mensal-filtro" data-tour="operacao-mes">
            <label htmlFor="operacao-mensal-mes">Mês</label>
            <CampoMes
              id="operacao-mensal-mes"
              valor={mes}
              max={mesAtual()}
              onChange={(novoMes) => atualizarConsulta(novoMes, 1)}
            />
          </div>
          <ExportarCsvButton
            nomeArquivo={`mensal_${mes.replace("-", "_")}.csv`}
            baixarCsv={(aoProgredir) => baixarOperacaoMensalCsv(
              mes,
              busca,
              tipoVeiculo,
              aoProgredir,
            )}
            disabled={status !== "success"}
          />
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
        <div className="operacao-mensal-feedback" role="status">
          <p>
            {filtrosAtivos
              ? `Nenhum motorista corresponde aos filtros aplicados${busca.trim() ? ` para “${busca}”` : ""}${tipoVeiculo ? ` e ao tipo “${tipoVeiculo}”` : ""}.`
              : "Nenhum motorista encontrado para este mês."}
          </p>
        </div>
      )}

      {status === "success" && dados && dados.content.length > 0 && (
        <>
          <DataTable
            colunas={COLUNAS}
            linhas={motoristasFiltrados}
            chaveLinha={(m) => `${m.motoristaId}-${m.veiculoId}`}
            // O nome leva à ficha no mesmo mês; o "Voltar" de lá restaura filtros e rolagem
            linkLinha={(m) => `/motoristas/${m.motoristaId}?mes=${encodeURIComponent(mes)}`}
            estadoNavegacaoLinha={() => ({
              retorno: {
                destino: `/operacao-mensal?mes=${encodeURIComponent(mes)}&page=${pagina}`,
                rotulo: "Voltar para Operação Mensal",
                restauracao: { busca, tipoVeiculo, ordenacao, scrollY: window.scrollY },
              },
            })}
            cardsNoMobile
            ordenacao={ordenacao}
            aoOrdenar={setOrdenacao}
          />

          {/* A lista já vem paginada do backend, por isso a paginação fica fora do DataTable */}
          <Paginacao
            pagina={pagina}
            totalPaginas={dados.totalPages}
            aoMudar={(proxima) => atualizarConsulta(mes, proxima)}
            rotulo="Paginação de motoristas"
            contagem={filtrosAtivos
              ? `${dados.totalElements.toLocaleString("pt-BR")} motoristas correspondem aos filtros`
              : `${dados.totalElements.toLocaleString("pt-BR")} motoristas`}
          />
        </>
      )}
    </section>
  );
}
