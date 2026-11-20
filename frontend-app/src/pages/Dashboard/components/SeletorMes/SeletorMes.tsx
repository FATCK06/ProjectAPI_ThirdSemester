import { useCallback, useRef, useState } from "react";
import { CalendarioMes } from "../../../../components/CampoMes/CalendarioMes";
import { useFecharAoClicarFora } from "../../../../hooks/useFecharAoClicarFora";
import { usePosicaoPopover } from "../../../../hooks/usePosicaoPopover";
import type { MesesReferencia } from "../../../../services/Dashboard";
import { formatarMesCurto } from "../../../../utils/datas";
import "./SeletorMes.css";

type Modo = "atual" | "anterior" | "selecionar";

interface PropsSeletorMes {
  meses: MesesReferencia;
  valor: string;
  onChange: (mes: string) => void;
}

function modoDe(valor: string, meses: MesesReferencia): Modo {
  if (valor === meses.mesAtual) return "atual";
  if (valor === meses.mesAnterior) return "anterior";
  return "selecionar";
}

export function SeletorMes({ meses, valor, onChange }: PropsSeletorMes) {
  // O modo é guardado à parte: escolher no calendário o próprio mês atual
  // deve manter "Selecionar mês" marcado
  const [modo, setModo] = useState<Modo>(() => modoDe(valor, meses));
  // Mês trocado por fora (voltar do navegador, link com ?mes): se o botão marcado
  // não corresponde mais a ele, acompanha. Ajuste durante o render, sem efeito
  const coerente =
    modo === "selecionar" || valor === (modo === "atual" ? meses.mesAtual : meses.mesAnterior);
  if (!coerente) setModo(modoDe(valor, meses));
  const [calendarioAberto, setCalendarioAberto] = useState(false);
  const ancoraRef = useRef<HTMLDivElement>(null);
  const botaoRef = useRef<HTMLButtonElement>(null);
  const popoverRef = useRef<HTMLDivElement>(null);
  const posicao = usePosicaoPopover(calendarioAberto, botaoRef, popoverRef);

  const fecharCalendario = useCallback(() => setCalendarioAberto(false), []);
  useFecharAoClicarFora(ancoraRef, calendarioAberto, fecharCalendario);

  function escolher(novoModo: "atual" | "anterior") {
    setModo(novoModo);
    onChange(novoModo === "atual" ? meses.mesAtual : meses.mesAnterior);
  }

  // O modo só muda quando um mês é de fato escolhido no calendário
  function escolherNoCalendario(mes: string) {
    setCalendarioAberto(false);
    botaoRef.current?.focus();
    setModo("selecionar");
    onChange(mes);
  }

  return (
    <div className="seletor-mes" role="group" aria-label="Mês de referência">
      <button
        type="button"
        className="seletor-mes-opcao"
        aria-pressed={modo === "atual"}
        onClick={() => escolher("atual")}
      >
        Mês atual
        <span>{formatarMesCurto(meses.mesAtual)}</span>
      </button>
      <button
        type="button"
        className="seletor-mes-opcao"
        aria-pressed={modo === "anterior"}
        onClick={() => escolher("anterior")}
      >
        Mês anterior
        <span>{formatarMesCurto(meses.mesAnterior)}</span>
      </button>
      <div
        ref={ancoraRef}
        className="seletor-mes-selecionar"
        onBlur={(evento) => {
          const destino = evento.relatedTarget as Node | null;
          if (destino && !evento.currentTarget.contains(destino)) setCalendarioAberto(false);
        }}
      >
        <button
          ref={botaoRef}
          type="button"
          className="seletor-mes-opcao"
          aria-pressed={modo === "selecionar"}
          aria-haspopup="dialog"
          aria-expanded={calendarioAberto}
          onClick={() => setCalendarioAberto((atual) => !atual)}
        >
          Selecionar mês
          {modo === "selecionar" && <span>{formatarMesCurto(valor)}</span>}
        </button>
        {calendarioAberto && (
          <div
            ref={popoverRef}
            className="seletor-mes-popover"
            role="dialog"
            aria-label="Escolher mês"
            data-acima={posicao.acima || undefined}
            data-direita={posicao.direita || undefined}
            onKeyDown={(evento) => {
              if (evento.key === "Escape") {
                evento.stopPropagation();
                setCalendarioAberto(false);
                botaoRef.current?.focus();
              }
            }}
          >
            {/* Mês atual já tem botão próprio ao lado: sem o atalho do rodapé */}
            <CalendarioMes
              valor={valor}
              max={meses.mesAtual}
              atalhoMesAtual={false}
              onSelecionar={escolherNoCalendario}
            />
          </div>
        )}
      </div>
    </div>
  );
}

export default SeletorMes;
