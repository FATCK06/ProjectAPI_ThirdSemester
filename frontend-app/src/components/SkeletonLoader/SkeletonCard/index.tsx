import type { ReactNode } from "react";
import { SkeletonBloco } from "../SkeletonBloco";
import "../skeleton.css";

interface PropsSkeletonCard {
  children: ReactNode;
  // Lido por leitor de tela no lugar do conteúdo
  rotulo: string;
  // Reserva o espaço do sobretítulo (ex.: "Visão geral" no card de indicadores)
  comSobretitulo?: boolean;
  // Larguras dos controles no canto do cabeçalho (ex.: [200, 96] = seletor + botão Filtros)
  acoes?: number[];
}

/** Moldura com a mesma caixa e o mesmo cabeçalho do Card, para o conteúdo entrar sem pular. */
export function SkeletonCard({
  children,
  rotulo,
  comSobretitulo = false,
  acoes = [],
}: PropsSkeletonCard) {
  return (
    <div className="skeleton-card" role="status" aria-busy="true" aria-label={rotulo}>
      <div className="skeleton-card-cabecalho">
        <div className="skeleton-card-titulos">
          {comSobretitulo && <SkeletonBloco largura={80} altura={10} />}
          <SkeletonBloco largura="40%" altura={20} />
          <SkeletonBloco largura="60%" altura={12} />
        </div>
        {acoes.length > 0 && (
          <div className="skeleton-card-acoes">
            {acoes.map((largura, i) => (
              <SkeletonBloco key={i} largura={largura} altura={30} raio={8} />
            ))}
          </div>
        )}
      </div>
      {children}
    </div>
  );
}
