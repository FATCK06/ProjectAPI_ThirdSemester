import type { CSSProperties } from "react";
import "../skeleton.css";

interface PropsSkeletonBloco {
  largura?: CSSProperties["width"];
  altura?: CSSProperties["height"];
  raio?: CSSProperties["borderRadius"];
  className?: string;
}

/** Peça básica dos skeletons: uma barra cinza animada no tamanho pedido. */
export function SkeletonBloco({
  largura = "100%",
  altura = 12,
  raio,
  className,
}: PropsSkeletonBloco) {
  return (
    <span
      className={["skeleton-bloco", "shimmer", className].filter(Boolean).join(" ")}
      style={{ width: largura, height: altura, borderRadius: raio }}
      aria-hidden="true"
    />
  );
}
