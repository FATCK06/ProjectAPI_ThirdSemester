import type { CSSProperties } from "react";
import { SkeletonBloco } from "../SkeletonBloco";
import "../skeleton.css";

interface PropsTableSkeleton {
  linhas?: number;
  colunas?: number;
}

// Larguras fixas, mas variadas: barras todas iguais parecem uma grade, não texto
const LARGURAS = ["70%", "45%", "60%", "35%", "55%", "40%", "65%", "50%"];

/** Tabela no formato do DataTable: cabeçalho cinza e linhas com texto de tamanhos variados. */
export function TableSkeleton({ linhas = 8, colunas = 6 }: PropsTableSkeleton) {
  const estiloColunas = { "--colunas": colunas } as CSSProperties;

  return (
    <div className="skeleton-tabela" role="status" aria-busy="true" aria-label="Carregando tabela">
      <div className="skeleton-tabela-linha skeleton-tabela-cabecalho" style={estiloColunas}>
        {Array.from({ length: colunas }, (_, c) => (
          <SkeletonBloco key={c} largura="50%" altura={10} />
        ))}
      </div>
      {Array.from({ length: linhas }, (_, l) => (
        <div key={l} className="skeleton-tabela-linha" style={estiloColunas}>
          {Array.from({ length: colunas }, (_, c) => (
            <SkeletonBloco key={c} largura={LARGURAS[(l + c) % LARGURAS.length]} altura={10} />
          ))}
        </div>
      ))}
    </div>
  );
}
