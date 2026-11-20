import { useEffect, useMemo, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import {
  buscarMotoristasMes,
  type MotoristasMesPage,
} from "../../../services/Motoristas";
import { CampoMes } from "../../../components/CampoMes/CampoMes";
import { Loading } from "../../../components/Loading";
import { SeloMes } from "../../../components/SeloMes/SeloMes";
import { useMesDaUrl } from "../../../hooks/useMesDaUrl";
import { mesAtual } from "../../../utils/datas";
import "./motoristas.css";

type Resultado =
  | { chave: string; status: "success"; dados: MotoristasMesPage }
  | { chave: string; status: "error" };

function normalizarBusca(valor: string) {
  return valor.normalize("NFD").replace(/\p{Diacritic}/gu, "").toLocaleLowerCase("pt-BR").trim();
}

// Sem ?mes na URL, espera o mês mais recente com dados antes de montar a tela
export function Motoristas() {
  const mes = useMesDaUrl();
  return mes ? <MotoristasDoMes mes={mes} /> : <Loading texto="Carregando mês" />;
}

function MotoristasDoMes({ mes }: { mes: string }) {
  const [searchParams, setSearchParams] = useSearchParams();
  const paginaParametro = searchParams.get("page");
  const paginaLida = Number(paginaParametro);
  const pagina = Number.isSafeInteger(paginaLida) && paginaLida > 0 ? paginaLida : 1;
  const [busca, setBusca] = useState("");
  const [tentativa, setTentativa] = useState(0);
  const [resultado, setResultado] = useState<Resultado | null>(null);
  const chave = `${mes}|${pagina}|${tentativa}`;
  const dados = resultado?.chave === chave && resultado.status === "success" ? resultado.dados : null;
  const status = resultado?.chave === chave ? resultado.status : "loading";
  const motoristas = useMemo(() => {
    const termo = normalizarBusca(busca);
    const unicos = new Map<string, { id: string; nome: string }>();
    for (const motorista of dados?.content ?? []) {
      if (!unicos.has(motorista.motoristaId)) {
        unicos.set(motorista.motoristaId, { id: motorista.motoristaId, nome: motorista.nomeMotorista });
      }
    }
    return Array.from(unicos.values())
      .filter((motorista) => normalizarBusca(motorista.nome).includes(termo))
      .sort((a, b) => a.nome.localeCompare(b.nome, "pt-BR"));
  }, [busca, dados]);

  useEffect(() => {
    if (paginaParametro !== String(pagina)) {
      setSearchParams((atual) => {
        const normalizados = new URLSearchParams(atual);
        normalizados.set("page", String(pagina));
        return normalizados;
      }, { replace: true });
    }
  }, [pagina, paginaParametro, setSearchParams]);

  useEffect(() => {
    let cancelado = false;
    buscarMotoristasMes(mes, pagina, { tamanho: 100 })
      .then((resposta) => {
        if (cancelado) return;
        if (pagina > Math.max(resposta.totalPages, 1)) {
          setSearchParams((atual) => {
            const normalizados = new URLSearchParams(atual);
            normalizados.set("page", String(Math.max(resposta.totalPages, 1)));
            return normalizados;
          }, { replace: true });
          return;
        }
        setResultado({ chave, status: "success", dados: resposta });
      })
      .catch(() => { if (!cancelado) setResultado({ chave, status: "error" }); });
    return () => { cancelado = true; };
  }, [chave, mes, pagina, setSearchParams]);

  function atualizarConsulta(proximoMes: string, proximaPagina: number) {
    setSearchParams((atual) => {
      const parametros = new URLSearchParams(atual);
      parametros.set("mes", proximoMes);
      parametros.set("page", String(proximaPagina));
      return parametros;
    });
  }

  return (
    <section className="motoristas-card" data-tour="motoristas-pagina">
      <header className="motoristas-header">
        <div>
          <h1 className="motoristas-title">Motoristas<SeloMes mes={mes} /></h1>
          <p className="motoristas-subtitle">Acesse a ficha cadastral do motorista</p>
        </div>
        <div className="motoristas-filtros">
          <div className="motoristas-filtro" data-tour="motoristas-busca">
            <label htmlFor="motoristas-busca">Buscar motorista</label>
            <input id="motoristas-busca" type="search" placeholder="Nome do motorista" value={busca} onChange={(event) => setBusca(event.target.value)} />
          </div>
          <div className="motoristas-filtro" data-tour="motoristas-mes">
            <label htmlFor="motoristas-mes">Mês de referência</label>
            <CampoMes id="motoristas-mes" valor={mes} max={mesAtual()} onChange={(novoMes) => atualizarConsulta(novoMes, 1)} />
          </div>
        </div>
      </header>

      {status === "loading" && <Loading tamanho="sm" texto="Carregando motoristas" />}
      {status === "error" && (
        <div className="motoristas-feedback" role="alert">
          <p>Não foi possível carregar os motoristas.</p>
          <button type="button" onClick={() => setTentativa((atual) => atual + 1)}>Tentar novamente</button>
        </div>
      )}
      {status === "success" && dados && (
        <>
          {motoristas.length ? (
            <nav className="motoristas-lista-fichas" aria-label="Fichas cadastrais dos motoristas">
              {motoristas.map((motorista) => (
                <Link key={motorista.id} to={`/motoristas/${motorista.id}?mes=${encodeURIComponent(mes)}`}>
                  <span>{motorista.nome}</span><span aria-hidden="true">›</span>
                </Link>
              ))}
            </nav>
          ) : (
            <div className="motoristas-feedback" role="status">
              {dados.content.length === 0 ? "Nenhum motorista encontrado para este mês." : "Nenhum motorista corresponde à busca."}
            </div>
          )}
          <nav className="motoristas-paginacao" aria-label="Paginação de motoristas">
            <span className="motoristas-contagem">{motoristas.length} motoristas nesta página</span>
            <div className="motoristas-paginacao-controles">
              <button type="button" disabled={pagina <= 1} onClick={() => atualizarConsulta(mes, pagina - 1)}>Anterior</button>
              <span aria-live="polite">Página {pagina} de {Math.max(dados.totalPages, 1)}</span>
              <button type="button" disabled={pagina >= dados.totalPages} onClick={() => atualizarConsulta(mes, pagina + 1)}>Próxima</button>
            </div>
          </nav>
        </>
      )}
    </section>
  );
}
