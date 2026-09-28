import { createContext } from 'react';

export type TipoToast = 'sucesso' | 'erro' | 'aviso' | 'info';

export interface OpcoesToast {
    titulo?: string;
    /** Em ms. 0 deixa o toast aberto até o usuário fechar. */
    duracao?: number;
}

export interface ApiToast {
    sucesso: (mensagem: string, opcoes?: OpcoesToast) => number;
    erro: (mensagem: string, opcoes?: OpcoesToast) => number;
    aviso: (mensagem: string, opcoes?: OpcoesToast) => number;
    info: (mensagem: string, opcoes?: OpcoesToast) => number;
    fechar: (id: number) => void;
}

export const ToastContext = createContext<ApiToast | null>(null);
