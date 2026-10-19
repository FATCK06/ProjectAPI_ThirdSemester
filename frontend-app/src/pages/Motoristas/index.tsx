import { useEffect, useState, type ReactNode } from "react";
import { TableSkeleton } from "../../components/SkeletonLoader/TableSkeleton";
import {
  buscarSituacaoMotoristas,
  type FaixaUtilizacao,
  type FiltroSituacao,
  type SituacaoMotorista,
  type SituacaoMotoristaItem,
} from "../../services/Motoristas";
import "./motoristas.css";

type Status = "loading" | "success" | "error";

const ROTULO_FAIXA: Record<FaixaUtilizacao, string> = {
  ALTA: "Alta",
  INTERMEDIARIA: "Intermediária",
  BAIXA: "Baixa",
};

const ROTULO_SITUACAO: Record<SituacaoMotorista, string> = {
  DISPONIVEL: "Disponível",
  INDISPONIVEL: "Indisponível",
  SEM_DIAS_DISPONIVEIS: "Sem dias disponíveis",
};

const OPCOES_SITUACAO: { valor: FiltroSituacao; rotulo: string }[] = [
  { valor: "todas", rotulo: "Todas" },
  { valor: "DISPONIVEL", rotulo: ROTULO_SITUACAO.DISPONIVEL },
  { valor: "INDISPONIVEL", rotulo: ROTULO_SITUACAO.INDISPONIVEL },
  { valor: "SEM_DIAS_DISPONIVEIS", rotulo: ROTULO_SITUACAO.SEM_DIAS_DISPONIVEIS },
];

function mesAtual() {
  const hoje = new Date();
  return `${hoje.getFullYear()}-${String(hoje.getMonth() + 1).padStart(2, "0")}`;
}

function formatarPercentual(valor: number) {
  return `${valor.toLocaleString("pt-BR", { maximumFractionDigits: 2 })}%`;
}

// A cor e so apoio visual: o rotulo em texto sempre aparece ao lado.
function Indicador({ classe, rotulo }: { classe: string; rotulo: string }) {
  return (
    <span className="motoristas-indicador">
      <span className={`motoristas-bolinha ${classe}`} aria-hidden="true" />
      {rotulo}
    </span>
  );
}

function celulaUtilizacao(m: SituacaoMotoristaItem) {
  if (m.utilizacao === null || m.faixaUtilizacao === null) return "—";
  return (
    <span className="motoristas-utilizacao">
      <span className="motoristas-percentual">{formatarPercentual(m.utilizacao)}</span>
      <Indicador
        classe={`faixa-${m.faixaUtilizacao.toLowerCase()}`}
        rotulo={ROTULO_FAIXA[m.faixaUtilizacao]}
      />
    </span>
  );
}

function celulaSituacao(m: SituacaoMotoristaItem) {
  return (
    <div className="motoristas-situacao">
      <Indicador
        classe={`situacao-${m.situacao.toLowerCase()}`}
        rotulo={ROTULO_SITUACAO[m.situacao]}
      />
      {m.aviso && <p className="motoristas-aviso">{m.aviso}</p>}
    </div>
  );
}

// Colunas declaradas em lista: uma coluna nova (ex.: quantidade de viagens)
// entra aqui, sem mexer no cabecalho nem nas linhas da tabela.
interface Coluna {
  chave: string;
  titulo: string;
  render: (m: SituacaoMotoristaItem) => ReactNode;
  className?: string;
}

const COLUNAS: Coluna[] = [
  {
    chave: "motorista",
    titulo: "Motorista",
    render: (m) => (m.nome ? m.nome.toLowerCase() : "—"),
    className: "motoristas-nome",
  },
  { chave: "utilizacao", titulo: "Utilização", render: celulaUtilizacao },
  { chave: "situacao", titulo: "Situação", render: celulaSituacao },
];

export function Motoristas() {
  const [mes, setMes] = useState(mesAtual());
  const [situacao, setSituacao] = useState<FiltroSituacao>("todas");
  const [itens, setItens] = useState<SituacaoMotoristaItem[]>([]);
  const [status, setStatus] = useState<Status>("loading");
  const [tentativa, setTentativa] = useState(0);

  useEffect(() => {
    if (!mes) return;

    let cancelado = false;

    buscarSituacaoMotoristas(mes, situacao)
      .then((dados) => {
        if (cancelado) return;
        setItens(dados);
        setStatus("success");
      })
      .catch(() => {
        if (cancelado) return;
        setStatus("error");
      });

    return () => {
      cancelado = true;
    };
  }, [mes, situacao, tentativa]);

  return (
    <section className="motoristas-card">
      <div className="motoristas-header">
        <div>
          <h2 className="motoristas-title">Motoristas</h2>
          <p className="motoristas-subtitle">
            Situação de disponibilidade no mês, para quem teve viagem no período
          </p>
        </div>

        <div className="motoristas-filtros">
          <div className="motoristas-filtro">
            <label htmlFor="motoristas-mes">Mês</label>
            <input
              id="motoristas-mes"
              type="month"
              value={mes}
              onChange={(e) => {
                // Campo limpo: o effect nao busca sem mes, e o skeleton ficaria para sempre.
                if (!e.target.value) return;
                setStatus("loading");
                setMes(e.target.value);
              }}
            />
          </div>

          <div className="motoristas-filtro">
            <label htmlFor="motoristas-situacao">Situação</label>
            <select
              id="motoristas-situacao"
              value={situacao}
              onChange={(e) => {
                setStatus("loading");
                setSituacao(e.target.value as FiltroSituacao);
              }}
            >
              {OPCOES_SITUACAO.map((o) => (
                <option key={o.valor} value={o.valor}>
                  {o.rotulo}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {status === "loading" && <TableSkeleton />}

      {status === "error" && (
        <div className="motoristas-feedback">
          <p>Não foi possível carregar os motoristas.</p>
          <button
            type="button"
            onClick={() => {
              setStatus("loading");
              setTentativa((t) => t + 1);
            }}
          >
            Tentar novamente
          </button>
        </div>
      )}

      {status === "success" && itens.length === 0 && (
        <div className="motoristas-feedback">
          <p>Nenhum motorista disponível com os filtros aplicados.</p>
        </div>
      )}

      {status === "success" && itens.length > 0 && (
        <div className="motoristas-table-wrap">
          <table className="motoristas-table">
            <thead>
              <tr>
                {COLUNAS.map((c) => (
                  <th key={c.chave} scope="col">
                    {c.titulo}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {itens.map((m) => (
                <tr key={m.motoristaId}>
                  {COLUNAS.map((c) => (
                    <td key={c.chave} className={c.className}>
                      {c.render(m)}
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
