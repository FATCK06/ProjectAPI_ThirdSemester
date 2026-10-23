import { useEffect, useState } from "react";
import { Card } from "../../../../../components/Card/Card";
import { CardSkeleton } from "../../../../../components/SkeletonLoader/CardSkeleton";
import { DataTable, type ColunaTabela } from "../../../../../components/DataTable/DataTable";
import { AVISO_SEM_VIAGENS_MEDIA } from "../../../../../components/Aviso/Aviso";
import { podeVerDadosFinanceiros } from "../../../../../services/auth";
import {
  buscarIndicadores,
  buscarRankingMotoristas,
  type IndicadoresMotorista,
  type RankingMotorista,
} from "../../../../../services/Dashboard";
import { mesAtual } from "../../../../../utils/datas";
import { formatarMoeda, formatarPercentual, SEM_VALOR } from "../../../../../utils/formatacao";
import "./ranking.css";

// A ordem vem do top N por viagens; os indicadores do mês completam cada linha.
type LinhaRanking = RankingMotorista & { indicadores?: IndicadoresMotorista };

type Status = "loading" | "success" | "error";

// Fora do componente: a definição das colunas não muda entre renders
const COLUNAS: ColunaTabela<LinhaRanking>[] = [
  {
    chave: "nome",
    titulo: "Motorista",
    className: "ranking-name",
    render: (l) => (l.nome ? l.nome.toLowerCase() : SEM_VALOR),
  },
  { chave: "tipoVeiculo", titulo: "Tipo de veículo", render: (l) => l.tipoVeiculo || SEM_VALOR },
  { chave: "numeroViagens", titulo: "Nº de viagens", render: (l) => l.numeroViagens },
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
    chave: "valorFrete",
    titulo: "Valor dos fretes",
    render: (l) => formatarMoeda(l.indicadores?.valorFrete),
  },
  {
    chave: "custoTotal",
    titulo: "Custos",
    render: (l) => formatarMoeda(l.indicadores?.custoTotal),
  },
  {
    chave: "rentabilidade",
    titulo: "Rentabilidade",
    render: (l) => formatarMoeda(l.indicadores?.rentabilidade),
  },
  {
    chave: "rentabilidadeMediaViagem",
    titulo: "Rentab. média por viagem",
    render: (l) =>
      l.indicadores && l.indicadores.rentabilidadeMediaViagem === undefined ? (
        <span title={AVISO_SEM_VIAGENS_MEDIA} aria-label={AVISO_SEM_VIAGENS_MEDIA}>{SEM_VALOR}</span>
      ) : (
        formatarMoeda(l.indicadores?.rentabilidadeMediaViagem)
      ),
  },
];

const COLUNAS_FINANCEIRAS = ["valorFrete", "custoTotal", "rentabilidade", "rentabilidadeMediaViagem"];

export function RankingMotoristas() {
  const [mes, setMes] = useState(mesAtual());
  const [itens, setItens] = useState<LinhaRanking[]>([]);
  const [status, setStatus] = useState<Status>("loading");
  const [tentativa, setTentativa] = useState(0);

  useEffect(() => {
    let cancelado = false;

    Promise.all([buscarRankingMotoristas(mes, 5), buscarIndicadores(mes)])
      .then(([ranking, indicadores]) => {
        if (cancelado) return;
        const porMotorista = new Map(
          indicadores.motoristas.map((m) => [m.motoristaId, m]),
        );
        setItens(
          ranking.map((item) => ({
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
          if (!e.target.value) return; 
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
          colunas={
            podeVerDadosFinanceiros()
              ? COLUNAS
              : COLUNAS.filter((c) => !COLUNAS_FINANCEIRAS.includes(c.chave))
          }
          linhas={itens}
          chaveLinha={(l) => l.motoristaId}
          alturaMaxima={232}
        />
      )}
    </Card>
  );
}