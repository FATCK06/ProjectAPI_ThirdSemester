import { CheckCircle2, FileText } from 'lucide-react';
import type { ImportacaoCriada, ResultadoValidacao } from '../../../../services/importacao';
import './step6.css';

interface PropsPasso6 {
    importacao: ImportacaoCriada;
    validacao: ResultadoValidacao | null;
    linhasGravadas: number;
    aoRecomecar: () => void;
}

export function Step6Resultado({ importacao, validacao, linhasGravadas, aoRecomecar }: PropsPasso6) {
    const lidas = validacao?.linhasValidas ?? linhasGravadas;
    // id_manifesto é UNIQUE: reimportar o mesmo arquivo pula o que já entrou.
    const ignoradas = Math.max(0, lidas - linhasGravadas);

    return (
        <div className="resultado-wrap">
            <div className="resultado-selo">
                <CheckCircle2 size={48} />
            </div>

            <h3 className="resultado-titulo">Importação concluída</h3>

            <div className="resultado-arquivo">
                <FileText size={18} />
                <span>{importacao.arquivoNome}</span>
                <span className="resultado-id">#{importacao.id}</span>
            </div>

            <div className="resultado-numeros">
                <div className="resultado-card destaque">
                    <span className="resultado-valor">{linhasGravadas}</span>
                    <span className="resultado-rotulo">viagens gravadas</span>
                </div>
                <div className="resultado-card">
                    <span className="resultado-valor">{lidas}</span>
                    <span className="resultado-rotulo">linhas lidas</span>
                </div>
                {ignoradas > 0 && (
                    <div className="resultado-card">
                        <span className="resultado-valor">{ignoradas}</span>
                        <span className="resultado-rotulo">já existiam</span>
                    </div>
                )}
            </div>

            {ignoradas > 0 && (
                <p className="resultado-nota">
                    {ignoradas} {ignoradas === 1 ? 'manifesto já havia sido importado' : 'manifestos já haviam sido importados'} antes
                    e {ignoradas === 1 ? 'foi ignorado' : 'foram ignorados'}. Reimportar o mesmo arquivo não duplica dados.
                </p>
            )}

            <button className="btn-recomecar" onClick={aoRecomecar}>
                Importar outro arquivo
            </button>
        </div>
    );
}
