import { Bar } from "react-chartjs-2";
import type { ChartData, ChartOptions } from "chart.js";
import "../../../../../components/chartSetup";
import { Card } from "../../../../../components/Card/Card";
import type { IndicadoresPorModelo } from "../../../../../services/Dashboard";

interface PropsGraficoModelos {
  porModelo: IndicadoresPorModelo[];
}

function montarDados(porModelo: IndicadoresPorModelo[]): ChartData<"bar"> {
  return {
    labels: porModelo.map((m) => m.modelo),
    datasets: [
      {
        label: "Rentabilidade (R$)",
        data: porModelo.map((m) => m.rentabilidade ?? null),
        
        backgroundColor: "rgba(37, 99, 235, 0.85)",
        hoverBackgroundColor: "rgba(29, 78, 216, 1)",
        borderColor: "#2563eb",
        borderWidth: 0,
        borderRadius: {
          topLeft: 6,
          topRight: 6,
          bottomLeft: 0,
          bottomRight: 0,
        },
        borderSkipped: false,
        barPercentage: 0.6,
        categoryPercentage: 0.7,
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
      backgroundColor: "rgba(15, 23, 42, 0.9)", 
      titleFont: { size: 13, family: "Inter, sans-serif", weight: "bold" },
      bodyFont: { size: 12, family: "Inter, sans-serif" },
      padding: 12,
      cornerRadius: 8,
      boxPadding: 6,
      usePointStyle: true,
      callbacks: {
        label: (context) => {
          const valor = context.parsed.y ?? 0;
          return ` Rentabilidade: R$ ${valor.toLocaleString("pt-BR", {
            minimumFractionDigits: 2,
          })}`;
        },
      },
    },
  },
  scales: {
    y: {
      beginAtZero: true,
      grid: {
        color: "rgba(226, 232, 240, 0.6)", 
        
      },
      ticks: {
        font: { size: 11, family: "Inter, sans-serif" },
        color: "#64748b", 
        callback: (value) => `R$ ${Number(value).toLocaleString("pt-BR", { notation: "compact" })}`,
      },
      border: { display: false }, 
      title: { display: false }, 
    },
    x: {
      grid: {
        display: false,
      },
      ticks: {
        font: { size: 11, family: "Inter, sans-serif" },
        color: "#64748b",
      },
      border: { display: false },
      title: { display: false },
    },
  },
};

export function GraficoModelos({ porModelo }: PropsGraficoModelos) {
  return (
    <Card titulo="Rentabilidade por Tipo de Veículo">
      <div className="ui-card-chart" style={{ width: "100%", height: "300px" }}>
        <Bar data={montarDados(porModelo)} options={opcoes} />
      </div>
    </Card>
  );
}

export default GraficoModelos;