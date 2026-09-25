import { type IndicadoresMes } from "../../../services/Dashboard";

interface PropsCard {
  indicadores: IndicadoresMes;
}

function formatarReais(valor: number): string {
  return valor.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

export function Card({ indicadores }: PropsCard) {
  return (
    <div className="card-indicadores">
      <div className="card-header">
        <h2>Indicadores de {indicadores.mesReferencia}</h2>
      </div>

      <div className="card-indicadores-grid">
        <div>
          <strong>Nº de viagens</strong>
          <p>{indicadores.totais.numeroViagens}</p>
        </div>
        <div>
          <strong>Dias disponíveis</strong>
          <p>{indicadores.diasDisponiveis}</p>
        </div>
        <div>
          <strong>% de utilização</strong>
          <p>{indicadores.totais.utilizacaoMedia}%</p>
        </div>
        <div>
          <strong>Valor total dos fretes</strong>
          <p>{formatarReais(indicadores.totais.valorFrete)}</p>
        </div>
        <div>
          <strong>Custo total</strong>
          <p>{formatarReais(indicadores.totais.custoTotal)}</p>
        </div>
        <div>
          <strong>Rentabilidade total</strong>
          <p>{formatarReais(indicadores.totais.rentabilidade)}</p>
        </div>
        <div>
          <strong>Rentabilidade média/viagem</strong>
          <p>{formatarReais(indicadores.totais.rentabilidadeMediaViagem)}</p>
        </div>
        <div>
          <strong>Margem</strong>
          <p>{indicadores.totais.margem}%</p>
        </div>
      </div>
    </div>
  );
}

export default Card;
