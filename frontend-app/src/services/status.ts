import api from './api';

export type EstadoServico = 'UP' | 'DOWN';

export interface ServicoStatus {
    servico: string;
    status: EstadoServico;
    tempoMs: number;
    /** Só vem quando o serviço está DOWN. */
    detalhe?: string;
}

export interface StatusGeral {
    geral: EstadoServico;
    servicos: ServicoStatus[];
}

/**
 * Situação dos serviços por trás do gateway. Restrito ao perfil Administrador —
 * quem não for recebe 403 do próprio gateway.
 */
export async function buscarStatus(): Promise<StatusGeral> {
    const { data } = await api.get<StatusGeral>('/status');
    return data;
}

export function mensagemDeErro(erro: unknown): string {
    const status = (erro as { response?: { status?: number } })?.response?.status;

    if (status === 403) return 'Esta página é restrita ao perfil Administrador.';
    if (status === 401) return 'Sua sessão expirou. Faça login novamente.';
    if (status) return `Falha ao consultar o status (erro ${status}).`;

    // Sem status = o próprio gateway não respondeu.
    return 'O gateway não respondeu. Provavelmente está fora do ar.';
}
