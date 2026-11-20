import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  LineElement,
  PointElement,
  Tooltip,
  Legend,
} from "chart.js";

// Só o que os gráficos usam: o Chart.js descarta do build o que não é registrado.
// O plugin Title ficou de fora porque nenhum gráfico exibe título próprio.
ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  LineElement,
  PointElement,
  Tooltip,
  Legend,
);
