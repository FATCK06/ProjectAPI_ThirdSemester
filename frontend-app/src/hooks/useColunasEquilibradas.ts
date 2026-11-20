import { useLayoutEffect, useState, type CSSProperties, type RefObject } from "react";

// Cada coluna precisa de pelo menos 300px (mais os 16px de espaço entre elas)
const LARGURA_MINIMA = 300;
const ESPACO = 16;
const MAXIMO_COLUNAS = 4;

/**
 * Colunas da grade de indicadores. Primeiro vê quantas cabem na largura (até 4)
 * e quantas linhas isso dá; depois espalha os itens por igual nessas linhas.
 * Assim nunca sobra um card sozinho: 9 viram 3+3+3, 7 viram 4+3, 5 viram 3+2.
 * O resultado vai para a variável --colunas, usada pelo CSS da grade.
 */
export function useColunasEquilibradas(
  ref: RefObject<HTMLElement | null>,
  itens: number,
): CSSProperties {
  const [largura, setLargura] = useState(0);

  useLayoutEffect(() => {
    const elemento = ref.current;
    if (!elemento) return;
    const observador = new ResizeObserver(([entrada]) => setLargura(entrada.contentRect.width));
    observador.observe(elemento);
    return () => observador.disconnect();
  }, [ref]);

  const cabem = Math.max(
    1,
    Math.min(MAXIMO_COLUNAS, Math.floor((largura + ESPACO) / (LARGURA_MINIMA + ESPACO))),
  );
  const linhas = Math.ceil(Math.max(itens, 1) / cabem);
  const colunas = Math.ceil(Math.max(itens, 1) / linhas);

  return { "--colunas": colunas } as CSSProperties;
}
