import { useEffect, type RefObject } from "react";

/**
 * Fecha um menu ou popover aberto quando o usuário clica fora do container ou
 * aperta Esc. Só escuta enquanto `aberto` for true.
 */
export function useFecharAoClicarFora(
  containerRef: RefObject<HTMLElement | null>,
  aberto: boolean,
  fechar: () => void,
) {
  useEffect(() => {
    if (!aberto) return;

    function aoClicarFora(evento: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(evento.target as Node)) {
        fechar();
      }
    }

    function aoPressionarTecla(evento: KeyboardEvent) {
      if (evento.key === "Escape") fechar();
    }

    document.addEventListener("mousedown", aoClicarFora);
    document.addEventListener("keydown", aoPressionarTecla);
    return () => {
      document.removeEventListener("mousedown", aoClicarFora);
      document.removeEventListener("keydown", aoPressionarTecla);
    };
  }, [containerRef, aberto, fechar]);
}
