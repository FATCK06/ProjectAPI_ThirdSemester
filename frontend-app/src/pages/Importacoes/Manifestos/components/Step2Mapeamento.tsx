import { useState } from "react";
import { CheckCircle2, AlertCircle, ArrowRight, ArrowDown } from 'lucide-react';
import './step2.css';

export interface LinhaMapeamento {
  id: string;
  campoDestino: string;
  obrigatorio: boolean;
  campoOrigem: string;
  valido: boolean;
}

interface PropsPasso2 {
  linhasIniciais?: LinhaMapeamento[];
}

const LINHAS_PADRAO: LinhaMapeamento[] = [
  { id: "1", campoDestino: "CNPJ", obrigatorio: true, campoOrigem: "cnpj", valido: true },
  { id: "2", campoDestino: "Nome do Manifesto", obrigatorio: true, campoOrigem: "Nome do Manifesto", valido: true },
  { id: "3", campoDestino: "Endereço", obrigatorio: false, campoOrigem: "Endereço", valido: true },
  { id: "4", campoDestino: "Destino", obrigatorio: false, campoOrigem: "Destino", valido: false }, // Mudei um para false para você ver o erro visual
  { id: "5", campoDestino: "kg", obrigatorio: false, campoOrigem: "kg", valido: true },
  { id: "6", campoDestino: "Despacho", obrigatorio: true, campoOrigem: "Despacho", valido: true },
  { id: "7", campoDestino: "Entregas", obrigatorio: false, campoOrigem: "Entregas", valido: true },
];

export function Step2Mapeamento({ linhasIniciais = LINHAS_PADRAO }: PropsPasso2) {
  const [linhas] = useState<LinhaMapeamento[]>(linhasIniciais);

  return (
    <div className="mapeamento-wrap">
      <div className="mapeamento-cartao">
        
        {/* Cabeçalho */}
        <div className="mapeamento-grade mapeamento-cabecalho">
          <div className="mapeamento-esquerda">
            <span className="mapeamento-status">Status</span>
            <span className="mapeamento-titulo-destino">Campo do Sistema</span>
          </div>
          <span /> {/* Espaço da seta */}
          <div className="mapeamento-direita">Campo da sua Planilha</div>
        </div>

        {/* Linhas de Dados */}
        {linhas.map((linha) => (
          <div key={linha.id} className="mapeamento-grade mapeamento-linha">
            <div className="mapeamento-esquerda">
              <div className="mapeamento-status">
                {linha.valido ? (
                  <CheckCircle2 size={18} className="icone-ok" />
                ) : (
                  <AlertCircle size={18} className="icone-erro" />
                )}
              </div>
              <div className="mapeamento-destino">
                {linha.campoDestino}
                {linha.obrigatorio && <span className="mapeamento-obrigatorio">*</span>}
              </div>
            </div>

            <div className="mapeamento-seta">
              <ArrowRight size={16} className="seta-h" />
              <ArrowDown size={16} className="seta-v" />
            </div>

            <div className="mapeamento-direita">
              <div className={`mapeamento-caixa ${!linha.valido ? 'caixa-erro' : ''}`}>
                {linha.campoOrigem}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}