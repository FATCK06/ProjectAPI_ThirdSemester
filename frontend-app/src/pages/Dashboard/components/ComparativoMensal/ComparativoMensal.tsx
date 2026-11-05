import { Aviso } from "../../../../components/Aviso/Aviso";
import { Card } from "../../../../components/Card/Card";
import { EmDesenvolvimento } from "../../../../components/EmDesenvolvimento";
import { GraficoSkeleton } from "../../../../components/SkeletonLoader/GraficoSkeleton";
import { formatarMesCurto } from "../../../../utils/datas";
import { MAXIMO_MESES, MINIMO_MESES, useComparativoMensal } from "./useComparativoMensal";
import "./ComparativoMensal.css";

const AVISO_MINIMO_MESES = "Selecione ao menos dois meses para comparar.";

export function ComparativoMensal() {
  const { meses, erroMeses, selecionados, alternarMes } = useComparativoMensal();
  const noLimite = selecionados.length >= MAXIMO_MESES;

  if (erroMeses) {
    return <p className="dashboard-erro">{erroMeses}</p>;
  }
  if (meses === null) {
    return <GraficoSkeleton altura={120} barras={6} />;
  }

  return (
    <Card
      titulo="Comparativo mensal"
      subtitulo="Indicadores gerais, somando todos os motoristas, nos meses já importados"
    >
      <div className="comparativo-meses" role="group" aria-label="Meses para comparar">
        {meses.map((mes) => {
          const selecionado = selecionados.includes(mes);
          return (
            <button
              key={mes}
              type="button"
              aria-pressed={selecionado}
              disabled={noLimite && !selecionado}
              onClick={() => alternarMes(mes)}
            >
              {formatarMesCurto(mes)}
            </button>
          );
        })}
      </div>
      {noLimite && (
        <p className="comparativo-limite">Limite de {MAXIMO_MESES} meses por comparação.</p>
      )}
      {selecionados.length < MINIMO_MESES ? (
        <Aviso mensagem={AVISO_MINIMO_MESES} />
      ) : (
        <EmDesenvolvimento mensagem="O gráfico e os cards de comparação estarão disponíveis em breve." />
      )}
    </Card>
  );
}

export default ComparativoMensal;
