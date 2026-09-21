import api from './api';

/** Limite do backend: spring.servlet.multipart.max-file-size=10MB */
export const TAMANHO_MAXIMO_BYTES = 10 * 1024 * 1024;

export const EXTENSOES_ACEITAS = ['.csv'];

export type StatusImportacao = 'EM_PROCESSAMENTO' | 'CONCLUIDO' | 'ERRO';

/** Resposta do POST: o processamento do CSV é síncrono, então o status já vem final. */
export interface ImportacaoCriada {
    id: number;
    status: StatusImportacao;
}

export interface ArquivoImportacao {
    id: number;
    arquivoNome: string;
    dataImportacao: string;
    status: StatusImportacao;
    mesReferencia: string;
    usuarioResponsavel: string | null;
    /** Viagens realmente gravadas — menor que o total quando o arquivo repete manifesto já importado. */
    quantidadeLinhasLidas: number | null;
}

export interface ResumoImportacao {
    mesReferencia: string;
    totalImportacoes: number;
    porStatus: Partial<Record<StatusImportacao, number>>;
    /** Ausente para o perfil operador. */
    indicadoresFinanceiros?: Record<string, unknown>;
}

/**
 * @param mesReferencia rótulo do arquivo no formato "2026-09". Não define o mês das
 *   viagens: esse vem da coluna Data de cada linha, já que um arquivo cobre vários meses.
 */
export async function enviarManifesto(
    arquivo: File,
    mesReferencia: string,
    aoProgredir?: (porcentagem: number) => void,
): Promise<ImportacaoCriada> {
    const formData = new FormData();
    formData.append('arquivo', arquivo);
    formData.append('mesReferencia', mesReferencia);

    const { data } = await api.post<ImportacaoCriada>('/importacao/arquivos', formData, {
        onUploadProgress: (evento) => {
            if (!aoProgredir || !evento.total) return;
            aoProgredir(Math.round((evento.loaded * 100) / evento.total));
        },
    });

    return data;
}

export async function buscarImportacao(id: number): Promise<ArquivoImportacao> {
    const { data } = await api.get<ArquivoImportacao>(`/importacao/arquivos/${id}`);
    return data;
}

export async function buscarResumo(mesReferencia: string): Promise<ResumoImportacao> {
    const { data } = await api.get<ResumoImportacao>('/importacao/arquivos/resumo', {
        params: { mesReferencia },
    });
    return data;
}

export function formatarTamanho(bytes: number): string {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

/** Mês corrente como "2026-09", no fuso local (toISOString usaria UTC e erraria na virada). */
export function mesCorrente(): string {
    const hoje = new Date();
    return `${hoje.getFullYear()}-${String(hoje.getMonth() + 1).padStart(2, '0')}`;
}
