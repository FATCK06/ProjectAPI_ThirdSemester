import { useRef, useState } from "react";
import type { MesesReferencia } from "../../../../services/Dashboard";
import { formatarMesCurto } from "../../../../utils/datas";
import "./SeletorMes.css";

type Modo = "atual" | "anterior" | "selecionar";

interface PropsSeletorMes {
  meses: MesesReferencia;
  valor: string;
  onChange: (mes: string) => void;
}

export function SeletorMes({ meses, valor, onChange }: PropsSeletorMes) {
  // O modo é guardado à parte: escolher no calendário o próprio mês atual
  // deve manter "Selecionar mês" marcado
  const [modo, setModo] = useState<Modo>(
    valor === meses.mesAtual
      ? "atual"
      : valor === meses.mesAnterior
        ? "anterior"
        : "selecionar",
  );
  const campoRef = useRef<HTMLInputElement>(null);

  function escolher(novoModo: "atual" | "anterior") {
    setModo(novoModo);
    onChange(novoModo === "atual" ? meses.mesAtual : meses.mesAnterior);
  }

  // O campo fica invisível embaixo do botão: só serve para abrir o calendário
  // do navegador ali. O modo só muda quando um mês é de fato escolhido.
  function abrirCalendario() {
    const campo = campoRef.current;
    if (!campo) return;
    try {
      campo.showPicker();
    } catch {
      // Navegador sem showPicker para type="month": o foco ao menos abre a edição
      campo.focus();
    }
  }

  return (
    <div className="seletor-mes" role="group" aria-label="Mês de referência">
      <button
        type="button"
        aria-pressed={modo === "atual"}
        onClick={() => escolher("atual")}
      >
        Mês atual
        <span>{formatarMesCurto(meses.mesAtual)}</span>
      </button>
      <button
        type="button"
        aria-pressed={modo === "anterior"}
        onClick={() => escolher("anterior")}
      >
        Mês anterior
        <span>{formatarMesCurto(meses.mesAnterior)}</span>
      </button>
      <div className="seletor-mes-selecionar">
        <button
          type="button"
          aria-pressed={modo === "selecionar"}
          onClick={abrirCalendario}
        >
          Selecionar mês
          {modo === "selecionar" && <span>{formatarMesCurto(valor)}</span>}
        </button>
        <input
          ref={campoRef}
          className="seletor-mes-campo"
          type="month"
          aria-label="Escolher mês"
          tabIndex={-1}
          value={valor}
          max={meses.mesAtual}
          onChange={(e) => {
            if (!e.target.value) return;
            setModo("selecionar");
            onChange(e.target.value);
          }}
        />
      </div>
    </div>
  );
}

export default SeletorMes;
