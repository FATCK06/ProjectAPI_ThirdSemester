import { useEffect, useRef, useState, type KeyboardEvent } from "react";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { mesAtual } from "../../utils/datas";
import "./campoMes.css";

// Fixo em pt-BR: o <input type="month"> segue o idioma do navegador e aparecia em inglês
const MESES_CURTOS = ["jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez"];
const MESES_EXTENSO = [
  "janeiro", "fevereiro", "março", "abril", "maio", "junho",
  "julho", "agosto", "setembro", "outubro", "novembro", "dezembro",
];

interface PropsCalendarioMes {
  /** "AAAA-MM"; null quando nada está escolhido. */
  valor: string | null;
  onSelecionar: (mes: string) => void;
  /** Limites inclusivos, em "AAAA-MM". */
  min?: string;
  max?: string;
  /** Mostra o botão "Este mês" no rodapé. */
  atalhoMesAtual?: boolean;
}

function montarMes(ano: number, indice: number): string {
  return `${ano}-${String(indice + 1).padStart(2, "0")}`;
}

function separarMes(mes: string): { ano: number; indice: number } {
  const [ano, numero] = mes.split("-").map(Number);
  return { ano, indice: numero - 1 };
}

/**
 * Grade de 12 meses com navegação por ano. Ao abrir, o foco vai para o mês
 * escolhido (ou o atual). Teclado: setas andam pelos meses (passando de ano
 * nas bordas), PageUp/PageDown trocam o ano, Enter/Espaço escolhem.
 */
export function CalendarioMes({ valor, onSelecionar, min, max, atalhoMesAtual = true }: PropsCalendarioMes) {
  const hoje = mesAtual();
  const inicial = separarMes(valor ?? (max && hoje > max ? max : hoje));
  const [ano, setAno] = useState(inicial.ano);
  const [foco, setFoco] = useState(inicial.indice);
  const gradeRef = useRef<HTMLDivElement>(null);
  // Só ao abrir e ao andar pelo teclado: trocar o ano pelas setas do cabeçalho
  // não pode tirar o foco do botão clicado
  const focarMes = useRef(true);

  useEffect(() => {
    if (!focarMes.current) return;
    focarMes.current = false;
    gradeRef.current?.querySelector<HTMLButtonElement>(`[data-indice="${foco}"]`)?.focus();
  }, [ano, foco]);

  function foraDoLimite(mes: string): boolean {
    return (min !== undefined && mes < min) || (max !== undefined && mes > max);
  }

  const anoMinimo = min ? separarMes(min).ano : -Infinity;
  const anoMaximo = max ? separarMes(max).ano : Infinity;

  function irPara(novoAno: number, novoIndice: number) {
    if (novoAno < anoMinimo || novoAno > anoMaximo) return;
    focarMes.current = true;
    setAno(novoAno);
    setFoco(novoIndice);
  }

  function aoPressionarTecla(evento: KeyboardEvent<HTMLDivElement>) {
    const deslocamentos: Record<string, number> = {
      ArrowLeft: -1,
      ArrowRight: 1,
      ArrowUp: -3,
      ArrowDown: 3,
    };

    if (evento.key in deslocamentos) {
      evento.preventDefault();
      const absoluto = ano * 12 + foco + deslocamentos[evento.key];
      irPara(Math.floor(absoluto / 12), ((absoluto % 12) + 12) % 12);
    } else if (evento.key === "PageUp" || evento.key === "PageDown") {
      evento.preventDefault();
      irPara(ano + (evento.key === "PageUp" ? -1 : 1), foco);
    } else if (evento.key === "Home" || evento.key === "End") {
      evento.preventDefault();
      const alvo = evento.key === "Home" ? 0 : 11;
      if (alvo === foco) return;
      focarMes.current = true;
      setFoco(alvo);
    }
  }

  const atualForaDoLimite = foraDoLimite(hoje);

  return (
    <div className="calendario-mes">
      <div className="calendario-mes-cabecalho">
        <button
          type="button"
          className="calendario-mes-nav"
          aria-label={`Ano anterior (${ano - 1})`}
          disabled={ano - 1 < anoMinimo}
          onClick={() => setAno(ano - 1)}
        >
          <ChevronLeft size={16} />
        </button>
        <span className="calendario-mes-ano" aria-live="polite">{ano}</span>
        <button
          type="button"
          className="calendario-mes-nav"
          aria-label={`Próximo ano (${ano + 1})`}
          disabled={ano + 1 > anoMaximo}
          onClick={() => setAno(ano + 1)}
        >
          <ChevronRight size={16} />
        </button>
      </div>

      <div ref={gradeRef} className="calendario-mes-grade" role="group" aria-label={`Meses de ${ano}`} onKeyDown={aoPressionarTecla}>
        {MESES_CURTOS.map((rotulo, indice) => {
          const mes = montarMes(ano, indice);
          const indisponivel = foraDoLimite(mes);
          return (
            <button
              key={mes}
              type="button"
              className="calendario-mes-item"
              data-indice={indice}
              data-atual={mes === hoje || undefined}
              aria-pressed={mes === valor}
              // aria-disabled em vez de disabled: o mês continua focável pelas setas
              aria-disabled={indisponivel || undefined}
              aria-label={`${MESES_EXTENSO[indice]} de ${ano}`}
              tabIndex={indice === foco ? 0 : -1}
              onClick={() => {
                if (!indisponivel) onSelecionar(mes);
              }}
              onFocus={() => setFoco(indice)}
            >
              {rotulo}
            </button>
          );
        })}
      </div>

      {atalhoMesAtual && (
        <div className="calendario-mes-rodape">
          <button type="button" disabled={atualForaDoLimite} onClick={() => onSelecionar(hoje)}>
            Este mês
          </button>
        </div>
      )}
    </div>
  );
}

export default CalendarioMes;
