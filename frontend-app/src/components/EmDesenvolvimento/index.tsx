import { Construction } from "lucide-react";
import "./emDesenvolvimento.css";

type EmDesenvolvimentoProps = {
  mensagem?: string;
  altura?: number;
};

/** Ocupa o lugar de um gráfico ou bloco que ainda não tem dados para ser exibido. */
export function EmDesenvolvimento({
  mensagem = "Este conteúdo estará disponível em breve.",
  altura = 300,
}: EmDesenvolvimentoProps) {
  return (
    <div className="em-desenvolvimento" style={{ minHeight: altura }} role="status">
      <Construction size={32} aria-hidden="true" />
      <strong>Em desenvolvimento</strong>
      <p>{mensagem}</p>
    </div>
  );
}
