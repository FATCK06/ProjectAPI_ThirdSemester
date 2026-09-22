import { Line } from "react-chartjs-2";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  type ChartOptions,
  type ChartData,
} from "chart.js";

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
);

const dadosDoGrafico: ChartData<"line"> = {
  labels: ["João", "Carlos", "Marcos", "Rafael", "André"],

  datasets: [
    {
      label: "Trajetos realizados",
      data: [24, 21, 16, 60, 36],

      borderColor: "#4BC0C0",
      backgroundColor: "rgba(75, 192, 192, 0.15)",

      borderWidth: 3,

      pointBackgroundColor: "#4BC0C0",
      pointBorderColor: "#ffffff",
      pointBorderWidth: 2,
      pointRadius: 5,

      tension: 0.4,
      fill: true,
    },
  ],
};

const opcoesDoGrafico: ChartOptions<"line"> = {
  responsive: true,
  maintainAspectRatio: false,

  plugins: {
    legend: {
      position: "top",
    },

    title: {
      display: true,
      text: "Top 5 motoristas com mais trajetos no mês",
    },

    tooltip: {
      callbacks: {
        label: (context) => {
          return `${context.parsed.y} trajetos`;
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

      title: {
        display: true,
        text: "Quantidade de trajetos",
      },
    },

    x: {
      title: {
        display: true,
        text: "Motoristas",
      },
    },
  },
};

export function Grafico2() {
  return (
    <div className="card">
      <div className="card-header">
        <h2>Gráfico de Linha</h2>
      </div>

      <div className="card-chart">
        <Line data={dadosDoGrafico} options={opcoesDoGrafico} />
      </div>
    </div>
  );
}

export default Grafico2;
