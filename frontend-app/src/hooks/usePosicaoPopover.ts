import { useLayoutEffect, useState, type RefObject } from "react";

export interface PosicaoPopover {
  /** Abre para cima: não cabe abaixo do gatilho, mas cabe acima. */
  acima: boolean;
  /** Alinha pela borda direita do gatilho: alinhado à esquerda, sairia da tela. */
  direita: boolean;
}

const MARGEM_TELA = 8;
const POSICAO_PADRAO: PosicaoPopover = { acima: false, direita: false };

/**
 * Decide para que lado o popover abre, medindo antes da pintura (sem piscar na
 * posição errada). O CSS de cada componente usa o resultado como atributos
 * data-acima e data-direita.
 */
export function usePosicaoPopover(
  aberto: boolean,
  gatilhoRef: RefObject<HTMLElement | null>,
  popoverRef: RefObject<HTMLElement | null>,
): PosicaoPopover {
  const [posicao, setPosicao] = useState<PosicaoPopover>(POSICAO_PADRAO);

  useLayoutEffect(() => {
    const gatilho = gatilhoRef.current;
    const popover = popoverRef.current;
    if (!aberto || !gatilho || !popover) {
      setPosicao(POSICAO_PADRAO);
      return;
    }

    const g = gatilho.getBoundingClientRect();
    const altura = popover.offsetHeight;
    const largura = popover.offsetWidth;

    const espacoAbaixo = window.innerHeight - g.bottom - MARGEM_TELA;
    const espacoAcima = g.top - MARGEM_TELA;
    const acima = altura > espacoAbaixo && espacoAcima > espacoAbaixo;

    const saiPelaDireita = g.left + largura > window.innerWidth - MARGEM_TELA;
    const cabeAlinhadoDireita = g.right - largura >= MARGEM_TELA;
    const direita = saiPelaDireita && cabeAlinhadoDireita;

    setPosicao({ acima, direita });
  }, [aberto, gatilhoRef, popoverRef]);

  return posicao;
}
