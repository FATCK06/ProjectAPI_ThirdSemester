import { useCallback, useEffect, useId, useRef, useState, type KeyboardEvent } from "react";
import { Check, ChevronDown } from "lucide-react";
import { useFecharAoClicarFora } from "../../hooks/useFecharAoClicarFora";
import { usePosicaoPopover } from "../../hooks/usePosicaoPopover";
import "./select.css";

export interface OpcaoSelect<T extends string | number> {
  valor: T;
  rotulo: string;
  /** Texto auxiliar só na lista (ex.: "sem dados"); não entra na largura do botão. */
  detalhe?: string;
  desabilitada?: boolean;
}

interface PropsSelect<T extends string | number> {
  /** Liga o campo a um <label htmlFor>. */
  id?: string;
  /** null (ou valor fora das opções) mostra o placeholder. */
  valor: T | null;
  opcoes: OpcaoSelect<T>[];
  onChange: (valor: T) => void;
  placeholder?: string;
  disabled?: boolean;
  title?: string;
  "aria-label"?: string;
  className?: string;
}

/**
 * Substitui o <select> nativo: o dropdown do navegador não aceita estilo. Segue
 * o padrão "select-only combobox" da WAI-ARIA: o foco fica no botão e a opção
 * ativa é anunciada por aria-activedescendant.
 *
 * Teclado: setas, Home/End, Enter/Espaço escolhem, Esc fecha, e digitar uma
 * letra pula para a próxima opção que começa com ela.
 */
export function Select<T extends string | number>({
  id,
  valor,
  opcoes,
  onChange,
  placeholder = "Selecione",
  disabled = false,
  title,
  "aria-label": ariaLabel,
  className,
}: PropsSelect<T>) {
  const idGerado = useId();
  const idBase = id ?? idGerado;
  const idLista = `${idBase}-lista`;

  const [aberto, setAberto] = useState(false);
  const [ativo, setAtivo] = useState(-1);
  const containerRef = useRef<HTMLDivElement>(null);
  const gatilhoRef = useRef<HTMLButtonElement>(null);
  const listaRef = useRef<HTMLUListElement>(null);
  const posicao = usePosicaoPopover(aberto, gatilhoRef, listaRef);

  const indiceSelecionado = opcoes.findIndex((o) => o.valor === valor);
  const selecionada = indiceSelecionado >= 0 ? opcoes[indiceSelecionado] : undefined;

  const fechar = useCallback(() => setAberto(false), []);
  useFecharAoClicarFora(containerRef, aberto, fechar);

  // Mantém a opção ativa visível quando a lista rola
  useEffect(() => {
    if (!aberto || ativo < 0) return;
    const item = listaRef.current?.children[ativo] as HTMLElement | undefined;
    item?.scrollIntoView({ block: "nearest" });
  }, [aberto, ativo]);

  // Desabilitado com a lista aberta (ex.: recarregando opções): fecha. Ajuste
  // durante o render, como a doc do React recomenda, em vez de um efeito
  const [disabledAnterior, setDisabledAnterior] = useState(disabled);
  if (disabled !== disabledAnterior) {
    setDisabledAnterior(disabled);
    if (disabled) setAberto(false);
  }

  function habilitada(indice: number): boolean {
    return indice >= 0 && indice < opcoes.length && !opcoes[indice].desabilitada;
  }

  /** Próxima opção habilitada a partir de `inicio`, andando `passo` (1 ou -1). */
  function proximaHabilitada(inicio: number, passo: 1 | -1): number {
    for (let i = inicio; i >= 0 && i < opcoes.length; i += passo) {
      if (habilitada(i)) return i;
    }
    return -1;
  }

  function abrir() {
    if (disabled) return;
    setAtivo(habilitada(indiceSelecionado) ? indiceSelecionado : proximaHabilitada(0, 1));
    setAberto(true);
  }

  function escolher(indice: number) {
    if (!habilitada(indice)) return;
    setAberto(false);
    gatilhoRef.current?.focus();
    if (opcoes[indice].valor !== valor) onChange(opcoes[indice].valor);
  }

  function moverAtivo(passo: 1 | -1) {
    const proximo = proximaHabilitada(ativo + passo, passo);
    if (proximo >= 0) setAtivo(proximo);
  }

  function buscarPorLetra(letra: string) {
    const alvo = letra.toLocaleLowerCase("pt-BR");
    const total = opcoes.length;
    const inicio = aberto ? ativo : indiceSelecionado;
    for (let passo = 1; passo <= total; passo++) {
      const i = (inicio + passo + total) % total;
      if (habilitada(i) && opcoes[i].rotulo.toLocaleLowerCase("pt-BR").startsWith(alvo)) {
        if (aberto) setAtivo(i);
        else if (opcoes[i].valor !== valor) onChange(opcoes[i].valor);
        return;
      }
    }
  }

  function aoPressionarTecla(evento: KeyboardEvent<HTMLButtonElement>) {
    if (disabled) return;
    const { key } = evento;

    if (!aberto) {
      if (key === "ArrowDown" || key === "ArrowUp" || key === "Enter" || key === " ") {
        evento.preventDefault();
        abrir();
      } else if (key.length === 1 && /\S/.test(key)) {
        buscarPorLetra(key);
      }
      return;
    }

    switch (key) {
      case "ArrowDown":
        evento.preventDefault();
        moverAtivo(1);
        break;
      case "ArrowUp":
        evento.preventDefault();
        moverAtivo(-1);
        break;
      case "Home":
        evento.preventDefault();
        setAtivo(proximaHabilitada(0, 1));
        break;
      case "End":
        evento.preventDefault();
        setAtivo(proximaHabilitada(opcoes.length - 1, -1));
        break;
      case "Enter":
      case " ":
        evento.preventDefault();
        escolher(ativo);
        break;
      case "Tab":
        setAberto(false);
        break;
      default:
        if (key.length === 1 && /\S/.test(key)) buscarPorLetra(key);
    }
  }

  return (
    <div
      ref={containerRef}
      className={["select", className].filter(Boolean).join(" ")}
      data-aberto={aberto || undefined}
    >
      <button
        ref={gatilhoRef}
        id={idBase}
        type="button"
        className="select-gatilho"
        role="combobox"
        aria-haspopup="listbox"
        aria-expanded={aberto}
        aria-controls={idLista}
        aria-activedescendant={aberto && ativo >= 0 ? `${idBase}-opcao-${ativo}` : undefined}
        aria-label={ariaLabel}
        title={title}
        disabled={disabled}
        onClick={() => (aberto ? setAberto(false) : abrir())}
        onKeyDown={aoPressionarTecla}
      >
        {/* Todas as opções ocupam a mesma célula, só a escolhida aparece: o
            botão fica com a largura da maior e não pula ao trocar de opção */}
        <span className="select-valor">
          {opcoes.map((o, i) => (
            <span key={String(o.valor)} aria-hidden={i !== indiceSelecionado || undefined} data-visivel={i === indiceSelecionado || undefined}>
              {o.rotulo}
            </span>
          ))}
          <span
            className="select-placeholder"
            aria-hidden={selecionada !== undefined || undefined}
            data-visivel={selecionada === undefined || undefined}
          >
            {placeholder}
          </span>
        </span>
        <ChevronDown className="select-seta" size={16} aria-hidden="true" />
      </button>

      {aberto && (
        <ul
          ref={listaRef}
          id={idLista}
          className="select-lista"
          role="listbox"
          aria-labelledby={idBase}
          data-acima={posicao.acima || undefined}
          data-direita={posicao.direita || undefined}
          // Mantém o foco no botão ao clicar numa opção
          onMouseDown={(evento) => evento.preventDefault()}
        >
          {opcoes.map((o, i) => (
            <li
              key={String(o.valor)}
              id={`${idBase}-opcao-${i}`}
              role="option"
              className="select-opcao"
              aria-selected={i === indiceSelecionado}
              aria-disabled={o.desabilitada || undefined}
              data-ativo={i === ativo || undefined}
              onMouseEnter={() => habilitada(i) && setAtivo(i)}
              onClick={() => escolher(i)}
            >
              <span>
                {o.rotulo}
                {o.detalhe && <small className="select-opcao-detalhe">{o.detalhe}</small>}
              </span>
              {i === indiceSelecionado && <Check size={14} aria-hidden="true" />}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default Select;
