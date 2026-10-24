import type { ReactNode } from "react";
import "./paginacao.css";

interface PropsPaginacao {
  pagina: number;
  totalPaginas: number;
  aoMudar: (pagina: number) => void;
  contagem?: ReactNode;
  rotulo?: string;
}

export function Paginacao({
  pagina,
  totalPaginas,
  aoMudar,
  contagem,
  rotulo = "Paginação",
}: PropsPaginacao) {
  const ultima = Math.max(totalPaginas, 1);

  return (
    <nav className="paginacao" aria-label={rotulo}>
      <span className="paginacao-contagem">{contagem}</span>
      <div className="paginacao-controles">
        <button
          type="button"
          aria-label="Página anterior"
          disabled={pagina <= 1}
          onClick={() => aoMudar(pagina - 1)}
        >
          Anterior
        </button>
        <span aria-live="polite">
          Página {pagina} de {ultima}
        </span>
        <button
          type="button"
          aria-label="Próxima página"
          disabled={pagina >= ultima}
          onClick={() => aoMudar(pagina + 1)}
        >
          Próxima
        </button>
      </div>
    </nav>
  );
}
