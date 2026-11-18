import { Line } from "react-chartjs-2";
import type { ChartData, ChartOptions } from "chart.js";
import "../../../../components/chartSetup";
import { Aviso } from "../../../../components/Aviso/Aviso";
import type { SerieMes, TotaisSerie } from "../../../../services/Dashboard";
import { podeVerDadosFinanceiros } from "../../../../services/auth";
import { formatarMesCurto, formatarMesNumerico } from "../../../../utils/datas";
import { formatarMoeda, formatarPercentual } from "../../../../utils/formatacao";

interface Linha {
  rotulo: string;
  cor: string;
  obter: (t: TotaisSerie) => number | undefined;
}

interface DefinicaoGrafico {
  id: string;
  titulo: string;
  linhas: Linha[];
  formatar: (valor: number) => string;
  financeiro?: boolean;
}

function formatarNumero(valor: number): string {
  return valor.toLocaleString("pt-BR", { maximumFractionDigits: 1 });
}

// Um gráfico por unidade: escalas diferentes no mesmo eixo achatariam as linhas
const GRAFICOS: DefinicaoGrafico[] = [
  {
    id: "viagens",
    titulo: "Nº de viagens",
    linhas: [{ rotulo: "Viagens", cor: "#16a34a", obter: (t) => t.numeroViagens }],
    formatar: formatarNumero,
  },
  {
    id: "utilizacao",
    titulo: "% de utilização",
    linhas: [{ rotulo: "Utilização", cor: "#7c3aed", obter: (t) => t.utilizacaoMedia }],
    formatar: formatarPercentual,
  },
  {
    id: "financeiro",
    titulo: "Valores (R$)",
    financeiro: true,
    linhas: [
      { rotulo: "Valor dos fretes", cor: "#0891b2", obter: (t) => t.valorFrete },
      { rotulo: "Custo total", cor: "#e11d48", obter: (t) => t.custoTotal },
      { rotulo: "Rentabilidade", cor: "#2563eb", obter: (t) => t.rentabilidade },
    ],
    formatar: formatarMoeda,
  },
];

// Mês sem dados: rótulo em duas linhas no eixo X
function rotulos(serie: SerieMes[]): (string | string[])[] {
  return serie.map((m) =>
    m.semDados ? [formatarMesCurto(m.mesReferencia), "sem dados"] : formatarMesCurto(m.mesReferencia),
  );
}

function montarDados(serie: SerieMes[], grafico: DefinicaoGrafico): ChartData<"line"> {
  return {
    labels: rotulos(serie),
    datasets: grafico.linhas.map((linha) => ({
      label: linha.rotulo,
      // null (nunca 0) no mês sem dados: com spanGaps false a linha fica interrompida
      data: serie.map((m) => (m.semDados || !m.totais ? null : (linha.obter(m.totais) ?? null))),
      borderColor: linha.cor,
      backgroundColor: linha.cor,
      borderWidth: 2,
      pointRadius: 4,
      pointHoverRadius: 6,
      tension: 0.25,
      spanGaps: false,
    })),
  };
}

function montarOpcoes(grafico: DefinicaoGrafico): ChartOptions<"line"> {
  return {
    responsive: true,
    maintainAspectRatio: false,
    interaction: { mode: "index", intersect: false },
    plugins: {
      legend: { display: grafico.linhas.length > 1, position: "bottom" },
      tooltip: {
        backgroundColor: "rgba(15, 23, 42, 0.9)",
        padding: 12,
        cornerRadius: 8,
        callbacks: {
          label: (context) => {
            const valor = context.parsed.y;
            return ` ${context.dataset.label}: ${valor === null ? "sem dados" : grafico.formatar(valor)}`;
          },
        },
      },
    },
    scales: {
      y: {
        beginAtZero: grafico.id !== "financeiro",
        grid: { color: "rgba(226, 232, 240, 0.6)" },
        ticks: {
          color: "#64748b",
          callback: (value) => grafico.formatar(Number(value)),
        },
        border: { display: false },
      },
      x: {
        grid: { display: false },
        ticks: { color: "#64748b" },
      },
    },
  };
}

interface PropsGraficoEvolucao {
  serie: SerieMes[];
}

export function GraficoEvolucao({ serie }: PropsGraficoEvolucao) {
  const financeiro = podeVerDadosFinanceiros();
  const graficos = GRAFICOS.filter((g) => !g.financeiro || financeiro);
  // O tooltip não abre em ponto null: a mensagem fica abaixo dos gráficos
  const semDados = serie.filter((m) => m.semDados);

  return (
    <div className="comparativo-evolucao">
      <div className="comparativo-graficos">
        {graficos.map((g) => (
          <figure key={g.id} className="comparativo-grafico">
            <figcaption>{g.titulo}</figcaption>
            <div className="comparativo-grafico-area">
              <Line data={montarDados(serie, g)} options={montarOpcoes(g)} />
            </div>
          </figure>
        ))}
      </div>
      {semDados.map((m) => (
        <Aviso key={m.mesReferencia} mensagem={`Sem dados para ${formatarMesNumerico(m.mesReferencia)}.`} />
      ))}
    </div>
  );
}

export default GraficoEvolucao;
