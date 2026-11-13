import { useId } from "react";

interface PropsSparkline {
  valores: number[];
  // Meses e valores por extenso: vira o tooltip e o texto para leitor de tela
  titulo?: string;
  largura?: number;
  altura?: number;
  animada?: boolean;
  posicaoPontoUnico?: number;
}

export function Sparkline({
  valores,
  titulo,
  largura = 140,
  altura = 48,
  animada = false,
  posicaoPontoUnico = 0.5,
}: PropsSparkline) {
  const idGradiente = useId().replace(/:/g, "");

  if (valores.length === 1) {
    const margem = 4;
    const x = margem + Math.max(0, Math.min(1, posicaoPontoUnico)) * (largura - margem * 2);
    const y = altura / 2;
    return (
      <svg className={`sparkline sparkline--unico${animada ? " sparkline--animada" : ""}`} width={largura} height={altura}
        viewBox={`0 0 ${largura} ${altura}`} preserveAspectRatio="xMidYMid meet"
        {...(titulo ? { role: "img", "aria-label": titulo } : { "aria-hidden": true })}>
        {titulo && <title>{titulo}</title>}
        <circle className="sparkline-dot sparkline-dot--atual" cx={x} cy={y} r="3.5" />
      </svg>
    );
  }

  if (valores.length < 1) return null;

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

  const [xInicial, yInicial] = pontos[0];
  const linha = pontos.slice(1).reduce((caminho, [x, y], indice) => {
    const [xAnterior, yAnterior] = pontos[indice];
    const distancia = x - xAnterior;
    const controle1 = xAnterior + distancia * 0.45;
    const controle2 = x - distancia * 0.45;
    return `${caminho} C${controle1.toFixed(1)} ${yAnterior.toFixed(1)} ${controle2.toFixed(1)} ${y.toFixed(1)} ${x.toFixed(1)} ${y.toFixed(1)}`;
  }, `M${xInicial.toFixed(1)} ${yInicial.toFixed(1)}`);

  const [ultimoX] = pontos[pontos.length - 1];
  const [primeiroX] = pontos[0];
  const area = `${linha} L${ultimoX.toFixed(1)} ${altura} L${primeiroX.toFixed(1)} ${altura} Z`;

  return (
    <svg
      className={`sparkline${animada ? " sparkline--animada" : ""}`}
      width={largura}
      height={altura}
      viewBox={`0 0 ${largura} ${altura}`}
      preserveAspectRatio="xMidYMid meet"
      {...(titulo ? { role: "img", "aria-label": titulo } : { "aria-hidden": true })}
    >
      {titulo && <title>{titulo}</title>}
      <defs>
        <linearGradient id={idGradiente} x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stopColor="currentColor" stopOpacity="0.25" />
          <stop offset="100%" stopColor="currentColor" stopOpacity="0" />
        </linearGradient>
      </defs>
      <path className="sparkline-area" d={area} fill={`url(#${idGradiente})`} />
      <path
        className="sparkline-line"
        d={linha}
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
        pathLength={animada ? 1 : undefined}
      />
      {animada && pontos.map(([x, y], indice) => (
        <circle
          key={indice}
          className={`sparkline-dot${indice === pontos.length - 1 ? " sparkline-dot--atual" : ""}`}
          cx={x}
          cy={y}
          r={indice === pontos.length - 1 ? 3.5 : 2.75}
          style={{ animationDelay: `${indice * 120}ms` }}
        />
      ))}
    </svg>
  );
}
