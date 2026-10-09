import { Bar } from "react-chartjs-2";
import type { ChartData, ChartOptions } from "chart.js";
import "../../../../components/chartSetup";
import { Card } from "../../../../components/Card/Card";
import type { IndicadoresPorModelo } from "../../../../services/Dashboard";

interface PropsGraficoModelos {
  porModelo: IndicadoresPorModelo[];
}

function montarDados(porModelo: IndicadoresPorModelo[]): ChartData<"bar"> {
  return {
    labels: porModelo.map((m) => m.modelo),
    datasets: [
      {
        label: "Rentabilidade (R$)",
        data: porModelo.map((m) => m.rentabilidade),
        backgroundColor: "rgba(29, 78, 216, 0.85)",
        borderColor: "#1d4ed8",
        borderWidth: 2,
        borderRadius: 8,
        barPercentage: 0.8,
        categoryPercentage: 0.8,
      },
    ],
  };
}

const opcoes: ChartOptions<"bar"> = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: {
      callbacks: {
        label: (context) => {
          const valor = context.parsed.y ?? 0;
          return `R$ ${valor.toLocaleString("pt-BR", { minimumFractionDigits: 2 })}`;
        },
      },
    },
  },
  scales: {
    y: {
      beginAtZero: true,
      title: { display: true, text: "Rentabilidade (R$)" },
    },
    x: { title: { display: true, text: "Modelo" } },
  },
};

export function GraficoModelos({ porModelo }: PropsGraficoModelos) {
  return (
    <Card titulo="Rentabilidade por modelo de veículo">
      <div className="ui-card-chart">
        <Bar data={montarDados(porModelo)} options={opcoes} />
      </div>
    </Card>
  );
}

export default GraficoModelos;