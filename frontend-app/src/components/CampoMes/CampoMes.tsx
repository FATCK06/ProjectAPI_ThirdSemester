import { useCallback, useId, useRef, useState } from "react";
import { CalendarDays, ChevronDown } from "lucide-react";
import { useFecharAoClicarFora } from "../../hooks/useFecharAoClicarFora";
import { usePosicaoPopover } from "../../hooks/usePosicaoPopover";
import { formatarMesExtenso } from "../../utils/datas";
import { CalendarioMes } from "./CalendarioMes";
import "./campoMes.css";

interface PropsCampoMes {
  /** Liga o campo a um <label htmlFor>. */
  id?: string;
  /** "AAAA-MM", o mesmo formato do <input type="month">. */
  valor: string;
  onChange: (mes: string) => void;
  min?: string;
  max?: string;
  disabled?: boolean;
  "aria-label"?: string;
  className?: string;
}

/** "junho de 2026" -> "Junho de 2026" */
function rotuloMes(mes: string): string {
  const texto = formatarMesExtenso(mes);
  return texto.charAt(0).toLocaleUpperCase("pt-BR") + texto.slice(1);
}

/**
 * Substitui o <input type="month">: o calendário do navegador segue o idioma
 * do sistema (aparecia em inglês) e não aceita estilo.
 */
export function CampoMes({
  id,
  valor,
  onChange,
  min,
  max,
  disabled = false,
  "aria-label": ariaLabel,
  className,
}: PropsCampoMes) {
  const idGerado = useId();
  const idBase = id ?? idGerado;
  const idValor = `${idBase}-valor`;

  const [aberto, setAberto] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);
  const gatilhoRef = useRef<HTMLButtonElement>(null);
  const popoverRef = useRef<HTMLDivElement>(null);
  const posicao = usePosicaoPopover(aberto, gatilhoRef, popoverRef);

  const fechar = useCallback(() => setAberto(false), []);
  useFecharAoClicarFora(containerRef, aberto, fechar);

  function fecharDevolvendoFoco() {
    setAberto(false);
    gatilhoRef.current?.focus();
  }

  return (
    <div
      ref={containerRef}
      className={["campo-mes", className].filter(Boolean).join(" ")}
      data-aberto={aberto || undefined}
      // Tab para fora do calendário fecha. relatedTarget nulo é clique em área
      // não focável do próprio popover: o clique fora já é tratado pelo hook
      onBlur={(evento) => {
        const destino = evento.relatedTarget as Node | null;
        if (destino && !evento.currentTarget.contains(destino)) setAberto(false);
      }}
    >
      <button
        ref={gatilhoRef}
        id={idBase}
        type="button"
        className="campo-mes-gatilho"
        aria-haspopup="dialog"
        aria-expanded={aberto}
        aria-label={ariaLabel}
        // Com <label htmlFor>, o nome do botão vira o rótulo: o mês vai na descrição
        aria-describedby={idValor}
        disabled={disabled}
        onClick={() => setAberto((atual) => !atual)}
      >
        <CalendarDays className="campo-mes-icone" size={15} aria-hidden="true" />
        <span id={idValor} className="campo-mes-valor">
          {valor ? rotuloMes(valor) : "Selecione o mês"}
        </span>
        <ChevronDown className="campo-mes-seta" size={16} aria-hidden="true" />
      </button>

      {aberto && (
        <div
          ref={popoverRef}
          className="campo-mes-popover"
          role="dialog"
          aria-label="Escolher mês"
          data-acima={posicao.acima || undefined}
          data-direita={posicao.direita || undefined}
          onKeyDown={(evento) => {
            if (evento.key === "Escape") {
              evento.stopPropagation();
              fecharDevolvendoFoco();
            }
          }}
        >
          <CalendarioMes
            valor={valor || null}
            min={min}
            max={max}
            onSelecionar={(mes) => {
              fecharDevolvendoFoco();
              if (mes !== valor) onChange(mes);
            }}
          />
        </div>
      )}
    </div>
  );
}

export default CampoMes;
