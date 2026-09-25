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
import type { IndicadoresMotorista } from "../../../services/Dashboard";

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
);

interface PropsGrafico {
  motoristas: IndicadoresMotorista[];
}

function montarDados(motoristas: IndicadoresMotorista[]): ChartData<"bar"> {
  const top = motoristas.slice(0, 10);
  return {
    labels: top.map((m) => m.nome ?? "Sem nome"),
    datasets: [
      {
        label: "Rentabilidade (R$)",
        data: top.map((m) => m.rentabilidade),
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
    title: { display: true, text: "Rentabilidade por motorista" },
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
    y: { beginAtZero: true },
  },
};

export function Grafico({ motoristas }: PropsGrafico) {
  return (
    <div className="card">
      <div className="card-header">
        <h2>Rentabilidade por motorista</h2>
      </div>
      <div className="card-chart">
        <Bar data={montarDados(motoristas)} options={opcoesDoGrafico} />
      </div>
    </div>
  );
}

export default Grafico;
