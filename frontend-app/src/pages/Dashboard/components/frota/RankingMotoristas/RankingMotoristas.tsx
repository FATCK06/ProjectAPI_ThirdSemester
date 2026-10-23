<<<<<<< HEAD
import { useEffect, useMemo, useState } from "react";
import { Card } from "../../../../../components/Card/Card";
import { TableSkeleton } from "../../../../../components/SkeletonLoader/TableSkeleton";
=======
import { useEffect, useState } from "react";
import { Card } from "../../../../../components/Card/Card";
import { CardSkeleton } from "../../../../../components/SkeletonLoader/CardSkeleton";
>>>>>>> origin/develop
import { DataTable, type ColunaTabela } from "../../../../../components/DataTable/DataTable";
import { AVISO_SEM_VIAGENS_MEDIA } from "../../../../../components/Aviso/Aviso";
import { podeVerDadosFinanceiros } from "../../../../../services/auth";
import {
<<<<<<< HEAD
=======
  buscarIndicadores,
>>>>>>> origin/develop
  buscarRankingMotoristas,
  type IndicadoresMotorista,
  type RankingMotorista,
} from "../../../../../services/Dashboard";
<<<<<<< HEAD
import { formatarMoeda, SEM_VALOR } from "../../../../../utils/formatacao";
import "./ranking.css";

const LIMITE = 5;

// O CSV não informa férias, afastamento nem manutenção: sem dias disponíveis,
// disponibilidade e utilização ficam vazias, igual ao card de indicadores.
const AVISO_SEM_DIAS_DISPONIVEIS = "Ainda não calculado: o CSV de manifestos não informa os dias disponíveis.";

=======
import { mesAtual } from "../../../../../utils/datas";
import { formatarMoeda, formatarPercentual, SEM_VALOR } from "../../../../../utils/formatacao";
import "./ranking.css";

>>>>>>> origin/develop
// A ordem vem do top N por viagens; os indicadores do mês completam cada linha.
type LinhaRanking = RankingMotorista & { indicadores?: IndicadoresMotorista };

type Status = "loading" | "success" | "error";

<<<<<<< HEAD
interface PropsRankingMotoristas {
  mesReferencia: string;
  // Os mesmos de /dashboard/indicadores que o Dashboard já carregou: reaproveitar
  // evita recalcular os indicadores do mês só para completar 5 linhas.
  // undefined enquanto o Dashboard ainda carrega.
  indicadores: IndicadoresMotorista[] | undefined;
}

function semDiasDisponiveis() {
  return (
    <span className="ranking-sem-valor" title={AVISO_SEM_DIAS_DISPONIVEIS} aria-label={AVISO_SEM_DIAS_DISPONIVEIS}>
      {SEM_VALOR}
    </span>
  );
}

=======
>>>>>>> origin/develop
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
<<<<<<< HEAD
  { chave: "disponibilidade", titulo: "Disponibilidade", render: semDiasDisponiveis },
  { chave: "utilizacao", titulo: "Utilização", render: semDiasDisponiveis },
=======
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
>>>>>>> origin/develop
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
<<<<<<< HEAD
        <span className="ranking-sem-valor" title={AVISO_SEM_VIAGENS_MEDIA} aria-label={AVISO_SEM_VIAGENS_MEDIA}>{SEM_VALOR}</span>
=======
        <span title={AVISO_SEM_VIAGENS_MEDIA} aria-label={AVISO_SEM_VIAGENS_MEDIA}>{SEM_VALOR}</span>
>>>>>>> origin/develop
      ) : (
        formatarMoeda(l.indicadores?.rentabilidadeMediaViagem)
      ),
  },
];

const COLUNAS_FINANCEIRAS = ["valorFrete", "custoTotal", "rentabilidade", "rentabilidadeMediaViagem"];

<<<<<<< HEAD
export function RankingMotoristas({ mesReferencia, indicadores }: PropsRankingMotoristas) {
  const [ranking, setRanking] = useState<RankingMotorista[]>([]);
=======
export function RankingMotoristas() {
  const [mes, setMes] = useState(mesAtual());
  const [itens, setItens] = useState<LinhaRanking[]>([]);
>>>>>>> origin/develop
  const [status, setStatus] = useState<Status>("loading");
  const [tentativa, setTentativa] = useState(0);

  useEffect(() => {
    let cancelado = false;
<<<<<<< HEAD
    setStatus("loading");

    buscarRankingMotoristas(mesReferencia, LIMITE)
      .then((linhas) => {
        if (cancelado) return;
        setRanking(linhas);
=======

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
>>>>>>> origin/develop
        setStatus("success");
      })
      .catch(() => {
        if (!cancelado) setStatus("error");
      });

    return () => {
      cancelado = true;
    };
<<<<<<< HEAD
  }, [mesReferencia, tentativa]);

  const itens = useMemo<LinhaRanking[]>(() => {
    const porMotorista = new Map((indicadores ?? []).map((m) => [m.motoristaId, m]));
    return ranking.map((item) => ({ ...item, indicadores: porMotorista.get(item.motoristaId) }));
  }, [ranking, indicadores]);

  // Só desenha quando as duas partes chegaram: o top 5 e os indicadores do Dashboard
  const carregando = status === "loading" || (status === "success" && indicadores === undefined);
  const colunas = podeVerDadosFinanceiros()
    ? COLUNAS
    : COLUNAS.filter((c) => !COLUNAS_FINANCEIRAS.includes(c.chave));
=======
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
>>>>>>> origin/develop

  return (
    <Card
      titulo="Ranking de Motoristas"
<<<<<<< HEAD
      subtitulo={`Top ${LIMITE} por quantidade de viagens`}
    >
      {/* O cabeçalho do card é fixo: só a tabela espera, com as mesmas 5 linhas */}
      {carregando && <TableSkeleton linhas={LIMITE} colunas={colunas.length} />}
=======
      subtitulo="Top 5 por quantidade de viagens"
      acoes={filtroMes}
    >
      {status === "loading" && <CardSkeleton isLoading altura={180} />}
>>>>>>> origin/develop

      {status === "error" && (
        <div className="ranking-feedback">
          <p>Não foi possível carregar o ranking.</p>
<<<<<<< HEAD
          <button type="button" onClick={() => setTentativa((t) => t + 1)}>
=======
          <button
            type="button"
            onClick={() => {
              setStatus("loading");
              setTentativa((t) => t + 1);
            }}
          >
>>>>>>> origin/develop
            Tentar novamente
          </button>
        </div>
      )}

<<<<<<< HEAD
      {!carregando && status === "success" && itens.length === 0 && (
=======
      {status === "success" && itens.length === 0 && (
>>>>>>> origin/develop
        <div className="ranking-feedback">
          <p>Nenhuma viagem importada para este mês.</p>
        </div>
      )}

<<<<<<< HEAD
      {!carregando && status === "success" && itens.length > 0 && (
        <DataTable
          colunas={colunas}
=======
      {status === "success" && itens.length > 0 && (
        <DataTable
          colunas={
            podeVerDadosFinanceiros()
              ? COLUNAS
              : COLUNAS.filter((c) => !COLUNAS_FINANCEIRAS.includes(c.chave))
          }
>>>>>>> origin/develop
          linhas={itens}
          chaveLinha={(l) => l.motoristaId}
          alturaMaxima={232}
        />
      )}
    </Card>
  );
<<<<<<< HEAD
}
=======
}
>>>>>>> origin/develop
