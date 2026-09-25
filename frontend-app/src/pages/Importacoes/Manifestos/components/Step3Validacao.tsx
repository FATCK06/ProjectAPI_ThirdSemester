import { CheckCircle2, AlertCircle, AlertTriangle, ChevronLeft, ChevronRight, FileSpreadsheet } from 'lucide-react';
import {
    linhaBloqueia,
    obrigatoriasFaltando,
    podeExecutar,
    type FiltroProblemas,
    type LinhaComProblema,
    type ProblemaCelula,
    type ResultadoValidacao,
} from '../../../../services/importacao';
import { Loading } from '../../../../components/Loading';
import './step3.css';

interface PropsPasso3 {
    validacao: ResultadoValidacao | null;
    carregando: boolean;
    /** Filtro com que a lista atual foi carregada. */
    filtro: FiltroProblemas;
    aoConsultar: (pagina: number, filtro: FiltroProblemas) => void;
}

type Tom = 'neutro' | 'ok' | 'aviso' | 'erro';

interface PropsCardResumo {
    tom: Tom;
    icone: typeof CheckCircle2;
    valor: number;
    rotulo: string;
    /** Presente só nos cards que filtram a lista. */
    aoClicar?: () => void;
    ativo?: boolean;
    desabilitado?: boolean;
}

/** Zerado fica cinza: a cor só aparece quando há algo naquele grupo. */
function CardResumo({ tom, icone: Icone, valor, rotulo, aoClicar, ativo, desabilitado }: PropsCardResumo) {
    const classe = `resumo-card resumo-${valor > 0 ? tom : 'zerado'} ${ativo ? 'ativo' : ''}`;
    const conteudo = (
        <>
            <span className="resumo-icone">
                <Icone size={18} />
            </span>
            <span className="resumo-valor">{valor.toLocaleString('pt-BR')}</span>
            <span className="resumo-rotulo">{rotulo}</span>
        </>
    );

    if (!aoClicar) {
        return <div className={classe}>{conteudo}</div>;
    }
    return (
        <button
            type="button"
            className={`${classe} clicavel`}
            onClick={aoClicar}
            disabled={desabilitado}
            aria-pressed={ativo}
            title={ativo ? 'Mostrar todas as linhas com problema' : `Mostrar só as linhas ${rotulo}`}
        >
            {conteudo}
        </button>
    );
}

/** Uma barra só com a proporção de cada grupo: dá para ler o arquivo sem ler número. */
function BarraSaude({ ok, aviso, erro }: { ok: number; aviso: number; erro: number }) {
    const total = ok + aviso + erro;
    if (total === 0) return null;

    const percentualOk = Math.floor((ok / total) * 100);
    const segmentos = [
        { chave: 'ok', valor: ok },
        { chave: 'aviso', valor: aviso },
        { chave: 'erro', valor: erro },
    ].filter((s) => s.valor > 0);

    return (
        <div className="saude">
            <div className="saude-legenda">
                <span>Saúde do arquivo</span>
                <strong>{percentualOk}% das linhas sem problema</strong>
            </div>
            <div
                className="saude-barra"
                role="img"
                aria-label={`${ok} linhas sem problema, ${aviso} com aviso, ${erro} com erro`}
            >
                {segmentos.map((s) => (
                    // flex-grow proporcional; o min-width no CSS garante que 1 erro em 2000 ainda apareça
                    <span key={s.chave} className={`saude-segmento saude-${s.chave}`} style={{ flexGrow: s.valor }} />
                ))}
            </div>
        </div>
    );
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

export function Step3Validacao({ validacao, carregando, filtro, aoConsultar }: PropsPasso3) {
    if (!validacao) {
        return <Loading texto="Conferindo os dados" />;
    }

    const liberado = podeExecutar(validacao);
    const colunasFaltando = obrigatoriasFaltando(validacao);
    const arquivoSemDados = validacao.totalLinhas === 0;

    // linhasValidas inclui as com aviso (elas são importadas); "sem problema" é o resto.
    const comAviso = validacao.linhasComPendencia;
    const comErro = validacao.linhasInvalidas;
    const semProblema = validacao.linhasValidas - comAviso;

    // Clicar no filtro já ativo desfaz; qualquer troca de filtro volta para a página 1.
    function alternarFiltro(alvo: Exclude<FiltroProblemas, null>) {
        aoConsultar(0, filtro === alvo ? null : alvo);
    }

    // O backend já ordena (erros antes de pendências), filtra e pagina: aqui é só exibir.
    const linhas = validacao.problemas;
    const { numero, tamanho, totalElementos } = validacao.pagina;
    const totalPaginas = Math.ceil(totalElementos / tamanho);
    const primeira = numero * tamanho + 1;
    const ultima = numero * tamanho + linhas.length;

    const tituloLista =
        filtro === 'ERRO' ? 'Só linhas com erro' : filtro === 'PENDENCIA' ? 'Só linhas com aviso' : 'Linhas com problema';

    return (
        <div className="validacao-wrap">
            <div className="resumo-cards">
                <CardResumo
                    tom="neutro"
                    icone={FileSpreadsheet}
                    valor={validacao.totalLinhas}
                    rotulo="linhas no arquivo"
                />
                <CardResumo tom="ok" icone={CheckCircle2} valor={semProblema} rotulo="sem problema" />
                <CardResumo
                    tom="aviso"
                    icone={AlertTriangle}
                    valor={comAviso}
                    rotulo="com aviso"
                    aoClicar={comAviso > 0 ? () => alternarFiltro('PENDENCIA') : undefined}
                    ativo={filtro === 'PENDENCIA'}
                    desabilitado={carregando}
                />
                <CardResumo
                    tom="erro"
                    icone={AlertCircle}
                    valor={comErro}
                    rotulo="com erro"
                    aoClicar={comErro > 0 ? () => alternarFiltro('ERRO') : undefined}
                    ativo={filtro === 'ERRO'}
                    desabilitado={carregando}
                />
            </div>

            <BarraSaude ok={semProblema} aviso={comAviso} erro={comErro} />

            {liberado ? (
                <div className="validacao-alerta ok">
                    <CheckCircle2 size={18} />
                    <span>
                        Nenhum erro encontrado. O arquivo está pronto para ser importado.
                        {comAviso > 0 && (
                            <>
                                {' '}
                                {comAviso === 1 ? '1 linha tem aviso' : `${comAviso} linhas têm aviso`}, que não
                                impede a importação — os campos com aviso ficam vazios.
                            </>
                        )}
                    </span>
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
                        {tituloLista}
                        {totalPaginas > 1 && (
                            <span className="erro-limite">
                                mostrando {primeira}–{ultima} de {totalElementos}
                            </span>
                        )}
                        {filtro && (
                            <button
                                type="button"
                                className="erro-limpar-filtro"
                                onClick={() => aoConsultar(0, null)}
                                disabled={carregando}
                            >
                                Mostrar todas
                            </button>
                        )}
                    </h4>
                )}

                {linhas.length > 0 && (
                    <div className="erro-linhas-rolagem">
                        {linhas.map((linha) => (
                            <CardLinha key={linha.numeroLinha} linha={linha} />
                        ))}
                    </div>
                )}
            </div>

            {totalPaginas > 1 && (
                <div className="erro-paginacao">
                    <button
                        type="button"
                        onClick={() => aoConsultar(numero - 1, filtro)}
                        disabled={carregando || numero === 0}
                    >
                        <ChevronLeft size={16} />
                        Anterior
                    </button>
                    <span>
                        Página {numero + 1} de {totalPaginas}
                    </span>
                    <button
                        type="button"
                        onClick={() => aoConsultar(numero + 1, filtro)}
                        disabled={carregando || numero + 1 >= totalPaginas}
                    >
                        Próxima
                        <ChevronRight size={16} />
                    </button>
                </div>
            )}
        </div>
    );
}
