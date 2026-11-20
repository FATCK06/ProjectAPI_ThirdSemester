import { useEffect, useRef, useState } from "react";
import { Check, Circle, Download, LoaderCircle, X } from "lucide-react";
import { usePosicaoPopover } from "../../hooks/usePosicaoPopover";
import "./exportarCsvButton.css";

// "saindo": o painel está fazendo a animação de saída antes de sumir
type EstadoExportacao = "ocioso" | "gerando" | "baixando" | "concluido" | "saindo" | "erro";
type ProgressoDownload = (progresso?: number) => void;

interface PropsExportarCsvButton {
  nomeArquivo: string;
  baixarCsv: (aoProgredir: ProgressoDownload) => Promise<Blob>;
  disabled?: boolean;
}

const ETAPAS = ["Gerando planilha", "Recebendo arquivo", "Download iniciado"];
// Quanto o painel fica na tela depois do download iniciado, e quanto dura a saída
const TEMPO_CONCLUIDO_MS = 2000;
const TEMPO_SAIDA_MS = 200;
const TEMPO_ERRO_MS = 6000;

/**
 * Botão de exportar com as etapas num painel flutuante logo abaixo dele: o
 * painel não ocupa espaço no cabeçalho, então o botão não sobe nem desce quando
 * ele aparece. Concluído (ou com erro), o painel some sozinho.
 */
export function ExportarCsvButton({
  nomeArquivo,
  baixarCsv,
  disabled = false,
}: PropsExportarCsvButton) {
  const [estado, setEstado] = useState<EstadoExportacao>("ocioso");
  const [progresso, setProgresso] = useState<number>();
  const emAndamento = estado === "gerando" || estado === "baixando";
  const botaoRef = useRef<HTMLButtonElement>(null);
  const painelRef = useRef<HTMLDivElement>(null);
  // Só o lado importa: o painel abre sempre para baixo, alinhado pela esquerda
  // do botão, ou pela direita quando sairia da tela (botão no canto do cabeçalho)
  const { direita } = usePosicaoPopover(estado !== "ocioso", botaoRef, painelRef);

  // Concluído: espera um pouco para a pessoa ver o "Download iniciado", anima a
  // saída e volta ao normal. Com erro, dá mais tempo para ler a mensagem.
  useEffect(() => {
    if (estado !== "concluido" && estado !== "erro" && estado !== "saindo") return;
    const espera = estado === "saindo" ? TEMPO_SAIDA_MS : estado === "erro" ? TEMPO_ERRO_MS : TEMPO_CONCLUIDO_MS;
    const timer = window.setTimeout(
      () => setEstado((atual) => (atual === "saindo" ? "ocioso" : "saindo")),
      espera,
    );
    return () => window.clearTimeout(timer);
  }, [estado]);

  async function exportar() {
    setEstado("gerando");
    setProgresso(undefined);

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
    }
  }

  const etapaAtual = estado === "gerando" ? 0
    : estado === "baixando" ? 1
      : estado === "concluido" || estado === "saindo" ? ETAPAS.length : -1;
  const erro = estado === "erro";

  return (
    <div className="exportar-csv" data-tour="exportar-csv">
      <button
        ref={botaoRef}
        className="exportar-csv-botao"
        type="button"
        onClick={exportar}
        disabled={disabled || emAndamento}
        aria-busy={emAndamento}
      >
        {emAndamento ? (
          <LoaderCircle className="exportar-csv-girando" size={16} aria-hidden="true" />
        ) : (
          <Download size={16} aria-hidden="true" />
        )}
        {emAndamento ? "Exportando..." : "Exportar CSV"}
      </button>

      {estado !== "ocioso" && (
        <div
          ref={painelRef}
          className="exportar-csv-painel"
          data-direita={direita || undefined}
          data-saindo={estado === "saindo" || undefined}
          role={erro ? "alert" : "status"}
          aria-live="polite"
        >
          {erro ? (
            <div className="exportar-csv-erro">
              <p>Não foi possível gerar a planilha. Tente novamente.</p>
              <button type="button" aria-label="Fechar aviso" onClick={() => setEstado("saindo")}>
                <X size={14} aria-hidden="true" />
              </button>
            </div>
          ) : (
            <ol aria-label="Etapas da exportação">
              {ETAPAS.map((etapa, indice) => {
                const situacao = indice < etapaAtual ? "concluida" : indice === etapaAtual ? "ativa" : "pendente";
                return (
                  <li key={etapa} data-situacao={situacao} aria-current={situacao === "ativa" ? "step" : undefined}>
                    <span className="exportar-csv-icone" aria-hidden="true">
                      {situacao === "concluida" ? (
                        <Check size={14} strokeWidth={3} />
                      ) : situacao === "ativa" ? (
                        <LoaderCircle className="exportar-csv-girando" size={14} />
                      ) : (
                        <Circle size={14} />
                      )}
                    </span>
                    <span className="exportar-csv-etapa">
                      {etapa}
                      {indice === 1 && situacao === "ativa" && progresso !== undefined && (
                        <span className="exportar-csv-barra" aria-hidden="true">
                          <span style={{ width: `${progresso}%` }} />
                        </span>
                      )}
                    </span>
                    {indice === 1 && situacao === "ativa" && progresso !== undefined && (
                      <span className="exportar-csv-percentual">{progresso}%</span>
                    )}
                  </li>
                );
              })}
            </ol>
          )}
        </div>
      )}
    </div>
  );
}
