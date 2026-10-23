import { Bar } from "react-chartjs-2";
import type { ChartData, ChartOptions } from "chart.js";
import "../../../../../components/chartSetup";
import { Card } from "../../../../../components/Card/Card";
import type { IndicadoresMotorista } from "../../../../../services/Dashboard";

interface PropsGraficoMotoristas {
  motoristas: IndicadoresMotorista[];
}

function abreviarNome(nome: string | null | undefined): string {
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
        data: motoristas.map((m) => m.rentabilidade ?? null),
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
        barPercentage: 0.65,
        categoryPercentage: 0.75,
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
        backgroundColor: "rgba(15, 23, 42, 0.9)", 
        titleFont: { size: 13, family: "Inter, sans-serif", weight: "bold" },
        bodyFont: { size: 12, family: "Inter, sans-serif" },
        padding: 12,
        cornerRadius: 8,
        boxPadding: 6,
        usePointStyle: true,
       
        callbacks: {
          title: (items) => {
            const index = items[0]?.dataIndex;
            return motoristas[index]?.nome ?? "Sem nome";
          },
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
          stepSize: 5000,
          font: { size: 11, family: "Inter, sans-serif" },
          color: "#64748b", 
          callback: (value) => `R$ ${Number(value).toLocaleString("pt-BR", { notation: "compact" })}`,
        },
        border: { display: false },
      },
      x: {
        grid: {
          display: false, 
        },
        ticks: {
          font: { size: 10, family: "Inter, sans-serif" },
          color: "#64748b",
          maxRotation: 30, 
          minRotation: 0,
          autoSkip: false,
        },
        border: { display: false },
      },
    },
  };
}

export function GraficoMotoristas({ motoristas }: PropsGraficoMotoristas) {
  const top = motoristas.slice(0, 10);

  return (
    <Card titulo="Rentabilidade por Motorista" subtitulo="Top 10 do mês">
      <div className="ui-card-chart" style={{ width: "100%", height: "320px" }}>
        <Bar data={montarDados(top)} options={montarOpcoes(top)} />
      </div>
    </Card>
  );
}

export default GraficoMotoristas;