import { useState } from "react";
import { Download } from "lucide-react";
import "./exportarCsvButton.css";

type EstadoExportacao = "ocioso" | "gerando" | "baixando" | "concluido" | "erro";
type ProgressoDownload = (progresso?: number) => void;

interface PropsExportarCsvButton {
  nomeArquivo: string;
  baixarCsv: (aoProgredir: ProgressoDownload) => Promise<Blob>;
  disabled?: boolean;
}

const ETAPAS = ["Gerando planilha", "Recebendo arquivo", "Download iniciado"];

export function ExportarCsvButton({
  nomeArquivo,
  baixarCsv,
  disabled = false,
}: PropsExportarCsvButton) {
  const [estado, setEstado] = useState<EstadoExportacao>("ocioso");
  const [progresso, setProgresso] = useState<number>();
  const [erro, setErro] = useState(false);
  const emAndamento = estado === "gerando" || estado === "baixando";

  async function exportar() {
    setEstado("gerando");
    setProgresso(undefined);
    setErro(false);

    try {
      const arquivo = await baixarCsv((valor) => {
        setEstado("baixando");
        setProgresso(valor === undefined ? undefined : Math.round(valor * 100));
      });
      const url = URL.createObjectURL(arquivo);
      const link = document.createElement("a");
      link.href = url;
      link.download = nomeArquivo;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.setTimeout(() => URL.revokeObjectURL(url), 1000);
      setEstado("concluido");
    } catch {
      setEstado("erro");
      setErro(true);
    }
  }

  const etapaAtual = estado === "gerando" ? 0
    : estado === "baixando" ? 1
      : estado === "concluido" ? ETAPAS.length : -1;

  return (
    <div className="exportar-csv">
      <button
        className="exportar-csv-botao"
        type="button"
        onClick={exportar}
        disabled={disabled || emAndamento}
      >
        <Download size={16} aria-hidden="true" />
        {emAndamento ? "Gerando planilha..." : "Exportar CSV"}
      </button>

      {estado !== "ocioso" && (
        <div className="exportar-csv-progresso" role={erro ? "alert" : "status"} aria-live="polite">
          <ol aria-label="Etapas da exportação">
            {ETAPAS.map((etapa, indice) => (
              <li
                key={etapa}
                className={indice < etapaAtual ? "concluida" : indice === etapaAtual ? "ativa" : ""}
                aria-current={indice === etapaAtual ? "step" : undefined}
              >
                {etapa}
                {indice === 1 && estado === "baixando" && progresso !== undefined
                  ? ` (${progresso}%)`
                  : ""}
              </li>
            ))}
          </ol>
          {erro && <p>Não foi possível gerar a planilha. Tente novamente.</p>}
        </div>
      )}
    </div>
  );
}
