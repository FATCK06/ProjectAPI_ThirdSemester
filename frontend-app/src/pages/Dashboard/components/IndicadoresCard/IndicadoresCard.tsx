import { useEffect, useRef, useState } from "react";
import {
  Activity,
  ArrowDown,
  ArrowUp,
  BadgePercent,
  CalendarDays,
  CircleDollarSign,
  CalendarCheck,
  CreditCard,
  Info,
  ListFilter,
  Percent,
  TrendingUp,
  Truck,
  type LucideIcon,
} from "lucide-react";
import { Card } from "../../../../components/Card/Card";
<<<<<<< HEAD
import { AVISO_SEM_VIAGENS_MEDIA } from "../../../../components/Aviso/Aviso";
import {
  type HistoricoMes,
  type IndicadoresMes,
  type Totais,
} from "../../../../services/Dashboard";
import { podeVerDadosFinanceiros } from "../../../../services/auth";
import { formatarIntervaloMeses, formatarMesCurto, formatarMesExtenso } from "../../../../utils/datas";
import { formatarMoeda, formatarPercentual, SEM_VALOR } from "../../../../utils/formatacao";
=======
import { type IndicadoresMes } from "../../../../services/Dashboard";
import { podeVerDadosFinanceiros } from "../../../../services/auth";
import { formatarMoeda, formatarPercentual } from "../../../../utils/formatacao";
>>>>>>> origin/develop
import { Sparkline } from "./Sparkline";
import {
  INDICADORES,
  OPCOES_COMPARACAO,
  indicadoresVisiveis,
  mesesComDadosParaComparar,
  temGrafico,
  type Categoria,
  type IndicadorId,
  type MesesComparacao,
} from "./selecao";
import "./IndicadoresCard.css";

type Tom = "verde" | "azul" | "roxo" | "ambar" | "rosa" | "ciano";

interface ComparativoIndicador {
  variacaoPercentual: number | null;
  rotuloAnterior: string;
  // Ausente quando o período é só o mês anterior: não há tendência
  serie?: number[];
  // "abr. de 2026: 792 · mai. de 2026: 596 · ...": tooltip da linha
  descricaoSerie?: string;
}

// Seleção e período ficam com o Dashboard: sobrevivem à troca de mês, quando
// este card dá lugar ao skeleton
interface PropsIndicadoresCard {
  indicadores: IndicadoresMes;
  // Do mais antigo ao mês exibido (o último); vem de /dashboard/indicadores/historico
  historico: HistoricoMes[];
  selecionados: IndicadorId[];
  onSelecionadosChange: (selecionados: IndicadorId[]) => void;
  // A escolha do usuário; o card usa no máximo os meses que têm dado (ver mesesComDadosParaComparar)
  mesesComparacao: MesesComparacao;
  onMesesComparacaoChange: (meses: MesesComparacao) => void;
}

interface DefinicaoIndicador {
  id: IndicadorId;
  titulo: string;
  tom: Tom;
  Icone: LucideIcon;
  maiorEhMelhor: boolean;
  // Tira o número dos totais; o mesmo serve para o mês exibido e para o histórico.
  // Ausente: indicador ainda sem dado (mostra "-" e não tem comparativo).
  // Devolve undefined quando a API omite o valor (média sem viagem apurável, margem sem frete).
  obterNumero?: (totais: Totais) => number | undefined;
  formatar: (valor: number) => string;
  // Tooltip quando o valor do mês vem ausente
  semValor?: string;
  // Texto do tooltip ao lado do título; função quando depende do mês
  ajuda?: string | ((totais: Totais) => string);
  // Marca com * o valor que é uma aproximação (o tooltip explica o porquê)
  aproximado?: boolean | ((totais: Totais) => boolean);
}

const LABEL_CATEGORIA: Record<Categoria, string> = {
  operacional: "Operacional",
  financeiro: "Financeiro",
};

const CATEGORIAS: Categoria[] = ["operacional", "financeiro"];

<<<<<<< HEAD
// Uma casa: a média de um período pode não ser inteira (ex.: 693,5 viagens)
function formatarNumero(valor: number): string {
  return valor.toLocaleString("pt-BR", { maximumFractionDigits: 1 });
=======
const SELECAO_INICIAL: IndicadorId[] = [
  "numeroViagens",
  "diasDisponiveis",
  "utilizacaoMedia",
];

function formatarMes(mesReferencia: string): string {
  const [ano, mes] = mesReferencia.split("-").map(Number);
  if (!ano || !mes) return mesReferencia;
  return new Date(ano, mes - 1, 1).toLocaleDateString("pt-BR", {
    month: "long",
    year: "numeric",
  });
>>>>>>> origin/develop
}

function formatarVariacao(valor: number): string {
  const numero = valor.toLocaleString("pt-BR", {
    minimumFractionDigits: 1,
    maximumFractionDigits: 1,
  });
  return `${valor > 0 ? "+" : ""}${numero}%`;
}

// Viagem sem Vale frete ou sem Valor Fretes no CSV fica fora das somas financeiras
function faltamViagens(t: Totais): boolean {
  return t.viagensApuradas < t.numeroViagens;
}

// Tooltip dos indicadores financeiros: o texto fixo e, quando alguma viagem
// ficou fora das somas, quantas foram
function comAvisoDeFalta(base: string) {
  return (t: Totais): string => {
    if (!faltamViagens(t)) return base;
    const faltando = t.numeroViagens - t.viagensApuradas;
    return (
      `${base} Neste mês, ${formatarNumero(faltando)} de ${formatarNumero(t.numeroViagens)} viagens ` +
      "vieram sem frete ou custo no CSV e ficaram de fora, então o valor pode não refletir o mês inteiro."
    );
  };
}

// Fora do componente: não muda entre renders. O valor só é calculado na hora de exibir.
const DEFINICOES: DefinicaoIndicador[] = [
  {
    id: "numeroViagens",
    titulo: "Nº de viagens",
    tom: "verde",
    Icone: Truck,
    maiorEhMelhor: true,
    obterNumero: (t) => t.numeroViagens,
    formatar: formatarNumero,
  },
  {
    id: "diasOperacao",
    titulo: "Dias em operação",
    tom: "ciano",
    Icone: CalendarCheck,
    maiorEhMelhor: true,
    obterNumero: (t) => t.diasOperacao,
    formatar: formatarNumero,
    aproximado: true,
    ajuda:
      "Soma, para cada motorista, das datas diferentes em que ele teve manifesto. " +
      "Uma viagem de mais de um dia conta como 1 dia, pois a coluna Diária do CSV vem vazia.",
  },
  {
    id: "diasDisponiveis",
    titulo: "Dias disponíveis",
    tom: "azul",
    Icone: CalendarDays,
    maiorEhMelhor: true,
    formatar: formatarNumero,
    ajuda:
      "Ainda não calculado: o CSV de manifestos não informa férias, afastamentos " +
      "ou manutenção. Aguardando definição da Newe.",
  },
  {
    id: "utilizacaoMedia",
    titulo: "% de utilização",
    tom: "roxo",
    Icone: Percent,
    maiorEhMelhor: true,
<<<<<<< HEAD
    formatar: formatarPercentual,
    ajuda:
      "Dias em operação ÷ dias disponíveis × 100. Ainda não calculado, pois depende " +
      "dos dias disponíveis.",
=======
    obterValor: (i) => formatarPercentual(i.totais.utilizacaoMedia),
>>>>>>> origin/develop
  },
  {
    id: "valorFrete",
    titulo: "Valor total dos fretes",
    tom: "ciano",
    Icone: CircleDollarSign,
    maiorEhMelhor: true,
<<<<<<< HEAD
    obterNumero: (t) => t.valorFrete,
    formatar: formatarMoeda,
    aproximado: faltamViagens,
    ajuda: comAvisoDeFalta(
      "Quanto os clientes pagaram à Newe pelas viagens (coluna Valor Fretes do CSV).",
    ),
=======
    obterValor: (i) => formatarMoeda(i.totais.valorFrete),
>>>>>>> origin/develop
  },
  {
    id: "custoTotal",
    titulo: "Custo total",
    tom: "rosa",
    Icone: CreditCard,
    maiorEhMelhor: false, // custo maior é ruim
<<<<<<< HEAD
    obterNumero: (t) => t.custoTotal,
    formatar: formatarMoeda,
    aproximado: faltamViagens,
    ajuda: comAvisoDeFalta(
      "Quanto a Newe pagou ao motorista pelas viagens (coluna Vale frete do CSV): " +
        "frete-base do motorista (Valor Frete) + pedágio, diárias e adicionais.",
    ),
=======
    obterValor: (i) => formatarMoeda(i.totais.custoTotal),
>>>>>>> origin/develop
  },
  {
    id: "rentabilidade",
    titulo: "Rentabilidade total",
    tom: "verde",
    Icone: TrendingUp,
    maiorEhMelhor: true,
<<<<<<< HEAD
    obterNumero: (t) => t.rentabilidade,
    formatar: formatarMoeda,
    aproximado: faltamViagens,
    ajuda: comAvisoDeFalta("Valor total dos fretes − custo total."),
=======
    obterValor: (i) => formatarMoeda(i.totais.rentabilidade),
>>>>>>> origin/develop
  },
  {
    id: "rentabilidadeMediaViagem",
    titulo: "Rentabilidade média/viagem",
    tom: "ambar",
    Icone: Activity,
    maiorEhMelhor: true,
<<<<<<< HEAD
    obterNumero: (t) => t.rentabilidadeMediaViagem,
    formatar: formatarMoeda,
    semValor: AVISO_SEM_VIAGENS_MEDIA,
    // Só quando alguma viagem ficou de fora: com todas apuradas o valor é exato
    aproximado: faltamViagens,
    ajuda: comAvisoDeFalta("Rentabilidade total ÷ viagens com frete e custo informados no CSV."),
=======
    obterValor: (i) => formatarMoeda(i.totais.rentabilidadeMediaViagem),
>>>>>>> origin/develop
  },
  {
    id: "margem",
    titulo: "Margem",
    tom: "azul",
    Icone: BadgePercent,
    maiorEhMelhor: true,
<<<<<<< HEAD
    obterNumero: (t) => t.margem,
    formatar: formatarPercentual,
    semValor: "Sem valor de frete no período. Não é possível calcular a margem.",
    aproximado: faltamViagens,
    ajuda: comAvisoDeFalta("Rentabilidade total ÷ valor total dos fretes × 100: quanto sobra de cada R$ 100 faturados."),
=======
    obterValor: (i) => formatarPercentual(i.totais.margem),
>>>>>>> origin/develop
  },
];

const DEFINICAO_POR_ID = new Map(DEFINICOES.map((d) => [d.id, d]));

function textoAjuda(d: DefinicaoIndicador, totais: Totais): string | undefined {
  return typeof d.ajuda === "function" ? d.ajuda(totais) : d.ajuda;
}

function ehAproximado(d: DefinicaoIndicador, totais: Totais): boolean {
  return typeof d.aproximado === "function" ? d.aproximado(totais) : Boolean(d.aproximado);
}

/** "mai. de 2026" ou "média de abr. a mai. de 2026": contra o que o card compara. */
function descreverPeriodo(anteriores: HistoricoMes[]): string {
  const primeiro = anteriores[0].mesReferencia;
  const ultimo = anteriores[anteriores.length - 1].mesReferencia;
  return anteriores.length === 1
    ? formatarMesCurto(ultimo)
    : `média de ${formatarIntervaloMeses(primeiro, ultimo)}`;
}

/**
 * Compara o mês exibido com a média dos `meses` anteriores (1 mês: o próprio
 * mês anterior). Quem chama já garantiu que todos esses meses têm viagens
 * (mesesComDadosParaComparar), então nenhum mês vazio entra como zero. Com 2 ou
 * mais meses, a linha de tendência cobre o período e o mês exibido.
 */
function montarComparativo(
  d: DefinicaoIndicador,
  historico: HistoricoMes[],
  meses: number,
): ComparativoIndicador | undefined {
  const obterNumero = d.obterNumero;
  if (!obterNumero || meses === 0) return undefined;

  const atual = historico[historico.length - 1];
  const anteriores = historico.slice(-(meses + 1), -1);
  const periodo = descreverPeriodo(anteriores);

  // Mês com o valor ausente (ex.: média sem viagem apurável) fica fora da conta
  const valoresAnteriores = anteriores
    .map((h) => obterNumero(h.totais))
    .filter((v): v is number => v !== undefined);
  const valorAtual = obterNumero(atual.totais);

  let serie: number[] | undefined;
  let descricaoSerie: string | undefined;
  if (temGrafico(meses)) {
    const pontos = [...anteriores, atual].map((h) => ({
      mes: formatarMesCurto(h.mesReferencia),
      valor: obterNumero(h.totais),
    }));
    serie = pontos.map((p) => p.valor).filter((v): v is number => v !== undefined);
    descricaoSerie = pontos
      .map((p) => `${p.mes}: ${p.valor === undefined ? "sem dados" : d.formatar(p.valor)}`)
      .join(" · ");
  }

  if (valoresAnteriores.length === 0) {
    return { variacaoPercentual: null, rotuloAnterior: `vs. ${periodo} (sem dados)`, serie, descricaoSerie };
  }

  const referencia = valoresAnteriores.reduce((soma, v) => soma + v, 0) / valoresAnteriores.length;
  return {
    // Base zero não tem variação percentual; abs mantém o sinal certo com base negativa
    variacaoPercentual:
      referencia === 0 || valorAtual === undefined
        ? null
        : ((valorAtual - referencia) / Math.abs(referencia)) * 100,
    rotuloAnterior: `vs. ${periodo} (${d.formatar(referencia)})`,
    serie,
    descricaoSerie,
  };
}

interface PropsVariacao {
  comparativo: ComparativoIndicador;
  maiorEhMelhor: boolean;
}

function VariacaoIndicador({ comparativo, maiorEhMelhor }: PropsVariacao) {
  const { variacaoPercentual, rotuloAnterior } = comparativo;

  if (variacaoPercentual === null) {
    return <div className="indicador-anterior">{rotuloAnterior}</div>;
  }

  const sentido =
    variacaoPercentual === 0
      ? "neutro"
      : variacaoPercentual > 0 === maiorEhMelhor
        ? "bom"
        : "ruim";

  return (
    <>
      <div className="indicador-variacao" data-sentido={sentido}>
        {variacaoPercentual > 0 && <ArrowUp size={14} />}
        {variacaoPercentual < 0 && <ArrowDown size={14} />}
        {formatarVariacao(variacaoPercentual)}
      </div>
      <div className="indicador-anterior">{rotuloAnterior}</div>
    </>
  );
}

interface PropsValorIndicador {
  d: DefinicaoIndicador;
  totais: Totais;
}

function ValorIndicador({ d, totais }: PropsValorIndicador) {
  const valor = d.obterNumero?.(totais);

  if (valor === undefined) {
    // Sem obterNumero: indicador ainda sem dado; com ele: a API omitiu o valor do mês
    const motivo = d.obterNumero ? d.semValor : undefined;
    return (
      <div className="indicador-valor">
        <span title={motivo} aria-label={motivo}>{SEM_VALOR}</span>
      </div>
    );
  }

  return (
    <div className="indicador-valor">
      {d.formatar(valor)}
      {ehAproximado(d, totais) && (
        <span className="indicador-asterisco" title={textoAjuda(d, totais)}>
          *
        </span>
      )}
    </div>
  );
}

export function IndicadoresCard({
  indicadores,
  historico,
  selecionados,
  onSelecionadosChange,
  mesesComparacao,
  onMesesComparacaoChange,
}: PropsIndicadoresCard) {
  // Quem não é gestor nem vê a categoria financeira no filtro: a API não manda os valores.
  const categorias = podeVerDadosFinanceiros()
    ? CATEGORIAS
    : CATEGORIAS.filter((c) => c !== "financeiro");
  const definicoes = DEFINICOES.filter((d) => categorias.includes(d.categoria));
  const [mostrarFiltro, setMostrarFiltro] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  // Sem VER_DADOS_FINANCEIROS o backend não manda esses valores: a categoria
  // some inteira, inclusive do filtro.
  const financeiro = podeVerDadosFinanceiros();
  const categorias = financeiro
    ? CATEGORIAS
    : CATEGORIAS.filter((c) => c !== "financeiro");
  const disponiveis = INDICADORES.filter((i) => categorias.includes(i.categoria));

  useEffect(() => {
    if (!mostrarFiltro) return;

    function aoClicarFora(evento: MouseEvent) {
      if (
        containerRef.current &&
        !containerRef.current.contains(evento.target as Node)
      ) {
        setMostrarFiltro(false);
      }
    }

    function aoPressionarTecla(evento: KeyboardEvent) {
      if (evento.key === "Escape") setMostrarFiltro(false);
    }

    document.addEventListener("mousedown", aoClicarFora);
    document.addEventListener("keydown", aoPressionarTecla);

    return () => {
      document.removeEventListener("mousedown", aoClicarFora);
      document.removeEventListener("keydown", aoPressionarTecla);
    };
  }, [mostrarFiltro]);

  function alternarIndicador(id: IndicadorId) {
    onSelecionadosChange(
      selecionados.includes(id)
        ? selecionados.filter((item) => item !== id)
        : [...selecionados, id],
    );
  }

<<<<<<< HEAD
  const visiveis = indicadoresVisiveis(selecionados, financeiro).map(
    (i) => DEFINICAO_POR_ID.get(i.id)!,
=======
  const indicadoresVisiveis = definicoes.filter((d) =>
    selecionados.includes(d.id),
>>>>>>> origin/develop
  );

  // A escolha do usuário fica guardada; aqui só se usa o que o mês permite.
  // Voltar para um mês com mais histórico devolve o período escolhido.
  const mesesDisponiveis = mesesComDadosParaComparar(historico);
  const mesesEmUso = Math.min(mesesComparacao, mesesDisponiveis);
  const semComparacao = mesesDisponiveis === 0;
  const avisoSemComparacao =
    historico[historico.length - 1]?.totais.motoristas === 0
      ? "Sem viagens importadas neste mês: não há o que comparar."
      : "Sem viagens importadas nos meses anteriores para comparar.";
  const descricaoComparacao = semComparacao
    ? avisoSemComparacao
    : `Comparando com ${descreverPeriodo(historico.slice(-(mesesEmUso + 1), -1))}.`;

  const acoes = (
    <div className="indicadores-acoes">
      <label
        className="indicadores-comparacao"
        title={semComparacao ? avisoSemComparacao : undefined}
      >
        <span>Comparar com</span>
        <select
          value={semComparacao ? "" : mesesEmUso}
          disabled={semComparacao}
          onChange={(e) => onMesesComparacaoChange(Number(e.target.value) as MesesComparacao)}
        >
          {semComparacao && <option value="">Sem dados</option>}
          {OPCOES_COMPARACAO.map((o) => (
            // Só vale a opção cujos meses todos têm viagens importadas
            <option key={o.valor} value={o.valor} disabled={o.valor > mesesDisponiveis}>
              {o.valor > mesesDisponiveis ? `${o.rotulo} (sem dados)` : o.rotulo}
            </option>
          ))}
        </select>
      </label>

<<<<<<< HEAD
      <div ref={containerRef} className="indicadores-filtro">
        <button
          type="button"
          className="indicadores-filtro-btn"
          onClick={() => setMostrarFiltro((atual) => !atual)}
          aria-expanded={mostrarFiltro}
        >
          <ListFilter size={14} color="#64748b" />
          Filtros
          <span className="indicadores-filtro-badge">{visiveis.length}</span>
        </button>

        {mostrarFiltro && (
          <div className="indicadores-menu">
            <div className="indicadores-menu-lista">
              {categorias.map((categoria) => (
                <div key={categoria}>
                  <div className="indicadores-menu-categoria">
                    {LABEL_CATEGORIA[categoria]}
                  </div>

                  {disponiveis.filter((i) => i.categoria === categoria).map(
                    (i) => (
                      <label key={i.id} className="indicadores-menu-item">
                        <input
                          type="checkbox"
                          checked={selecionados.includes(i.id)}
                          onChange={() => alternarIndicador(i.id)}
                        />
                        {DEFINICAO_POR_ID.get(i.id)!.titulo}
                      </label>
                    ),
                  )}
=======
      {mostrarFiltro && (
        <div className="indicadores-menu">
          <div className="indicadores-menu-lista">
            {categorias.map((categoria) => (
              <div key={categoria}>
                <div className="indicadores-menu-categoria">
                  {LABEL_CATEGORIA[categoria]}
>>>>>>> origin/develop
                </div>
              ))}
            </div>

<<<<<<< HEAD
            <div className="indicadores-menu-rodape">
              <button type="button" onClick={() => onSelecionadosChange([])}>
                Limpar
              </button>
              <button
                type="button"
                onClick={() => onSelecionadosChange(disponiveis.map((i) => i.id))}
              >
                Selecionar todos
              </button>
            </div>
          </div>
        )}
      </div>
=======
                {definicoes.filter((d) => d.categoria === categoria).map(
                  (d) => (
                    <label key={d.id} className="indicadores-menu-item">
                      <input
                        type="checkbox"
                        checked={selecionados.includes(d.id)}
                        onChange={() => alternarIndicador(d.id)}
                      />
                      {d.titulo}
                    </label>
                  ),
                )}
              </div>
            ))}
          </div>

          <div className="indicadores-menu-rodape">
            <button type="button" onClick={() => setSelecionados([])}>
              Limpar
            </button>
            <button
              type="button"
              onClick={() => setSelecionados(definicoes.map((d) => d.id))}
            >
              Selecionar todos
            </button>
          </div>
        </div>
      )}
>>>>>>> origin/develop
    </div>
  );

  return (
    <Card
      sobretitulo="Visão geral"
      titulo={`Indicadores de ${formatarMesExtenso(indicadores.mesReferencia)}`}
      subtitulo={descricaoComparacao}
      acoes={acoes}
    >
      {visiveis.length === 0 ? (
        <p className="indicadores-vazio">
          Nenhum indicador selecionado. Use os filtros para escolher o que
          exibir.
        </p>
      ) : (
        // A grade mede a largura do card (container query) para centralizar a última linha
        <div className="indicadores-area">
          <div className="indicadores-grid">
            {visiveis.map((d) => {
              const comp = montarComparativo(d, historico, mesesEmUso);
              const ajuda = textoAjuda(d, indicadores.totais);

              return (
                <div key={d.id} className="indicador-item" data-tom={d.tom}>
                  <span className="indicador-icone">
                    <d.Icone size={22} />
                  </span>

                  <div className="indicador-corpo">
                    <div className="indicador-titulo">
                      {d.titulo}
                      {ajuda && (
                        <span
                          className="indicador-ajuda"
                          title={ajuda}
                          aria-label={ajuda}
                          tabIndex={0}
                        >
                          <Info size={13} />
                        </span>
                      )}
                    </div>
                    <ValorIndicador d={d} totais={indicadores.totais} />
                    {comp && (
                      <VariacaoIndicador
                        comparativo={comp}
                        maiorEhMelhor={d.maiorEhMelhor}
                      />
                    )}
                  </div>

                  {comp?.serie && (
                    <Sparkline valores={comp.serie} titulo={comp.descricaoSerie} />
                  )}
                </div>
              );
            })}
          </div>
        </div>
      )}
    </Card>
  );
}

export default IndicadoresCard;
