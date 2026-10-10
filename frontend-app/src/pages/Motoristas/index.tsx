import { useEffect, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { TableSkeleton } from "../../components/SkeletonLoader/TableSkeleton";
import {
  buscarMotoristasMes,
  type MotoristasMesPage,
  type MotoristaMes,
} from "../../services/Motoristas";
import "./motoristas.css";

type Status = "loading" | "success" | "error";
type ResultadoBusca =
  | { chave: string; status: "success"; dados: MotoristasMesPage }
  | { chave: string; status: "error" };

function mesAtual() {
  const hoje = new Date();
  return `${hoje.getFullYear()}-${String(hoje.getMonth() + 1).padStart(2, "0")}`;
}

function percentual(valor: number | null) {
  return valor === null
    ? "—"
    : `${valor.toLocaleString("pt-BR", { maximumFractionDigits: 2 })}%`;
}

function paginaDaUrl(valor: string | null) {
  if (!valor || !/^\d+$/.test(valor)) return 1;
  const pagina = Number(valor);
  return Number.isSafeInteger(pagina) && pagina > 0 && pagina <= 2_147_483_647
    ? pagina
    : 1;
}

function valoresMotorista(motorista: MotoristaMes) {
  return [
    { rotulo: "Veículo", valor: motorista.tipoVeiculo },
    { rotulo: "Placa", valor: motorista.placa },
    { rotulo: "Viagens no mês", valor: String(motorista.viagensNoMes) },
    { rotulo: "Dias em operação", valor: String(motorista.diasUtilizados) },
    { rotulo: "Disponibilidade", valor: percentual(motorista.disponibilidade) },
    { rotulo: "Utilização", valor: percentual(motorista.utilizacao) },
    { rotulo: "Efetividade", valor: percentual(motorista.percentualEfetividade) },
  ];
}

export function Motoristas() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const mesParametro = searchParams.get("mes");
  const mes = mesParametro && /^\d{4}-(0[1-9]|1[0-2])$/.test(mesParametro)
    ? mesParametro
    : mesAtual();
  const paginaParametro = searchParams.get("page");
  const pagina = paginaDaUrl(paginaParametro);
  const [resultado, setResultado] = useState<ResultadoBusca | null>(null);
  const [tentativa, setTentativa] = useState(0);
  const chaveConsulta = `${mes}|${pagina}|${tentativa}`;
  const status: Status = resultado?.chave === chaveConsulta
    ? resultado.status
    : "loading";
  const dados = resultado?.chave === chaveConsulta && resultado.status === "success"
    ? resultado.dados
    : null;

  useEffect(() => {
    if (mesParametro !== mes || paginaParametro !== String(pagina)) {
      setSearchParams((atual) => {
        const normalizados = new URLSearchParams(atual);
        normalizados.set("mes", mes);
        normalizados.set("page", String(pagina));
        return normalizados;
      }, { replace: true });
    }
  }, [mes, mesParametro, pagina, paginaParametro, setSearchParams]);

  useEffect(() => {
    let cancelado = false;

    buscarMotoristasMes(mes, pagina)
      .then((resultado) => {
        if (cancelado) return;
        if (pagina > Math.max(resultado.totalPages, 1)) {
          setSearchParams((atual) => {
            const normalizados = new URLSearchParams(atual);
            normalizados.set("page", String(Math.max(resultado.totalPages, 1)));
            return normalizados;
          }, { replace: true });
          return;
        }
        setResultado({ chave: chaveConsulta, status: "success", dados: resultado });
      })
      .catch(() => {
        if (cancelado) return;
        setResultado({ chave: chaveConsulta, status: "error" });
      });

    return () => {
      cancelado = true;
    };
  }, [chaveConsulta, mes, pagina, setSearchParams]);

  function atualizarConsulta(proximaMes: string, proximaPagina: number) {
    setSearchParams((atual) => {
      const parametros = new URLSearchParams(atual);
      parametros.set("mes", proximaMes);
      parametros.set("page", String(proximaPagina));
      return parametros;
    });
  }

  return (
    <section className="motoristas-card">
      <div className="motoristas-header">
        <div>
          <h1 className="motoristas-title">Motoristas</h1>
          <p className="motoristas-subtitle">
            Viagens e indicadores operacionais por mês
          </p>
        </div>

        <div className="motoristas-filtro">
          <label htmlFor="motoristas-mes">Mês</label>
          <input
            id="motoristas-mes"
            type="month"
            value={mes}
            onChange={(event) => {
              if (event.target.value) atualizarConsulta(event.target.value, 1);
            }}
          />
        </div>
      </div>

      {status === "loading" && <TableSkeleton />}

      {status === "error" && (
        <div className="motoristas-feedback" role="alert">
          <p>Não foi possível carregar os motoristas.</p>
          <button
            type="button"
            onClick={() => setTentativa((atual) => atual + 1)}
          >
            Tentar novamente
          </button>
        </div>
      )}

      {status === "success" && dados && dados.content.length === 0 && (
        <div className="motoristas-feedback">
          <p>Nenhum motorista encontrado para este mês.</p>
        </div>
      )}

      {status === "success" && dados && dados.content.length > 0 && (
        <>
          <div className="motoristas-table-wrap">
            <table className="motoristas-table">
              <thead>
                <tr>
                  <th scope="col">Motorista</th>
                  <th scope="col">Veículo</th>
                  <th scope="col">Placa</th>
                  <th scope="col">Viagens</th>
                  <th scope="col">Dias em operação</th>
                  <th scope="col">Disponibilidade</th>
                  <th scope="col">Utilização</th>
                  <th scope="col">Efetividade</th>
                </tr>
              </thead>
              <tbody>
                {dados.content.map((motorista) => {
                  const destino = `/motoristas/${motorista.motoristaId}?mes=${encodeURIComponent(mes)}&page=${pagina}`;
                  return (
                    <tr
                      key={`${motorista.motoristaId}-${motorista.veiculoId}`}
                      className="motoristas-table-row"
                      onClick={() => navigate(destino)}
                    >
                      <td className="motoristas-nome">
                        <Link
                          to={destino}
                          className="motoristas-profile-link"
                          onClick={(event) => event.stopPropagation()}
                        >
                          {motorista.nomeMotorista}
                        </Link>
                      </td>
                      {valoresMotorista(motorista).map((campo) => (
                        <td key={campo.rotulo} data-label={campo.rotulo}>
                          {campo.valor}
                        </td>
                      ))}
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          <div className="motoristas-mobile-cards">
            {dados.content.map((motorista) => (
              <Link
                key={`${motorista.motoristaId}-${motorista.veiculoId}`}
                to={`/motoristas/${motorista.motoristaId}?mes=${encodeURIComponent(mes)}&page=${pagina}`}
                className="motorista-mobile-card"
              >
                <span className="motorista-mobile-name">
                  {motorista.nomeMotorista}
                  <span aria-hidden="true">›</span>
                </span>
                <span className="motorista-mobile-fields">
                  {valoresMotorista(motorista).map((campo) => (
                    <span className="motorista-mobile-field" key={campo.rotulo}>
                      <span>{campo.rotulo}</span>
                      <strong>{campo.valor}</strong>
                    </span>
                  ))}
                </span>
              </Link>
            ))}
          </div>

          <nav className="motoristas-paginacao" aria-label="Paginação de motoristas">
            <span className="motoristas-contagem">
              {dados.totalElements.toLocaleString("pt-BR")} motoristas
            </span>
            <div className="motoristas-paginacao-controles">
              <button
                type="button"
                aria-label="Página anterior"
                disabled={pagina <= 1}
                onClick={() => atualizarConsulta(mes, pagina - 1)}
              >
                Anterior
              </button>
              <span aria-live="polite">
                Página {pagina} de {Math.max(dados.totalPages, 1)}
              </span>
              <button
                type="button"
                aria-label="Próxima página"
                disabled={pagina >= dados.totalPages}
                onClick={() => atualizarConsulta(mes, pagina + 1)}
              >
                Próxima
              </button>
            </div>
          </nav>
        </>
      )}
    </section>
  );
}
