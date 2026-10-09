import { useEffect, useState } from "react";
import api from "../../../../services/api";
import { Card } from "../../../../components/Card/Card";
import { CardSkeleton } from "../../../../components/SkeletonLoader/CardSkeleton";
import { DataTable, type ColunaTabela } from "../../../../components/DataTable/DataTable";
import "./ranking.css";

interface RankingItem {
  posicao: number;
  motoristaId: string;
  nome: string | null;
  cpf: string | null;
  totalViagens: number;
  veiculo: string | null;
  distanciaMaximaKm: number | null;
}

// Linha de /dashboard/indicadores; valores em R$ e percentuais de 0 a 100
interface IndicadoresMotorista {
  motoristaId: string;
  utilizacao: number;
  disponibilidade: number;
  valorFrete: number;
  custoTotal: number;
  rentabilidade: number;
  rentabilidadeMediaViagem: number;
}

interface IndicadoresMes {
  motoristas: IndicadoresMotorista[];
}

type LinhaRanking = RankingItem & { indicadores?: IndicadoresMotorista };

type Status = "loading" | "success" | "error";

function mesAtual() {
  const hoje = new Date();
  return `${hoje.getFullYear()}-${String(hoje.getMonth() + 1).padStart(2, "0")}`;
}

function formatarMoeda(valor: number | undefined) {
  return valor === undefined
    ? "—"
    : valor.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

function formatarPercentual(valor: number | undefined) {
  return valor === undefined
    ? "—"
    : `${valor.toLocaleString("pt-BR", { maximumFractionDigits: 2 })}%`;
}

// Fora do componente: a definição das colunas não muda entre renders
const COLUNAS: ColunaTabela<LinhaRanking>[] = [
  {
    chave: "nome",
    titulo: "Motorista",
    className: "ranking-name",
    render: (l) => (l.nome ? l.nome.toLowerCase() : "—"),
  },
  { chave: "veiculo", titulo: "Tipo de veículo", render: (l) => l.veiculo || "—" },
  { chave: "viagens", titulo: "Nº de viagens", render: (l) => l.totalViagens },
  {
    chave: "disponibilidade",
    titulo: "Disponibilidade",
    render: (l) => formatarPercentual(l.indicadores?.disponibilidade),
  },
  {
    chave: "utilizacao",
    titulo: "Utilização",
    render: (l) => formatarPercentual(l.indicadores?.utilizacao),
  },
  {
    chave: "frete",
    titulo: "Valor dos fretes",
    render: (l) => formatarMoeda(l.indicadores?.valorFrete),
  },
  {
    chave: "custos",
    titulo: "Custos",
    render: (l) => formatarMoeda(l.indicadores?.custoTotal),
  },
  {
    chave: "rentabilidade",
    titulo: "Rentabilidade",
    render: (l) => formatarMoeda(l.indicadores?.rentabilidade),
  },
  {
    chave: "rentabilidadeMedia",
    titulo: "Rentab. média por viagem",
    render: (l) => formatarMoeda(l.indicadores?.rentabilidadeMediaViagem),
  },
];

export function RankingMotoristas() {
  const [mes, setMes] = useState(mesAtual());
  const [itens, setItens] = useState<LinhaRanking[]>([]);
  const [status, setStatus] = useState<Status>("loading");
  const [tentativa, setTentativa] = useState(0);

  useEffect(() => {
    let cancelado = false;

    // A ordem vem do ranking (top 5 por viagens); os indicadores completam cada linha
    Promise.all([
      api.get<RankingItem[]>("/dashboard/ranking-motoristas", {
        params: { mesReferencia: mes, limite: 5 },
      }),
      api.get<IndicadoresMes>("/dashboard/indicadores", {
        params: { mesReferencia: mes },
      }),
    ])
      .then(([ranking, indicadores]) => {
        if (cancelado) return;
        const porMotorista = new Map(
          indicadores.data.motoristas.map((m) => [m.motoristaId, m]),
        );
        setItens(
          ranking.data.map((item) => ({
            ...item,
            indicadores: porMotorista.get(item.motoristaId),
          })),
        );
        setStatus("success");
      })
      .catch(() => {
        if (!cancelado) setStatus("error");
      });

    return () => {
      cancelado = true;
    };
  }, [mes, tentativa]);

  const filtroMes = (
    <div className="ranking-filter">
      <label htmlFor="ranking-mes">Mês</label>
      <input
        id="ranking-mes"
        type="month"
        value={mes}
        onChange={(e) => {
          if (!e.target.value) return; // campo apagado: mantém o mês atual
          setStatus("loading");
          setMes(e.target.value);
        }}
      />
    </div>
  );

  return (
    <Card
      titulo="Ranking de Motoristas"
      subtitulo="Top 5 por quantidade de viagens"
      acoes={filtroMes}
    >
      {status === "loading" && <CardSkeleton isLoading altura={180} />}

      {status === "error" && (
        <div className="ranking-feedback">
          <p>Não foi possível carregar o ranking.</p>
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
        <div className="ranking-feedback">
          <p>Nenhuma viagem importada para este mês.</p>
        </div>
      )}

      {status === "success" && itens.length > 0 && (
        <DataTable
          colunas={COLUNAS}
          linhas={itens}
          chaveLinha={(l) => l.motoristaId}
          alturaMaxima={232}
        />
      )}
    </Card>
  );
}