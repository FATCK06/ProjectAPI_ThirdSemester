import { useId } from "react";

interface PropsSparkline {
  valores: number[];
  largura?: number;
  altura?: number;
}

export function Sparkline({
  valores,
  largura = 140,
  altura = 48,
}: PropsSparkline) {
  const idGradiente = useId().replace(/:/g, "");

  if (valores.length < 2) return null; // com menos de 2 pontos não há tendência

  const min = Math.min(...valores);
  const max = Math.max(...valores);
  const amplitude = max - min || 1; // evita divisão por zero se tudo for igual
  const margem = 4;
  const passoX = (largura - margem * 2) / (valores.length - 1);

  const pontos = valores.map((v, i) => {
    const x = margem + i * passoX;
    const y = margem + (1 - (v - min) / amplitude) * (altura - margem * 2);
    return [x, y] as const;
  });

  const linha = pontos
    .map(([x, y], i) => `${i === 0 ? "M" : "L"}${x.toFixed(1)} ${y.toFixed(1)}`)
    .join(" ");

  const [ultimoX] = pontos[pontos.length - 1];
  const [primeiroX] = pontos[0];
  const area = `${linha} L${ultimoX.toFixed(1)} ${altura} L${primeiroX.toFixed(1)} ${altura} Z`;

  return (
    <svg
      className="sparkline"
      width={largura}
      height={altura}
      viewBox={`0 0 ${largura} ${altura}`}
      aria-hidden="true"
    >
      <defs>
        <linearGradient id={idGradiente} x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stopColor="currentColor" stopOpacity="0.25" />
          <stop offset="100%" stopColor="currentColor" stopOpacity="0" />
        </linearGradient>
      </defs>
      <path d={area} fill={`url(#${idGradiente})`} />
      <path
        d={linha}
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}