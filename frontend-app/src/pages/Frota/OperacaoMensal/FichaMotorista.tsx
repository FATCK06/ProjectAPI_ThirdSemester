import { useEffect, useState } from "react";
import { Link, useLocation, useParams, useSearchParams } from "react-router-dom";
import {
  buscarHistoricoMotorista,
  buscarFichaMotorista,
  buscarMotoristaNoMes,
  type IndicadorMotoristaMes,
  type MotoristaFicha as MotoristaFichaDados,
  type MotoristaMes,
} from "../../../services/Motoristas";
import { buscarMesesImportados } from "../../../services/Dashboard";
import { podeVerDadosFinanceiros } from "../../../services/auth";
import { ComparativoMotorista } from "./ComparativoMotorista";
import "./operacaoMensal.css";
import "../FichaMotorista.css";

type Status = "loading" | "success" | "error";
type ResultadoFicha =
  | { id: string; status: "success"; motorista: MotoristaFichaDados }
  | { id: string; status: "error" };
type ResultadoMes =
  | { chave: string; status: "success"; veiculo: MotoristaMes | null }
  | { chave: string; status: "error" };
type ResultadoComparacao =
  | { chave: string; status: "success"; historico: IndicadorMotoristaMes[]; veiculoAnterior: MotoristaMes | null; veiculoIntermediario: MotoristaMes | null; veiculoAtual: MotoristaMes | null }
  | { chave: string; status: "error" };

function mesesDaComparacao(mes: string) {
  const [ano, numero] = mes.split("-").map(Number);
  return [2, 1, 0].map((deslocamento) => {
    const data = new Date(ano, numero - 1 - deslocamento, 1);
    return `${data.getFullYear()}-${String(data.getMonth() + 1).padStart(2, "0")}`;
  });
}

function rotuloMes(mes: string) {
  const [ano, numero] = mes.split("-").map(Number);
  return new Intl.DateTimeFormat("pt-BR", { month: "long", year: "numeric" })
    .format(new Date(ano, numero - 1, 1));
}

function formatarData(data: string | null) {
  if (!data) return "Não informado";
  const partes = data.split("-");
  if (partes.length !== 3) return data;
  return `${partes[2]}/${partes[1]}/${partes[0]}`;
}

function valorOuAusente(valor: string | number | null) {
  return valor === null || valor === "" ? "Não informado" : String(valor);
}

export function FichaMotorista() {
  const { motoristaId } = useParams();
  const location = useLocation();
  const [searchParams, setSearchParams] = useSearchParams();
  const [resultado, setResultado] = useState<ResultadoFicha | null>(null);
  const [meses, setMeses] = useState<string[]>([]);
  const [erroMeses, setErroMeses] = useState(false);
  const [resultadoMes, setResultadoMes] = useState<ResultadoMes | null>(null);
  const [resultadoComparacao, setResultadoComparacao] = useState<ResultadoComparacao | null>(null);
  const status: Status = !motoristaId
    ? "error"
    : resultado?.id === motoristaId
      ? resultado.status
      : "loading";
  const motorista = resultado && resultado.id === motoristaId && resultado.status === "success"
    ? resultado.motorista
    : null;
  const mes = searchParams.get("mes");
  const chaveMes = `${motoristaId ?? ""}|${mes ?? ""}`;
  const veiculo = resultadoMes?.chave === chaveMes && resultadoMes.status === "success"
    ? resultadoMes.veiculo
    : null;
  const carregandoMes = Boolean(motoristaId && mes && resultadoMes?.chave !== chaveMes);
  const erroMes = resultadoMes?.chave === chaveMes && resultadoMes.status === "error";
  const mesesComparacao = mes ? mesesDaComparacao(mes) : [];
  const chaveComparacao = `${motoristaId ?? ""}|${mesesComparacao.join("|")}`;
  const comparacao = resultadoComparacao?.chave === chaveComparacao && resultadoComparacao.status === "success"
    ? resultadoComparacao
    : null;
  const erroComparacao = resultadoComparacao?.chave === chaveComparacao && resultadoComparacao.status === "error";
  const pagina = searchParams.get("page");
  const queryVolta = new URLSearchParams();
  if (mes) queryVolta.set("mes", mes);
  if (pagina) queryVolta.set("page", pagina);
  const destinoVolta = `/motoristas${queryVolta.size ? `?${queryVolta}` : ""}`;
  const retorno = (location.state as { retorno?: { destino?: string; restauracao?: unknown } } | null)?.retorno;

  useEffect(() => {
    let cancelado = false;
    buscarMesesImportados()
      .then((lista) => {
        if (cancelado) return;
        setMeses(lista);
        setErroMeses(false);
        const mesInicial = mes && lista.includes(mes) ? mes : lista[0] ?? "";
        if (mesInicial !== mes) {
          setSearchParams((atual) => {
            const parametros = new URLSearchParams(atual);
            if (mesInicial) parametros.set("mes", mesInicial);
            else parametros.delete("mes");
            return parametros;
          }, { replace: true });
        }
      })
      .catch(() => { if (!cancelado) setErroMeses(true); });
    return () => { cancelado = true; };
  }, [motoristaId]);

  useEffect(() => {
    let cancelado = false;
    if (!motoristaId) return;

    buscarFichaMotorista(motoristaId)
      .then((dados) => {
        if (cancelado) return;
        setResultado({ id: motoristaId, status: "success", motorista: dados });
      })
      .catch(() => {
        if (cancelado) return;
        setResultado({ id: motoristaId, status: "error" });
      });

    return () => {
      cancelado = true;
    };
  }, [motoristaId]);

  useEffect(() => {
    let cancelado = false;
    if (!motoristaId || !mes || !meses.includes(mes)) return;
    buscarMotoristaNoMes(motoristaId, mes)
      .then((dados) => {
        if (!cancelado) setResultadoMes({ chave: chaveMes, status: "success", veiculo: dados });
      })
      .catch(() => {
        if (!cancelado) setResultadoMes({ chave: chaveMes, status: "error" });
      });
    return () => { cancelado = true; };
  }, [motoristaId, mes, chaveMes, meses]);

  useEffect(() => {
    let cancelado = false;
    if (!motoristaId || !mes || !meses.includes(mes)) return;
    const mesesHistorico = mesesDaComparacao(mes);
    const [mesInicial, mesIntermediario] = mesesHistorico;
    Promise.all([
      buscarHistoricoMotorista(motoristaId, mesInicial, mes),
      buscarMotoristaNoMes(motoristaId, mesInicial),
      buscarMotoristaNoMes(motoristaId, mesIntermediario),
      buscarMotoristaNoMes(motoristaId, mes),
    ]).then(([historico, veiculoAnterior, veiculoIntermediario, veiculoAtual]) => {
      if (!cancelado) setResultadoComparacao({
        chave: `${motoristaId}|${mesesHistorico.join("|")}`,
        status: "success",
        historico,
        veiculoAnterior,
        veiculoIntermediario,
        veiculoAtual,
      });
    }).catch(() => {
      if (!cancelado) setResultadoComparacao({ chave: `${motoristaId}|${mesesHistorico.join("|")}`, status: "error" });
    });
    return () => { cancelado = true; };
  }, [motoristaId, mes, meses]);

  function mudarMes(proximo: string) {
    setSearchParams((atual) => {
      const parametros = new URLSearchParams(atual);
      parametros.set("mes", proximo);
      return parametros;
    });
  }

  return (
    <section className="ficha-motorista-painel">
      <Link className="ficha-motorista-voltar" to={retorno?.destino ?? destinoVolta}
        state={retorno?.restauracao ? { restauracao: retorno.restauracao } : undefined}>
        <span aria-hidden="true">←</span> Voltar para motoristas
      </Link>

      {status === "loading" && (
        <div className="operacao-mensal-feedback" role="status">
          Carregando ficha do motorista…
        </div>
      )}

      {status === "error" && (
        <div className="operacao-mensal-feedback" role="alert">
          <p>Não foi possível carregar a ficha deste motorista.</p>
          <Link className="ficha-motorista-voltar" to={destinoVolta}>
            Voltar para a lista
          </Link>
        </div>
      )}

      {status === "success" && motorista && (
        <>
          <header className="ficha-motorista-cabecalho">
            <div>
              <h1 className="ficha-motorista-titulo">{motorista.nome}</h1>
              <p className="ficha-motorista-subtitulo">Ficha cadastral do motorista</p>
            </div>
            <div className="operacao-mensal-filtro">
              <label htmlFor="ficha-motorista-mes">Período</label>
              <select
                id="ficha-motorista-mes"
                value={mes ?? ""}
                disabled={!meses.length || erroMeses}
                onChange={(evento) => mudarMes(evento.target.value)}
              >
                {meses.length === 0 && <option value="">{erroMeses ? "Meses indisponíveis" : "Carregando meses..."}</option>}
                {meses.map((item) => <option key={item} value={item}>{rotuloMes(item)}</option>)}
              </select>
            </div>
          </header>
          {erroMeses && <div className="operacao-mensal-feedback" role="alert">Não foi possível carregar os meses importados.</div>}
          {erroMes && <div className="operacao-mensal-feedback" role="alert">Não foi possível carregar os dados do período.</div>}
          {!erroMeses && !mes && <div className="operacao-mensal-feedback">Nenhum mês importado encontrado.</div>}

          <section className="ficha-motorista-secao" aria-labelledby="ficha-dados-veiculo">
            <h2 id="ficha-dados-veiculo">Dados do veículo no período</h2>
            <dl className="ficha-motorista-grade">
              <div><dt>Placa</dt><dd>{carregandoMes ? "Carregando..." : erroMes ? "Indisponível" : veiculo?.placa ?? "Sem veículo no período"}</dd></div>
              <div><dt>Tipo de veículo</dt><dd>{carregandoMes ? "Carregando..." : erroMes ? "Indisponível" : veiculo?.tipoVeiculo ?? "Sem veículo no período"}</dd></div>
            </dl>
          </section>

          <section className="ficha-motorista-secao" aria-labelledby="ficha-dados-pessoais">
            <h2 id="ficha-dados-pessoais">Dados pessoais</h2>
            <dl className="ficha-motorista-grade">
              <div><dt>CPF</dt><dd>{motorista.cpf}</dd></div>
              <div><dt>PIS</dt><dd>{valorOuAusente(motorista.pis)}</dd></div>
              <div><dt>Data de nascimento</dt><dd>{formatarData(motorista.dataNascimento)}</dd></div>
            </dl>
          </section>

          <section className="ficha-motorista-secao" aria-labelledby="ficha-endereco">
            <h2 id="ficha-endereco">Endereço</h2>
            <dl className="ficha-motorista-grade">
              <div><dt>CEP</dt><dd>{valorOuAusente(motorista.cep)}</dd></div>
              <div><dt>Logradouro</dt><dd>{valorOuAusente(motorista.logradouro)}</dd></div>
              <div><dt>Número</dt><dd>{valorOuAusente(motorista.numero)}</dd></div>
              <div><dt>Bairro</dt><dd>{valorOuAusente(motorista.bairro)}</dd></div>
              <div><dt>ID da cidade</dt><dd>{valorOuAusente(motorista.idCidade)}</dd></div>
            </dl>
          </section>

          {mes && comparacao && (() => {
            const indicadores = mesesComparacao.map((mesHistorico) =>
              comparacao.historico.find((item) => item.mesReferencia === mesHistorico)?.indicadores,
            );
            return (
              <ComparativoMotorista
                meses={mesesComparacao}
                utilizacao={indicadores.map((item) => item?.utilizacao)}
                efetividade={[
                  comparacao.veiculoAnterior?.percentualEfetividade,
                  comparacao.veiculoIntermediario?.percentualEfetividade,
                  comparacao.veiculoAtual?.percentualEfetividade,
                ]}
                financeiros={indicadores}
                mostrarFinanceiros={podeVerDadosFinanceiros()}
              />
            );
          })()}
          {erroComparacao && <div className="operacao-mensal-feedback" role="alert">Não foi possível carregar a comparação com o mês anterior.</div>}
        </>
      )}
    </section>
  );
}
