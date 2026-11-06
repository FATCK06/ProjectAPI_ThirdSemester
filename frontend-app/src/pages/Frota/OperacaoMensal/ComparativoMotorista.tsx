import { Sparkline } from "../../Dashboard/components/IndicadoresCard/Sparkline";
import { formatarMoeda, formatarPercentual } from "../../../utils/formatacao";
import "./comparativoMotorista.css";

type Metrica = {
  id: string;
  titulo: string;
  anterior: number | null | undefined;
  atual: number | null | undefined;
  formatar: (valor: number) => string;
};

function rotuloMes(mes: string) {
  const [ano, numero] = mes.split("-").map(Number);
  return new Intl.DateTimeFormat("pt-BR", { month: "short", year: "numeric" })
    .format(new Date(ano, numero - 1, 1));
}

function variacaoPercentual(anterior: number, atual: number) {
  if (anterior === 0) return null;
  return ((atual - anterior) / Math.abs(anterior)) * 100;
}

function formatarVariacao(valor: number) {
  const sinal = valor > 0 ? "+" : "";
  return `${sinal}${valor.toLocaleString("pt-BR", { maximumFractionDigits: 1 })}%`;
}

function GraficoComparacao({ metrica, mesAnterior, mesAtual }: {
  metrica: Metrica;
  mesAnterior: string;
  mesAtual: string;
}) {
  const valores = [metrica.anterior, metrica.atual];
  const temDados = valores.every((valor) => typeof valor === "number" && Number.isFinite(valor));
  const valoresValidos = temDados ? valores as number[] : [];
  const variacao = temDados ? variacaoPercentual(valoresValidos[0], valoresValidos[1]) : null;
  const sentido = variacao === null ? "indisponivel" : variacao > 0 ? "alta" : variacao < 0 ? "queda" : "estavel";
  const tituloGrafico = `${metrica.titulo}: ${rotuloMes(mesAnterior)} ${metrica.formatar(valoresValidos[0])}; ${rotuloMes(mesAtual)} ${metrica.formatar(valoresValidos[1])}`;

  return (
    <article className="comparativo-motorista-card">
      <div className="comparativo-motorista-card-cabecalho">
        <h3>{metrica.titulo}</h3>
        {temDados && (
          <span className="comparativo-motorista-variacao" data-sentido={sentido}>
            {variacao === null
              ? "Sem base para %"
              : `${variacao > 0 ? "↑" : variacao < 0 ? "↓" : "•"} ${formatarVariacao(variacao)}`}
          </span>
        )}
      </div>
      {temDados ? (
        <>
          <Sparkline
            key={`${mesAnterior}-${mesAtual}-${valoresValidos.join("-")}`}
            valores={valoresValidos}
            titulo={tituloGrafico}
            largura={240}
            altura={72}
            animada
          />
          <div className="comparativo-motorista-valores">
            <span><small>{rotuloMes(mesAnterior)}</small><strong>{metrica.formatar(valoresValidos[0])}</strong></span>
            <span><small>{rotuloMes(mesAtual)}</small><strong>{metrica.formatar(valoresValidos[1])}</strong></span>
          </div>
        </>
      ) : (
        <p className="comparativo-motorista-sem-dados">Sem dados nos dois meses para comparar.</p>
      )}
    </article>
  );
}

export function ComparativoMotorista({
  mesAnterior,
  mesAtual,
  utilizacaoAnterior,
  utilizacaoAtual,
  efetividadeAnterior,
  efetividadeAtual,
  financeirosAnterior,
  financeirosAtual,
  mostrarFinanceiros,
}: {
  mesAnterior: string;
  mesAtual: string;
  utilizacaoAnterior?: number;
  utilizacaoAtual?: number;
  efetividadeAnterior?: number | null;
  efetividadeAtual?: number | null;
  financeirosAnterior?: { valorFrete?: number; custoTotal?: number; rentabilidade?: number; rentabilidadeMediaViagem?: number; margem?: number };
  financeirosAtual?: { valorFrete?: number; custoTotal?: number; rentabilidade?: number; rentabilidadeMediaViagem?: number; margem?: number };
  mostrarFinanceiros: boolean;
}) {
  const metricas: Metrica[] = [
    { id: "utilizacao", titulo: "Utilização", anterior: utilizacaoAnterior, atual: utilizacaoAtual, formatar: formatarPercentual },
    { id: "efetividade", titulo: "Efetividade", anterior: efetividadeAnterior, atual: efetividadeAtual, formatar: formatarPercentual },
  ];

  if (mostrarFinanceiros) {
    metricas.push(
      { id: "frete", titulo: "Fretes", anterior: financeirosAnterior?.valorFrete, atual: financeirosAtual?.valorFrete, formatar: formatarMoeda },
      { id: "custos", titulo: "Custos", anterior: financeirosAnterior?.custoTotal, atual: financeirosAtual?.custoTotal, formatar: formatarMoeda },
      { id: "rentabilidade", titulo: "Rentabilidade", anterior: financeirosAnterior?.rentabilidade, atual: financeirosAtual?.rentabilidade, formatar: formatarMoeda },
      { id: "rentabilidade-media", titulo: "Rentabilidade média por viagem", anterior: financeirosAnterior?.rentabilidadeMediaViagem, atual: financeirosAtual?.rentabilidadeMediaViagem, formatar: formatarMoeda },
      { id: "margem", titulo: "Margem", anterior: financeirosAnterior?.margem, atual: financeirosAtual?.margem, formatar: formatarPercentual },
    );
  }

  return (
    <section className="comparativo-motorista-secao" aria-labelledby="comparativo-motorista-titulo">
      <h2 id="comparativo-motorista-titulo">Comparação com o mês anterior</h2>
      <p className="comparativo-motorista-periodo">{rotuloMes(mesAnterior)} vs. {rotuloMes(mesAtual)}</p>
      <div className="comparativo-motorista-grade">
        {metricas.map((metrica) => <GraficoComparacao key={metrica.id} metrica={metrica} mesAnterior={mesAnterior} mesAtual={mesAtual} />)}
      </div>
    </section>
  );
}
