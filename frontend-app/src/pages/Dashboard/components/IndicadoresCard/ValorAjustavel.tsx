import { useLayoutEffect, useRef, useState, type ReactNode } from "react";

interface PropsValorAjustavel {
  completo: string;
  // Versão curta ("R$ 1,76 mi"), usada só se o completo não couber nem na menor fonte
  compacto?: string;
  // Ao lado do valor, sem entrar na troca (ex.: o asterisco de aproximação)
  extra?: ReactNode;
}

/**
 * Valor do indicador numa linha só. A fonte já diminui com a largura do card
 * (IndicadoresCard.css); se ainda assim não couber, mostra a versão compacta com
 * o valor completo no tooltip. Uma cópia invisível do completo é medida o tempo
 * todo, então o card volta ao completo quando cresce de novo.
 */
export function ValorAjustavel({ completo, compacto, extra }: PropsValorAjustavel) {
  const linhaRef = useRef<HTMLDivElement>(null);
  const medidaRef = useRef<HTMLSpanElement>(null);
  const extraRef = useRef<HTMLSpanElement>(null);
  const [cabe, setCabe] = useState(true);

  useLayoutEffect(() => {
    const linha = linhaRef.current;
    const medida = medidaRef.current;
    if (!compacto || !linha || !medida) return;

    const observador = new ResizeObserver(() => {
      const disponivel = linha.clientWidth - (extraRef.current?.offsetWidth ?? 0);
      setCabe(medida.offsetWidth <= disponivel);
    });
    observador.observe(linha);
    return () => observador.disconnect();
  }, [compacto, completo]);

  const usarCompacto = Boolean(compacto) && !cabe;

  return (
    <div ref={linhaRef} className="indicador-valor">
      <span className="indicador-valor-texto" title={usarCompacto ? completo : undefined}>
        {usarCompacto ? compacto : completo}
      </span>
      {extra && <span ref={extraRef}>{extra}</span>}
      {compacto && (
        <span ref={medidaRef} className="indicador-valor-medida" aria-hidden="true">
          {completo}
        </span>
      )}
    </div>
  );
}

export default ValorAjustavel;
