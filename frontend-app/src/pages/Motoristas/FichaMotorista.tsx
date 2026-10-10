import { useEffect, useState } from "react";
import { Link, useParams, useSearchParams } from "react-router-dom";
import {
  buscarFichaMotorista,
  type MotoristaFicha as MotoristaFichaDados,
} from "../../services/Motoristas";
import "./motoristas.css";

type Status = "loading" | "success" | "error";
type ResultadoFicha =
  | { id: string; status: "success"; motorista: MotoristaFichaDados }
  | { id: string; status: "error" };

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
  const [searchParams] = useSearchParams();
  const [resultado, setResultado] = useState<ResultadoFicha | null>(null);
  const status: Status = !motoristaId
    ? "error"
    : resultado?.id === motoristaId
      ? resultado.status
      : "loading";
  const motorista = resultado && resultado.id === motoristaId && resultado.status === "success"
    ? resultado.motorista
    : null;
  const mes = searchParams.get("mes");
  const pagina = searchParams.get("page");
  const queryVolta = new URLSearchParams();
  if (mes) queryVolta.set("mes", mes);
  if (pagina) queryVolta.set("page", pagina);
  const destinoVolta = `/motoristas${queryVolta.size ? `?${queryVolta}` : ""}`;

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

  return (
    <section className="motoristas-card ficha-motorista">
      <Link className="ficha-voltar" to={destinoVolta}>
        <span aria-hidden="true">←</span> Voltar para motoristas
      </Link>

      {status === "loading" && (
        <div className="motoristas-feedback" role="status">
          Carregando ficha do motorista…
        </div>
      )}

      {status === "error" && (
        <div className="motoristas-feedback" role="alert">
          <p>Não foi possível carregar a ficha deste motorista.</p>
          <Link className="ficha-voltar" to={destinoVolta}>
            Voltar para a lista
          </Link>
        </div>
      )}

      {status === "success" && motorista && (
        <>
          <header className="ficha-header">
            <div>
              <h1 className="motoristas-title">{motorista.nome}</h1>
              <p className="motoristas-subtitle">Ficha cadastral do motorista</p>
            </div>
          </header>

          <section className="ficha-secao" aria-labelledby="ficha-dados-pessoais">
            <h2 id="ficha-dados-pessoais">Dados pessoais</h2>
            <dl className="ficha-grid">
              <div><dt>CPF</dt><dd>{motorista.cpf}</dd></div>
              <div><dt>PIS</dt><dd>{valorOuAusente(motorista.pis)}</dd></div>
              <div><dt>Data de nascimento</dt><dd>{formatarData(motorista.dataNascimento)}</dd></div>
            </dl>
          </section>

          <section className="ficha-secao" aria-labelledby="ficha-endereco">
            <h2 id="ficha-endereco">Endereço</h2>
            <dl className="ficha-grid">
              <div><dt>CEP</dt><dd>{valorOuAusente(motorista.cep)}</dd></div>
              <div><dt>Logradouro</dt><dd>{valorOuAusente(motorista.logradouro)}</dd></div>
              <div><dt>Número</dt><dd>{valorOuAusente(motorista.numero)}</dd></div>
              <div><dt>Bairro</dt><dd>{valorOuAusente(motorista.bairro)}</dd></div>
              <div><dt>ID da cidade</dt><dd>{valorOuAusente(motorista.idCidade)}</dd></div>
            </dl>
          </section>
        </>
      )}
    </section>
  );
}
