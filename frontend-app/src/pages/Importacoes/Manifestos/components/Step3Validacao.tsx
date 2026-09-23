import { CheckCircle2, AlertCircle } from 'lucide-react';
import { podeExecutar, type ResultadoValidacao } from '../../../../services/importacao';
import './step3.css';

interface PropsPasso3 {
    validacao: ResultadoValidacao | null;
}

export function Step3Validacao({ validacao }: PropsPasso3) {
    if (!validacao) {
        return <p className="validacao-vazio">Conferindo os dados...</p>;
    }

    const liberado = podeExecutar(validacao);
    const temMaisErros = validacao.linhasInvalidas > validacao.erros.length;

    return (
        <div className="validacao-wrap">
            <div className="validacao-numeros">
                <div className="validacao-card">
                    <span className="validacao-valor">{validacao.totalLinhas}</span>
                    <span className="validacao-rotulo">linhas no arquivo</span>
                </div>
                <div className="validacao-card ok">
                    <span className="validacao-valor">{validacao.linhasValidas}</span>
                    <span className="validacao-rotulo">válidas</span>
                </div>
                <div className={`validacao-card ${validacao.linhasInvalidas > 0 ? 'falha' : ''}`}>
                    <span className="validacao-valor">{validacao.linhasInvalidas}</span>
                    <span className="validacao-rotulo">com erro</span>
                </div>
            </div>

            {liberado ? (
                <div className="validacao-alerta ok">
                    <CheckCircle2 size={18} />
                    <span>Nenhum erro encontrado. O arquivo está pronto para ser importado.</span>
                </div>
            ) : (
                <div className="validacao-alerta falha">
                    <AlertCircle size={18} />
                    <span>
                        O arquivo não pode ser importado enquanto houver erro. Corrija as linhas
                        abaixo e envie novamente — <strong>nada será gravado</strong> até o arquivo
                        estar limpo.
                    </span>
                </div>
            )}

            {validacao.erros.length > 0 && (
                <div className="erro-lista">
                    <h4 className="erro-titulo">
                        Linhas com problema
                        {temMaisErros && (
                            <span className="erro-limite">
                                mostrando as {validacao.erros.length} primeiras de {validacao.linhasInvalidas}
                            </span>
                        )}
                    </h4>

                    <table className="erro-tabela">
                        <thead>
                            <tr>
                                <th>Linha</th>
                                <th>Motivo</th>
                            </tr>
                        </thead>
                        <tbody>
                            {validacao.erros.map((erro) => (
                                <tr key={erro.numeroLinha}>
                                    <td className="erro-numero">{erro.numeroLinha}</td>
                                    <td>{erro.mensagem}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}
