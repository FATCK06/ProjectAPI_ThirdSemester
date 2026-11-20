import { useEffect, useMemo, useState } from "react";
import { Card } from "../../../../../components/Card/Card";
import { TableSkeleton } from "../../../../../components/SkeletonLoader/TableSkeleton";
import { DataTable, type ColunaTabela } from "../../../../../components/DataTable/DataTable";
import { AVISO_SEM_VIAGENS_MEDIA } from "../../../../../components/Aviso/Aviso";
import { podeVerDadosFinanceiros } from "../../../../../services/auth";
import {
  buscarRankingMotoristas,
  type IndicadoresMotorista,
  type RankingMotorista,
} from "../../../../../services/Dashboard";
import { formatarMoeda, SEM_VALOR } from "../../../../../utils/formatacao";
import "./ranking.css";

const LIMITE = 5;

// O CSV não informa férias, afastamento nem manutenção: sem dias disponíveis,
// disponibilidade e utilização ficam vazias, igual ao card de indicadores.
const AVISO_SEM_DIAS_DISPONIVEIS = "Ainda não calculado: o CSV de manifestos não informa os dias disponíveis.";

// A ordem vem do top N por viagens; os indicadores do mês completam cada linha.
type LinhaRanking = RankingMotorista & { indicadores?: IndicadoresMotorista };

type Status = "loading" | "success" | "error";

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
  { chave: "disponibilidade", titulo: "Disponibilidade", render: semDiasDisponiveis },
  { chave: "utilizacao", titulo: "Utilização", render: semDiasDisponiveis },
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
        <span className="ranking-sem-valor" title={AVISO_SEM_VIAGENS_MEDIA} aria-label={AVISO_SEM_VIAGENS_MEDIA}>{SEM_VALOR}</span>
      ) : (
        formatarMoeda(l.indicadores?.rentabilidadeMediaViagem)
      ),
  },
];

const COLUNAS_FINANCEIRAS = ["valorFrete", "custoTotal", "rentabilidade", "rentabilidadeMediaViagem"];

export function RankingMotoristas({ mesReferencia, indicadores }: PropsRankingMotoristas) {
  const [tentativa, setTentativa] = useState(0);
  // O resultado guarda a chave do pedido: se ela não bate com a atual, o pedido
  // novo ainda está a caminho. Assim o carregando é derivado, sem setState no efeito.
  const [resultado, setResultado] = useState<
    { chave: string; status: "success"; linhas: RankingMotorista[] } | { chave: string; status: "error" } | null
  >(null);
  const chave = `${mesReferencia}|${tentativa}`;
  const status: Status = resultado?.chave === chave ? resultado.status : "loading";
  const ranking = useMemo(
    () => (resultado?.chave === chave && resultado.status === "success" ? resultado.linhas : []),
    [resultado, chave],
  );

  useEffect(() => {
    let cancelado = false;

    buscarRankingMotoristas(mesReferencia, LIMITE)
      .then((linhas) => {
        if (!cancelado) setResultado({ chave, status: "success", linhas });
      })
      .catch(() => {
        if (!cancelado) setResultado({ chave, status: "error" });
      });

    return () => {
      cancelado = true;
    };
  }, [mesReferencia, chave]);

  const itens = useMemo<LinhaRanking[]>(() => {
    const porMotorista = new Map((indicadores ?? []).map((m) => [m.motoristaId, m]));
    return ranking.map((item) => ({ ...item, indicadores: porMotorista.get(item.motoristaId) }));
  }, [ranking, indicadores]);

  // Só desenha quando as duas partes chegaram: o top 5 e os indicadores do Dashboard
  const carregando = status === "loading" || (status === "success" && indicadores === undefined);
  const colunas = podeVerDadosFinanceiros()
    ? COLUNAS
    : COLUNAS.filter((c) => !COLUNAS_FINANCEIRAS.includes(c.chave));

  return (
    <Card
      titulo="Ranking de Motoristas"
      subtitulo={`Top ${LIMITE} por quantidade de viagens`}
    >
      {/* O cabeçalho do card é fixo: só a tabela espera, com as mesmas 5 linhas */}
      {carregando && <TableSkeleton linhas={LIMITE} colunas={colunas.length} />}

      {status === "error" && (
        <div className="ranking-feedback">
          <p>Não foi possível carregar o ranking.</p>
          <button type="button" onClick={() => setTentativa((t) => t + 1)}>
            Tentar novamente
          </button>
        </div>
      )}

      {!carregando && status === "success" && itens.length === 0 && (
        <div className="ranking-feedback">
          <p>Nenhuma viagem importada para este mês.</p>
        </div>
      )}

      {!carregando && status === "success" && itens.length > 0 && (
        <DataTable
          colunas={colunas}
          linhas={itens}
          chaveLinha={(l) => l.motoristaId}
          linkLinha={(l) => `/motoristas/${l.motoristaId}?mes=${encodeURIComponent(mesReferencia)}`}
          estadoNavegacaoLinha={() => ({
            retorno: {
              destino: `/dashboard?mes=${encodeURIComponent(mesReferencia)}`,
              rotulo: "Voltar para o Dashboard",
            },
          })}
          alturaMaxima={232}
        />
      )}
    </Card>
  );
}
