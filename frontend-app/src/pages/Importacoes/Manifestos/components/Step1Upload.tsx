import { useRef, useState } from 'react';
import { FileDown, FileText, Trash2, CheckCircle2, AlertCircle } from 'lucide-react';
import {
    enviarArquivo,
    formatarTamanho,
    mensagemDeErro,
    EXTENSOES_ACEITAS,
    TAMANHO_MAXIMO_BYTES,
    type ImportacaoCriada,
} from '../../../../services/importacao';
import { useToast } from '../../../../components/Toast';
import './step1.css';

type Situacao = 'ocioso' | 'enviando' | 'concluido' | 'erro';

interface PropsPasso1 {
    importacao: ImportacaoCriada | null;
    aoImportar: (importacao: ImportacaoCriada) => void;
    /** O arquivo foi descartado: o fluxo não pode seguir com a importação anterior. */
    aoRemover: () => void;
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

export function Step1Upload({ importacao, aoImportar, aoRemover }: PropsPasso1) {
    const [arquivo, setArquivo] = useState<File | null>(null);
    const [progresso, setProgresso] = useState(0);
    const [situacao, setSituacao] = useState<Situacao>(importacao ? 'concluido' : 'ocioso');
    const [erro, setErro] = useState<string | null>(null);
    const [arrastando, setArrastando] = useState(false);
    const inputRef = useRef<HTMLInputElement>(null);
    const toast = useToast();

    function selecionar(escolhido: File | undefined) {
        if (!escolhido) return;

        const problema = validar(escolhido);
        if (problema) {
            setErro(problema);
            setArquivo(null);
            return;
        }

        setArquivo(escolhido);
        // Envia já na seleção: o botão "Enviar arquivo" era um clique a mais sem
        // decisão nenhuma. Nada é gravado em viagens aqui, só o arquivo fica guardado.
        void enviar(escolhido);
    }

    function remover() {
        setArquivo(null);
        setErro(null);
        setProgresso(0);
        setSituacao('ocioso');
        if (inputRef.current) inputRef.current.value = '';
        aoRemover();
    }

    // Recebe o arquivo por parâmetro: chamado logo após setArquivo, o estado ainda não mudou.
    async function enviar(alvo: File) {
        setSituacao('enviando');
        setErro(null);
        setProgresso(0);

        try {
            const criada = await enviarArquivo(alvo, setProgresso);
            setSituacao('concluido');
            aoImportar(criada);
            toast.sucesso(`${alvo.name} recebido. Nada foi gravado ainda — avance para conferir os dados.`, {
                titulo: 'Arquivo enviado',
            });
        } catch (falha) {
            // Falha de envio é passageira (rede, servidor): toast. Arquivo inválido
            // continua no alerta fixo, porque o usuário precisa trocar o arquivo.
            setSituacao('erro');
            toast.erro(mensagemDeErro(falha), { titulo: 'Falha no envio do arquivo' });
        }
    }

    const enviando = situacao === 'enviando';
    const concluido = situacao === 'concluido';
    // Ao voltar do passo 2 o componente remonta sem o File, mas a importação existe:
    // o card continua aparecendo, com o nome que o servidor guardou.
    const nomeArquivo = arquivo?.name ?? importacao?.arquivoNome;

    return (
        <div className="upload-step-container">
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

            {nomeArquivo && (
                <div className="file-list-section">
                    <h4 className="file-list-title">Arquivo selecionado</h4>

                    <div className="file-card">
                        <div className="file-icon-bg">
                            <FileText size={20} color="#64748b" />
                        </div>

                        <div className="file-info">
                            <span className="file-name">{nomeArquivo}</span>

                            <div className="file-meta">
                                {arquivo && <span>{formatarTamanho(arquivo.size)}</span>}

                                {enviando && <span className="status-uploading">Enviando...</span>}
                                {concluido && (
                                    <span className="status-complete">
                                        <CheckCircle2 size={14} /> Recebido
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

                    {/* Só sobra botão para repetir um envio que falhou */}
                    {situacao === 'erro' && arquivo && (
                        <button className="btn-enviar" onClick={() => enviar(arquivo)}>
                            Tentar novamente
                        </button>
                    )}

                    {concluido && importacao && (
                        <p className="aviso-sucesso">
                            Arquivo #{importacao.id} recebido e aguardando. Nada foi gravado ainda —
                            avance para conferir os dados.
                        </p>
                    )}
                </div>
            )}
        </div>
    );
}
