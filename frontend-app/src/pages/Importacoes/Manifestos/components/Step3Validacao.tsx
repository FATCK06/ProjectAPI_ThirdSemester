import { useState } from "react";
import styles from "./step3.module.css";

export interface LinhaMotorista {
  id: string;
  manifesto: string;
  motorista: string;
  cpf: string;
  status: string;
  veiculo: string;
}

interface PropsPasso3 {
  linhas?: LinhaMotorista[];
  total?: number;
  painelAbertoInicial?: boolean;
}

const LINHAS_PADRAO: LinhaMotorista[] = [
  {
    id: "1",
    manifesto: "16882",
    motorista: "",
    cpf: "000.000.00",
    status: "",
    veiculo: "HNW3E20",
  },
  {
    id: "2",
    manifesto: "16908",
    motorista: "",
    cpf: "",
    status: "",
    veiculo: "",
  },
  {
    id: "3",
    manifesto: "16912",
    motorista: "",
    cpf: "",
    status: "",
    veiculo: "",
  },
  {
    id: "4",
    manifesto: "16912",
    motorista: "",
    cpf: "",
    status: "",
    veiculo: "",
  },
  {
    id: "5",
    manifesto: "16912",
    motorista: "",
    cpf: "",
    status: "",
    veiculo: "",
  },
  {
    id: "6",
    manifesto: "16912",
    motorista: "",
    cpf: "",
    status: "",
    veiculo: "",
  },
  {
    id: "7",
    manifesto: "16912",
    motorista: "",
    cpf: "",
    status: "",
    veiculo: "",
  },
  {
    id: "8",
    manifesto: "16912",
    motorista: "",
    cpf: "",
    status: "",
    veiculo: "",
  },
  {
    id: "9",
    manifesto: "16912",
    motorista: "",
    cpf: "",
    status: "",
    veiculo: "",
  },
];

export function Step3Validacao({
  linhas = LINHAS_PADRAO,
  total = 9,
  painelAbertoInicial = false,
}: PropsPasso3) {
  const [painelAberto, setPainelAberto] = useState(painelAbertoInicial);
  const [linhaSelecionada, setLinhaSelecionada] = useState<string | null>(null);

  const selecionarLinha = (id: string) => {
    setLinhaSelecionada(id);
    setPainelAberto(true);
  };

  return (
    <div className={styles.wrap}>
      <div className={styles.conteudo}>
        <div className={styles.cartao}>
          <div className={styles.rolagem}>
            <table className={styles.tabela}>
              <thead>
                <tr>
                  <th className={styles.indice} />
                  <th>Manifesto</th>
                  <th>Motorista</th>
                  <th>CPF</th>
                  <th>Status</th>
                  <th>Veículo</th>
                </tr>
              </thead>
              <tbody>
                {linhas.map((linha, i) => (
                  <tr
                    key={linha.id}
                    className={`${styles.linha} ${
                      linhaSelecionada === linha.id
                        ? styles.linhaSelecionada
                        : ""
                    }`}
                    onClick={() => selecionarLinha(linha.id)}
                  >
                    <td className={styles.indice}>{i + 1}</td>
                    <td>{linha.manifesto}</td>
                    <td>{linha.motorista}</td>
                    <td>{linha.cpf}</td>
                    <td>{linha.status}</td>
                    <td>{linha.veiculo}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className={styles.rodape}>
            Mostrando <strong>1-{linhas.length}</strong> de{" "}
            <strong>{total}</strong> motoristas
          </div>
        </div>

        {painelAberto && (
          <aside className={styles.painel}>
            <button
              type="button"
              className={styles.fechar}
              aria-label="Fechar painel"
              onClick={() => setPainelAberto(false)}
            >
              ✕
            </button>
            <div className={styles.blocoCinza} />
            <div className={styles.blocoCinza} />
          </aside>
        )}
      </div>
    </div>
  );
}
