import { useState } from "react";
import {
  Lock,
  Hourglass,
  CheckCircle2,
  ArrowLeft,
  ArrowRight,
} from "lucide-react";
import { Step1Upload } from "./components/Step1Upload";
import { Step2Mapeamento } from "./components/Step2Mapeamento";
import type { ImportacaoCriada } from "../../../services/importacao";
import "./manifestos.css";
import { Step3Validacao } from "./components/Step3Validacao";

const STEPS = [
  "Upload de Arquivo",
  "Mapeamento de Títulos de Colunas",
  "Validação de Dados",
  "Revisão/Preview",
  "Confirmação e execução",
  "Resultado",
];

export function ManifestosImport() {
  const [passoAtual, setPassoAtual] = useState(3);
  // Guardado aqui porque os passos seguintes (validação, preview, resultado)
  // precisam do id da importação criada no passo 1.
  const [importacao, setImportacao] = useState<ImportacaoCriada | null>(null);

  const proximoPasso = () =>
    setPassoAtual((prev) => Math.min(prev + 1, STEPS.length));
  const voltarPasso = () => setPassoAtual((prev) => Math.max(prev - 1, 1));

  // Sem arquivo importado não há o que mapear nos passos seguintes.
  const podeContinuar =
    passoAtual === 1 ? importacao !== null : passoAtual < STEPS.length;

  return (
    <div className="importacao-container">
      <div className="stepper-wrapper">
        {STEPS.map((step, index) => {
          const stepNumber = index + 1;
          const isActive = stepNumber === passoAtual;
          const isConcluido = stepNumber < passoAtual;
          const isPending = stepNumber > passoAtual;

          return (
            <div
              key={step}
              className={`step-item ${isActive ? "active" : ""} ${isConcluido ? "concluido" : ""} ${isPending ? "pending" : ""}`}
            >
              <div className="step-icon-wrapper">
                {isConcluido ? (
                  <CheckCircle2 size={20} />
                ) : isActive ? (
                  <Hourglass size={20} />
                ) : (
                  <Lock size={20} />
                )}
              </div>
              <span className="step-title">{step}</span>
              <span className="step-badge">
                {isConcluido
                  ? "Concluído"
                  : isActive
                    ? "Em progresso"
                    : "Pendente"}
              </span>
              {stepNumber < STEPS.length && (
                <div className="step-connector"></div>
              )}
            </div>
          );
        })}
      </div>

      <div className="step-content">
        {passoAtual === 1 && (
          <Step1Upload importacao={importacao} aoImportar={setImportacao} />
        )}
        {passoAtual === 2 && <Step2Mapeamento />}
        {passoAtual == 3 && <Step3Validacao />}
      </div>

      <div className="step-footer">
        <button
          className="btn-voltar"
          onClick={voltarPasso}
          disabled={passoAtual === 1}
        >
          <ArrowLeft size={20} />
          Anterior
        </button>

        <button
          className="btn-continuar"
          onClick={proximoPasso}
          disabled={!podeContinuar}
          title={
            passoAtual === 1 && !importacao
              ? "Envie um arquivo para continuar"
              : undefined
          }
        >
          Continuar
          <ArrowRight size={20} />
        </button>
      </div>
    </div>
  );
}
