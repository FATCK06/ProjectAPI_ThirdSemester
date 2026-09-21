import { useRef, useState } from 'react';
import { FileDown, FileText, Trash2, CheckCircle2, AlertCircle } from 'lucide-react';
import {
    enviarManifesto,
    formatarTamanho,
    mesCorrente,
    EXTENSOES_ACEITAS,
    TAMANHO_MAXIMO_BYTES,
    type ImportacaoCriada,
} from '../../../../services/importacao';
import './step1.css';

type Situacao = 'ocioso' | 'enviando' | 'concluido' | 'erro';

interface PropsPasso1 {
    importacao: ImportacaoCriada | null;
    aoImportar: (importacao: ImportacaoCriada) => void;
}

function validar(arquivo: File): string | null {
    const nome = arquivo.name.toLowerCase();

    if (!EXTENSOES_ACEITAS.some((ext) => nome.endsWith(ext))) {
        return 'Apenas arquivos .csv são aceitos.';
    }
    if (arquivo.size === 0) {
        return 'O arquivo está vazio.';
    }
    if (arquivo.size > TAMANHO_MAXIMO_BYTES) {
        return `Arquivo de ${formatarTamanho(arquivo.size)} excede o limite de ${formatarTamanho(TAMANHO_MAXIMO_BYTES)}.`;
    }

    return null;
}

function mensagemDeErro(erro: unknown): string {
    const status = (erro as { response?: { status?: number } })?.response?.status;

    if (status === 400) return 'O servidor recusou o arquivo. Confira a extensão e o mês de referência.';
    if (status === 401 || status === 403) return 'Sua sessão expirou. Faça login novamente.';
    if (status === 413) return 'Arquivo maior que o limite aceito pelo servidor.';
    if (status) return `Falha no envio (erro ${status}).`;

    return 'Não foi possível falar com o servidor. Verifique se os serviços estão no ar.';
}

export function Step1Upload({ importacao, aoImportar }: PropsPasso1) {
    const [arquivo, setArquivo] = useState<File | null>(null);
    const [mesReferencia, setMesReferencia] = useState(mesCorrente);
    const [progresso, setProgresso] = useState(0);
    const [situacao, setSituacao] = useState<Situacao>(importacao ? 'concluido' : 'ocioso');
    const [erro, setErro] = useState<string | null>(null);
    const [arrastando, setArrastando] = useState(false);
    const inputRef = useRef<HTMLInputElement>(null);

    function selecionar(escolhido: File | undefined) {
        if (!escolhido) return;

        const problema = validar(escolhido);
        if (problema) {
            setErro(problema);
            setArquivo(null);
            return;
        }

        setErro(null);
        setArquivo(escolhido);
        setSituacao('ocioso');
        setProgresso(0);
    }

    function remover() {
        setArquivo(null);
        setErro(null);
        setProgresso(0);
        setSituacao('ocioso');
        if (inputRef.current) inputRef.current.value = '';
    }

    async function enviar() {
        if (!arquivo) return;

        setSituacao('enviando');
        setErro(null);
        setProgresso(0);

        try {
            const criada = await enviarManifesto(arquivo, mesReferencia, setProgresso);

            // O processamento do CSV é síncrono: o status já chega final. ERRO aqui
            // significa que o arquivo subiu mas o parsing falhou — não é erro de rede.
            if (criada.status === 'ERRO') {
                setSituacao('erro');
                setErro('O arquivo foi recebido, mas o processamento falhou. Confira o conteúdo e as colunas.');
                return;
            }

            setSituacao('concluido');
            aoImportar(criada);
        } catch (falha) {
            setSituacao('erro');
            setErro(mensagemDeErro(falha));
        }
    }

    const enviando = situacao === 'enviando';
    const concluido = situacao === 'concluido';

    return (
        <div className="upload-step-container">
            <div className="campo-mes">
                <label htmlFor="mes-referencia">Mês de referência</label>
                <input
                    id="mes-referencia"
                    type="month"
                    value={mesReferencia}
                    onChange={(e) => setMesReferencia(e.target.value)}
                    disabled={enviando || concluido}
                />
                <span className="campo-ajuda">
                    Identifica o arquivo. O mês de cada viagem vem da coluna Data.
                </span>
            </div>

            <input
                ref={inputRef}
                type="file"
                accept=".csv,text/csv"
                hidden
                onChange={(e) => selecionar(e.target.files?.[0])}
            />

            <div
                className={`dropzone ${arrastando ? 'arrastando' : ''}`}
                onClick={() => !enviando && !concluido && inputRef.current?.click()}
                onDragOver={(e) => {
                    e.preventDefault();
                    setArrastando(true);
                }}
                onDragLeave={() => setArrastando(false)}
                onDrop={(e) => {
                    e.preventDefault();
                    setArrastando(false);
                    if (enviando || concluido) return;
                    selecionar(e.dataTransfer.files?.[0]);
                }}
            >
                <div className="dropzone-icon">
                    <FileDown size={32} color="#ffffff" />
                </div>
                <h3>{arrastando ? 'Solte o arquivo aqui' : 'Arraste seu arquivo ou clique aqui'}</h3>
                <p>Escolha seu arquivo</p>
            </div>

            <div className="dropzone-footer">
                <span>Apenas arquivos .csv.</span>
                <span>Limite de {formatarTamanho(TAMANHO_MAXIMO_BYTES)}</span>
            </div>

            {erro && (
                <div className="alerta-erro">
                    <AlertCircle size={18} />
                    <span>{erro}</span>
                </div>
            )}

            {arquivo && (
                <div className="file-list-section">
                    <h4 className="file-list-title">Arquivo selecionado</h4>

                    <div className="file-card">
                        <div className="file-icon-bg">
                            <FileText size={20} color="#64748b" />
                        </div>

                        <div className="file-info">
                            <span className="file-name">{arquivo.name}</span>

                            <div className="file-meta">
                                <span>{formatarTamanho(arquivo.size)}</span>

                                {enviando && <span className="status-uploading">Enviando...</span>}
                                {concluido && (
                                    <span className="status-complete">
                                        <CheckCircle2 size={14} /> Importado
                                    </span>
                                )}
                                {situacao === 'erro' && <span className="status-erro">Falhou</span>}

                                {enviando && <span className="progress-text">{progresso}%</span>}
                            </div>

                            {enviando && (
                                <div className="progress-bar-bg">
                                    <div className="progress-bar-fill" style={{ width: `${progresso}%` }} />
                                </div>
                            )}
                        </div>

                        <button className="btn-delete" onClick={remover} disabled={enviando} title="Remover">
                            <Trash2 size={20} />
                        </button>
                    </div>

                    {!concluido && (
                        <button className="btn-enviar" onClick={enviar} disabled={enviando}>
                            {enviando ? 'Enviando...' : 'Enviar arquivo'}
                        </button>
                    )}

                    {concluido && importacao && (
                        <p className="aviso-sucesso">
                            Importação #{importacao.id} concluída. Avance para o mapeamento.
                        </p>
                    )}
                </div>
            )}
        </div>
    );
}
