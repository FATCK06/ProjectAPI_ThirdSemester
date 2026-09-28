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
    labels: top.map((motorista) => {
      const nomes = motorista.nome?.trim().split(/\s+/).filter(Boolean) ?? [];
      if (nomes.length === 0) return "Sem nome";
      return nomes.length > 1
        ? `${nomes[0]} ${nomes[nomes.length - 1]}`
        : nomes[0];
    }),
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

function montarOpcoes(motoristas: IndicadoresMotorista[]): ChartOptions<"bar"> {
  return {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { position: "top" },
    tooltip: {
      callbacks: {
        title: (items) =>
          motoristas[items[0]?.dataIndex]?.nome ?? "Sem nome",
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
      ticks: { stepSize: 5000, precision: 0 },
    },
    x: {
      ticks: {
        font: { size: 9 },
        maxRotation: 60,
        minRotation: 45,
        autoSkip: false,
      },
    },
  },
  };
}

export function Grafico({ motoristas }: PropsGrafico) {
  return (
    <div className="card">
      <div className="card-header">
        <h2>Rentabilidade por motorista</h2>
      </div>
      <div className="card-chart">
        <Bar data={montarDados(motoristas)} options={montarOpcoes(motoristas)} />
      </div>
    </div>
  );
}

export default Grafico;
