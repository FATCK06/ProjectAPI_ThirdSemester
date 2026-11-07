import { Sparkline } from "../../Dashboard/components/IndicadoresCard/Sparkline";
import { formatarMoeda, formatarPercentual } from "../../../utils/formatacao";
import "./comparativoMotorista.css";

type Indicadores = { valorFrete?: number; custoTotal?: number; rentabilidade?: number; rentabilidadeMediaViagem?: number; margem?: number } | null | undefined;
type Metrica = { id: string; titulo: string; valores: (number | null | undefined)[]; formatar: (valor: number) => string };

function rotuloMes(mes: string) {
  const [ano, numero] = mes.split("-").map(Number);
  const nomeMes = new Intl.DateTimeFormat("pt-BR", { month: "short" })
    .format(new Date(ano, numero - 1, 1)).replace(".", "");
  return `${nomeMes}/${String(ano).slice(-2)}`;
}

function variacaoPercentual(inicial: number, atual: number) {
  if (inicial === 0) return null;
  return ((atual - inicial) / Math.abs(inicial)) * 100;
}

function formatarVariacao(valor: number) {
  const sinal = valor > 0 ? "+" : "";
  return `${sinal}${valor.toLocaleString("pt-BR", { maximumFractionDigits: 1 })}%`;
}

function GraficoComparacao({ metrica, meses }: { metrica: Metrica; meses: string[] }) {
  const disponiveis = meses.flatMap((mes, indice) => {
    const valor = metrica.valores[indice];
    return typeof valor === "number" && Number.isFinite(valor) ? [{ mes, valor }] : [];
  });
  const valores = disponiveis.map(({ valor }) => valor);
  const variacao = disponiveis.length > 1 ? variacaoPercentual(valores[0], valores[valores.length - 1]) : null;
  const sentido = variacao === null ? "indisponivel" : variacao > 0 ? "alta" : variacao < 0 ? "queda" : "estavel";
  const tituloGrafico = `${metrica.titulo}: ${disponiveis.map(({ mes, valor }) => `${rotuloMes(mes)} ${metrica.formatar(valor)}`).join("; ")}`;

  return (
    <article className="comparativo-motorista-card" data-resultado-unico={disponiveis.length === 1}>
      <div className="comparativo-motorista-card-cabecalho">
        <h3>{metrica.titulo}</h3>
        {disponiveis.length > 1 && (
          <span className="comparativo-motorista-variacao" data-sentido={sentido}>
            {variacao === null ? "Sem base para %" : `${variacao > 0 ? "↑" : variacao < 0 ? "↓" : "•"} ${formatarVariacao(variacao)}`}
          </span>
        )}
      </div>
      {disponiveis.length > 0 ? (
        <>
          <Sparkline
            key={`${meses.join("-")}-${valores.join("-")}`}
            valores={valores}
            titulo={tituloGrafico}
            largura={disponiveis.length === 1 ? 40 : 360}
            altura={disponiveis.length === 1 ? 28 : 72}
            animada
            posicaoPontoUnico={0.5}
          />
          <div className="comparativo-motorista-valores">
            {(disponiveis.length === 1 ? disponiveis : meses.map((mes, indice) => ({ mes, valor: metrica.valores[indice] }))).map(({ mes, valor }) => {
              const disponivel = typeof valor === "number" && Number.isFinite(valor);
              return <span key={mes} data-indisponivel={!disponivel}><small>{rotuloMes(mes)}</small><strong>{disponivel ? metrica.formatar(valor) : "—"}</strong></span>;
            })}
          </div>
        </>
      ) : (
        <p className="comparativo-motorista-sem-dados">Sem dados nos três meses selecionados.</p>
      )}
    </article>
  );
}

export function ComparativoMotorista({ meses, utilizacao, efetividade, financeiros, mostrarFinanceiros }: {
  meses: string[];
  utilizacao: (number | undefined)[];
  efetividade: (number | null | undefined)[];
  financeiros: Indicadores[];
  mostrarFinanceiros: boolean;
}) {
  const metricas: Metrica[] = [
    { id: "utilizacao", titulo: "Utilização", valores: utilizacao, formatar: formatarPercentual },
    { id: "efetividade", titulo: "Efetividade", valores: efetividade, formatar: formatarPercentual },
  ];

  if (mostrarFinanceiros) {
    metricas.push(
      { id: "frete", titulo: "Fretes", valores: financeiros.map((item) => item?.valorFrete), formatar: formatarMoeda },
      { id: "custos", titulo: "Custos", valores: financeiros.map((item) => item?.custoTotal), formatar: formatarMoeda },
      { id: "rentabilidade", titulo: "Rentabilidade", valores: financeiros.map((item) => item?.rentabilidade), formatar: formatarMoeda },
      { id: "rentabilidade-media", titulo: "Rentabilidade média por viagem", valores: financeiros.map((item) => item?.rentabilidadeMediaViagem), formatar: formatarMoeda },
      { id: "margem", titulo: "Margem", valores: financeiros.map((item) => item?.margem), formatar: formatarPercentual },
    );
  }

  return (
    <section className="comparativo-motorista-secao" aria-labelledby="comparativo-motorista-titulo">
      <h2 id="comparativo-motorista-titulo">Comparação dos últimos três meses</h2>
      <p className="comparativo-motorista-periodo">{meses.map(rotuloMes).join(" · ")}</p>
      <div className="comparativo-motorista-grade">
        {metricas.map((metrica) => <GraficoComparacao key={metrica.id} metrica={metrica} meses={meses} />)}
      </div>
    </section>
  );
}
