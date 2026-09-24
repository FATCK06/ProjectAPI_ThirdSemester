import { CheckCircle2, AlertCircle, AlertTriangle } from 'lucide-react';
import {
    linhaBloqueia,
    obrigatoriasFaltando,
    podeExecutar,
    type LinhaComProblema,
    type ProblemaCelula,
    type ResultadoValidacao,
} from '../../../../services/importacao';
import './step3.css';

interface PropsPasso3 {
    validacao: ResultadoValidacao | null;
}

function DetalheProblema({ problema }: { problema: ProblemaCelula }) {
    return (
        <li>
            <p className="erro-card-mensagem">{problema.mensagem}</p>
            {problema.coluna && (
                <dl className="erro-card-detalhe">
                    <dt>Valor encontrado</dt>
                    <dd className="erro-card-valor">
                        {problema.valorEncontrado?.trim() ? problema.valorEncontrado : '(vazio)'}
                    </dd>
                    {problema.valorEsperado && (
                        <>
                            <dt>Esperado</dt>
                            <dd>{problema.valorEsperado}</dd>
                        </>
                    )}
                </dl>
            )}
        </li>
    );
}

/** Vermelho quando a linha bloqueia a importação; amarelo quando é só pendência. */
function CardLinha({ linha }: { linha: LinhaComProblema }) {
    const bloqueia = linhaBloqueia(linha);
    // Numa linha que bloqueia, as pendências dela iriam só poluir o card.
    const exibidos = bloqueia ? linha.problemas.filter((p) => p.severidade === 'ERRO') : linha.problemas;

    return (
        <div className={`erro-card ${bloqueia ? '' : 'pendencia'}`}>
            <div className="erro-card-titulo">
                {bloqueia ? <AlertCircle size={18} /> : <AlertTriangle size={18} />}
                <span>Linha {linha.numeroLinha}</span>
                {!bloqueia && <span className="erro-card-selo">aviso — não impede a importação</span>}
            </div>
            <ul className="erro-card-lista">
                {exibidos.map((problema, i) => (
                    <DetalheProblema key={i} problema={problema} />
                ))}
            </ul>
        </div>
    );
}

export function Step3Validacao({ validacao }: PropsPasso3) {
    if (!validacao) {
        return <p className="validacao-vazio">Conferindo os dados...</p>;
    }

    const liberado = podeExecutar(validacao);
    const colunasFaltando = obrigatoriasFaltando(validacao);
    const arquivoSemDados = validacao.totalLinhas === 0;
    const temMaisProblemas = validacao.pagina.totalElementos > validacao.problemas.length;

    // Erros primeiro: é o que o usuário precisa corrigir para seguir.
    const linhas = [...validacao.problemas].sort(
        (a, b) => Number(linhaBloqueia(b)) - Number(linhaBloqueia(a)) || a.numeroLinha - b.numeroLinha,
    );

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
                        O arquivo não pode ser importado enquanto houver erro. Corrija os pontos
                        abaixo e envie novamente — <strong>nada será gravado</strong> até o arquivo
                        estar limpo.
                    </span>
                </div>
            )}

            <div className="erro-lista">
                {arquivoSemDados && (
                    <div className="erro-card">
                        <div className="erro-card-titulo">
                            <AlertCircle size={18} />
                            <span>Arquivo sem dados</span>
                        </div>
                        <p className="erro-card-mensagem">O CSV não tem nenhuma linha além do cabeçalho.</p>
                    </div>
                )}

                {colunasFaltando.length > 0 && (
                    <div className="erro-card">
                        <div className="erro-card-titulo">
                            <AlertCircle size={18} />
                            <span>Colunas obrigatórias ausentes</span>
                        </div>
                        <p className="erro-card-mensagem">
                            O cabeçalho do arquivo não tem: <strong>{colunasFaltando.join(', ')}</strong>.
                        </p>
                    </div>
                )}

                {linhas.length > 0 && (
                    <h4 className="erro-titulo">
                        Linhas com problema
                        {temMaisProblemas && (
                            <span className="erro-limite">
                                mostrando {validacao.problemas.length} de {validacao.pagina.totalElementos}
                            </span>
                        )}
                    </h4>
                )}

                {linhas.map((linha) => (
                    <CardLinha key={linha.numeroLinha} linha={linha} />
                ))}
            </div>
        </div>
    );
}
