import { formatarMesTitulo } from "../../utils/datas";
import "./seloMes.css";

interface PropsSeloMes {
  /** "AAAA-MM" */
  mes: string;
  /** "titulo" ao lado de um título (h1/h2); "texto" no meio de uma frase menor. */
  tamanho?: "titulo" | "texto";
}

/** Destaca de que mês são os dados da tela: "Agosto 2026". */
export function SeloMes({ mes, tamanho = "titulo" }: PropsSeloMes) {
  return (
    <span className="selo-mes" data-tamanho={tamanho}>
      {formatarMesTitulo(mes)}
    </span>
  );
}

export default SeloMes;
