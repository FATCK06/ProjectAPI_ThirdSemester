import { useEffect, useState } from "react";
import { Link, useLocation, useParams, useSearchParams } from "react-router-dom";
import { PageHeader } from "../../../components/PageHeader";
import { buscarMesesImportados, buscarIndicadores, type IndicadoresMotorista } from "../../../services/Dashboard";
import { buscarFichaMotorista, buscarMotoristaNoMes, type MotoristaFicha, type MotoristaMes } from "../../../services/Motoristas";
import { podeVerDadosFinanceiros } from "../../../services/auth";
import { formatarMoeda, formatarPercentual, SEM_VALOR } from "../../../utils/formatacao";
import "./historicoMotorista.css";
import "../FichaMotorista.css";

type DadosHistorico = { motorista: MotoristaFicha; veiculo: MotoristaMes | null; indicadores: IndicadoresMotorista | null };
type Resultado = { chave: string; dados: DadosHistorico } | { chave: string; erro: true };

function rotuloMes(mes: string) {
  const [ano, numero] = mes.split("-").map(Number);
  return new Intl.DateTimeFormat("pt-BR", { month: "long", year: "numeric" }).format(new Date(ano, numero - 1, 1));
}

export function HistoricoMotorista() {
  const { motoristaId } = useParams();
  const location = useLocation();
  const [searchParams, setSearchParams] = useSearchParams();
  const [meses, setMeses] = useState<string[]>([]);
  const [mes, setMes] = useState(searchParams.get("mes") ?? "");
  const [mesesErro, setMesesErro] = useState(false);
  const [resultado, setResultado] = useState<Resultado | null>(null);
  const chave = `${motoristaId ?? ""}|${mes}`;
  const dados = resultado && "dados" in resultado && resultado.chave === chave ? resultado.dados : null;
  const carregando = Boolean(motoristaId && mes && resultado?.chave !== chave);
  const erro = resultado?.chave === chave && "erro" in resultado;
  const financeiro = podeVerDadosFinanceiros();
  const origem = searchParams.get("origem");
  const [retorno] = useState(() => (location.state as { retorno?: { destino?: string; restauracao?: unknown } } | null)?.retorno);
  const destinoVolta = retorno?.destino ?? (origem === "ranking" ? "/ranking" : origem === "dashboard" ? "/dashboard" : origem === "motoristas" ? "/motoristas" : "/operacao-mensal");
  const estadoVolta = retorno?.restauracao ? { restauracao: retorno.restauracao } : undefined;
  const destinoFicha = `/operacao-mensal/motoristas/${motoristaId}${mes ? `?mes=${encodeURIComponent(mes)}` : ""}`;

  useEffect(() => {
    let cancelado = false;
    buscarMesesImportados()
      .then((lista) => {
        if (cancelado) return;
        setMeses(lista);
        setMesesErro(false);
        const informado = searchParams.get("mes");
        const inicial = informado && lista.includes(informado) ? informado : lista[0] ?? "";
        setMes(inicial);
        if (inicial !== informado) {
          setSearchParams((atual) => {
            const parametros = new URLSearchParams(atual);
            if (inicial) parametros.set("mes", inicial);
            else parametros.delete("mes");
            return parametros;
          }, { replace: true });
        }
      })
      .catch(() => { if (!cancelado) setMesesErro(true); });
    return () => { cancelado = true; };
  }, [motoristaId]);

  useEffect(() => {
    let cancelado = false;
    if (!motoristaId || !mes) return;
    Promise.all([
      buscarFichaMotorista(motoristaId),
      buscarMotoristaNoMes(motoristaId, mes),
      buscarIndicadores(mes),
    ]).then(([motorista, veiculo, indicadores]) => {
      if (cancelado) return;
      setResultado({
        chave,
        dados: {
          motorista,
          veiculo,
          indicadores: indicadores.motoristas.find((item) => item.motoristaId === motoristaId) ?? null,
        },
      });
    }).catch(() => { if (!cancelado) setResultado({ chave, erro: true }); });
    return () => { cancelado = true; };
  }, [motoristaId, mes, chave]);

  function mudarMes(proximo: string) {
    setMes(proximo);
    setSearchParams((atual) => {
      const parametros = new URLSearchParams(atual);
      parametros.set("mes", proximo);
      return parametros;
    });
  }

  return (
    <section className="historico-motorista-page">
      <Link className="historico-motorista-voltar" to={destinoVolta} state={estadoVolta}>← Voltar</Link>
      <PageHeader titulo={dados?.motorista.nome ?? "Histórico do motorista"} subtitulo="Desempenho no período selecionado">
        <label className="historico-motorista-periodo" htmlFor="historico-motorista-mes">
          Período
          <select id="historico-motorista-mes" value={mes} disabled={!meses.length} onChange={(evento) => mudarMes(evento.target.value)}>
            {meses.map((item) => <option key={item} value={item}>{rotuloMes(item)}</option>)}
          </select>
        </label>
      </PageHeader>

      {mesesErro && <div className="historico-motorista-feedback" role="alert">Não foi possível carregar os meses importados.</div>}
      {!mesesErro && meses.length === 0 && <div className="historico-motorista-feedback">Nenhum mês importado encontrado.</div>}
      {carregando && <div className="historico-motorista-feedback" role="status">Carregando histórico…</div>}
      {erro && <div className="historico-motorista-feedback" role="alert">Não foi possível carregar os dados deste motorista.</div>}

      {dados && (
        <>
          <dl className="historico-motorista-identificacao">
            <div><dt>Placa</dt><dd>{dados.veiculo?.placa ?? "Sem veículo no período"}</dd></div>
            <div><dt>Tipo de veículo</dt><dd>{dados.veiculo?.tipoVeiculo ?? "Sem veículo no período"}</dd></div>
          </dl>
          <Link className="historico-motorista-voltar" to={destinoFicha}>Ver ficha cadastral</Link>
          {dados.indicadores ? (
            <div className="historico-motorista-indicadores">
              <article><span>Viagens</span><strong>{dados.indicadores.numeroViagens}</strong></article>
              <article><span>Dias em operação</span><strong>{dados.indicadores.diasOperacao}</strong></article>
              <article><span>Utilização</span><strong>{formatarPercentual(dados.indicadores.utilizacao)}</strong></article>
              <article><span>Disponibilidade</span><strong>{formatarPercentual(dados.indicadores.disponibilidade)}</strong></article>
              {financeiro && <>
                <article><span>Fretes</span><strong>{formatarMoeda(dados.indicadores.valorFrete)}</strong></article>
                <article><span>Custos</span><strong>{formatarMoeda(dados.indicadores.custoTotal)}</strong></article>
                <article><span>Rentabilidade</span><strong>{formatarMoeda(dados.indicadores.rentabilidade)}</strong></article>
                <article><span>Rentabilidade média</span><strong>{dados.indicadores.rentabilidadeMediaViagem === undefined ? SEM_VALOR : formatarMoeda(dados.indicadores.rentabilidadeMediaViagem)}</strong></article>
              </>}
            </div>
          ) : <div className="historico-motorista-feedback">Sem viagens deste motorista no mês selecionado.</div>}
        </>
      )}
    </section>
  );
}
