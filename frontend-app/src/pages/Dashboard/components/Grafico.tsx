import { Bar } from "react-chartjs-2";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
  Filler,
  type ChartOptions,
  type ChartData,
} from "chart.js";

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
  Filler,
);

const dadosDoGrafico: ChartData<"bar"> = {
  labels: ["Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho"],

  datasets: [
    {
      label: "Motoristas ativos",
      data: [128, 145, 162, 178, 193, 215],

      backgroundColor: "rgba(29, 78, 216, 0.85)",
      borderColor: "#1d4ed8",

      borderWidth: 5,
      borderRadius: 8,

      barPercentage: 0.8,
      categoryPercentage: 0.8,
    },
  ],
};

const opcoesDoGrafico: ChartOptions<"bar"> = {
  responsive: true,
  maintainAspectRatio: false,

  plugins: {
    legend: {
      position: "top",
    },

    title: {
      display: true,
      text: "Motoristas ativos em 2026",
    },

    tooltip: {
      callbacks: {
        label: (context) => {
          return `${context.parsed.y} motoristas`;
        },
      },
    },
  },

  scales: {
    y: {
      beginAtZero: true,

      ticks: {
        precision: 0,
      },
    },
  },
};

export function Grafico() {
  return (
    <div className="card">
      <div className="card-header">
        <h2>Gráfico de Coluna</h2>
      </div>

      <div className="card-chart">
        <Bar data={dadosDoGrafico} options={opcoesDoGrafico} />
      </div>
    </div>
  );
}

export default Grafico;
