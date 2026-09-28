import { Bar } from "react-chartjs-2";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
  type ChartOptions,
  type ChartData,
} from "chart.js";
import type { IndicadoresPorModelo } from "../../../services/Dashboard";

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
);

interface PropsGrafico2 {
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

const opcoesDoGrafico: ChartOptions<"bar"> = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { position: "top" },
    title: { display: true, text: "Rentabilidade por modelo de veículo" },
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

export function Grafico2({ porModelo }: PropsGrafico2) {
  return (
    <div className="card">
      <div className="card-header">
        <h2>Rentabilidade por modelo de veículo</h2>
      </div>
      <div className="card-chart">
        <Bar data={montarDados(porModelo)} options={opcoesDoGrafico} />
      </div>
    </div>
  );
}

export default Grafico2;
