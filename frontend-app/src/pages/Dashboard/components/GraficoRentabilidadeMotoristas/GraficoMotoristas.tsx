import { Bar } from "react-chartjs-2";
import type { ChartData, ChartOptions } from "chart.js";
import "../../../../components/chartSetup";
import { Card } from "../../../../components/Card/Card";
import type { IndicadoresMotorista } from "../../../../services/Dashboard";

interface PropsGraficoMotoristas {
  motoristas: IndicadoresMotorista[];
}

function abreviarNome(nome?: string): string {
  const partes = nome?.trim().split(/\s+/).filter(Boolean) ?? [];
  if (partes.length === 0) return "Sem nome";
  return partes.length > 1 ? `${partes[0]} ${partes[partes.length - 1]}` : partes[0];
}

function montarDados(motoristas: IndicadoresMotorista[]): ChartData<"bar"> {
  return {
    labels: motoristas.map((m) => abreviarNome(m.nome)),
    datasets: [
      {
        label: "Rentabilidade (R$)",
        data: motoristas.map((m) => m.rentabilidade),
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
      legend: { display: false },
      tooltip: {
        callbacks: {
          title: (items) => motoristas[items[0]?.dataIndex]?.nome ?? "Sem nome",
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

export function GraficoMotoristas({ motoristas }: PropsGraficoMotoristas) {
  const top = motoristas.slice(0, 10);

  return (
    <Card titulo="Rentabilidade por motorista" subtitulo="Top 10 do mês">
      <div className="ui-card-chart">
        <Bar data={montarDados(top)} options={montarOpcoes(top)} />
      </div>
    </Card>
  );
}

export default GraficoMotoristas;