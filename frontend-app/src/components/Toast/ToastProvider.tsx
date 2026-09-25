import { useCallback, useMemo, useRef, useState, type ReactNode } from 'react';
import { CheckCircle2, AlertCircle, AlertTriangle, Info, X } from 'lucide-react';
import { ToastContext, type ApiToast, type OpcoesToast, type TipoToast } from './ToastContext';
import './toast.css';

interface Toast {
    id: number;
    tipo: TipoToast;
    mensagem: string;
    titulo?: string;
    duracao: number;
    saindo: boolean;
}

/** Erro fica mais tempo: costuma ter algo para ler e agir. */
const DURACAO_PADRAO: Record<TipoToast, number> = {
    sucesso: 4000,
    info: 4000,
    aviso: 6000,
    erro: 7000,
};

const TITULO_PADRAO: Record<TipoToast, string> = {
    sucesso: 'Tudo certo',
    info: 'Informação',
    aviso: 'Atenção',
    erro: 'Algo deu errado',
};

const ICONE: Record<TipoToast, typeof Info> = {
    sucesso: CheckCircle2,
    info: Info,
    aviso: AlertTriangle,
    erro: AlertCircle,
};

/** Mais que isso vira parede de avisos; os mais antigos saem. */
const MAXIMO_VISIVEIS = 4;

/** Igual à duração da animação de saída em toast.css. */
const DURACAO_SAIDA = 200;

export function ToastProvider({ children }: { children: ReactNode }) {
    const [toasts, setToasts] = useState<Toast[]>([]);
    const proximoId = useRef(1);

    const remover = useCallback((id: number) => {
        setToasts((atuais) => atuais.filter((t) => t.id !== id));
    }, []);

    // Marca como saindo para animar, e só então tira da lista.
    const fechar = useCallback(
        (id: number) => {
            setToasts((atuais) => atuais.map((t) => (t.id === id ? { ...t, saindo: true } : t)));
            window.setTimeout(() => remover(id), DURACAO_SAIDA);
        },
        [remover],
    );

    const mostrar = useCallback((tipo: TipoToast, mensagem: string, opcoes?: OpcoesToast) => {
        const id = proximoId.current++;
        const novo: Toast = {
            id,
            tipo,
            mensagem,
            titulo: opcoes?.titulo ?? TITULO_PADRAO[tipo],
            duracao: opcoes?.duracao ?? DURACAO_PADRAO[tipo],
            saindo: false,
        };
        setToasts((atuais) => [...atuais, novo].slice(-MAXIMO_VISIVEIS));
        return id;
    }, []);

    const api = useMemo<ApiToast>(
        () => ({
            sucesso: (mensagem, opcoes) => mostrar('sucesso', mensagem, opcoes),
            erro: (mensagem, opcoes) => mostrar('erro', mensagem, opcoes),
            aviso: (mensagem, opcoes) => mostrar('aviso', mensagem, opcoes),
            info: (mensagem, opcoes) => mostrar('info', mensagem, opcoes),
            fechar,
        }),
        [mostrar, fechar],
    );

    return (
        <ToastContext.Provider value={api}>
            {children}
            <section className="toast-regiao" aria-label="Notificações">
                {toasts.map((toast) => (
                    <ItemToast key={toast.id} toast={toast} aoFechar={() => fechar(toast.id)} />
                ))}
            </section>
        </ToastContext.Provider>
    );
}

function ItemToast({ toast, aoFechar }: { toast: Toast; aoFechar: () => void }) {
    const Icone = ICONE[toast.tipo];
    const temTempo = toast.duracao > 0;

    return (
        <div
            className={`toast toast-${toast.tipo} ${toast.saindo ? 'saindo' : ''}`}
            // Erro interrompe o leitor de tela; o resto espera a vez.
            role={toast.tipo === 'erro' ? 'alert' : 'status'}
        >
            <span className="toast-icone">
                <Icone size={18} />
            </span>

            <div className="toast-texto">
                {toast.titulo && <strong className="toast-titulo">{toast.titulo}</strong>}
                <p className="toast-mensagem">{toast.mensagem}</p>
            </div>

            <button type="button" className="toast-fechar" onClick={aoFechar} aria-label="Fechar notificação">
                <X size={16} />
            </button>

            {/* A barra é o próprio timer: quando a animação termina o toast fecha,
                e o hover pausa as duas coisas juntas. */}
            {temTempo && (
                <span
                    className="toast-progresso"
                    style={{ animationDuration: `${toast.duracao}ms` }}
                    onAnimationEnd={aoFechar}
                />
            )}
        </div>
    );
}
