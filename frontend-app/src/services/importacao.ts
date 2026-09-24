import api from './api';

/** Limite do backend: spring.servlet.multipart.max-file-size=10MB */
export const TAMANHO_MAXIMO_BYTES = 10 * 1024 * 1024;

export const EXTENSOES_ACEITAS = ['.csv'];

export type StatusImportacao =
    | 'AGUARDANDO'
    | 'VALIDADO'
    | 'EM_PROCESSAMENTO'
    | 'CONCLUIDO'
    | 'ERRO';

/** Resposta do upload. Nesse ponto o arquivo está guardado, mas nada foi gravado. */
export interface ImportacaoCriada {
    id: number;
    arquivoNome: string;
    status: StatusImportacao;
}

export interface Importacao {
    id: number;
    arquivoNome: string;
    dataImportacao: string;
    status: StatusImportacao;
    usuarioResponsavel: string | null;
    totalLinhas: number | null;
    linhasValidas: number | null;
    linhasInvalidas: number | null;
    /** Viagens inseridas; menor que totalLinhas quando o arquivo repete manifesto já importado. */
    linhasGravadas: number | null;
}

/** ERRO bloqueia a importação; PENDENCIA só avisa. */
export type Severidade = 'OK' | 'PENDENCIA' | 'ERRO';

export type EstadoCampo = 'OK' | 'VAZIO' | 'AUSENTE_COMO_ZERO' | 'NAO_CONVERTE';

/** Problema em uma célula: onde está, o que veio e o que era esperado. */
export interface ProblemaCelula {
    /** Nulo quando a falha não é de uma coluna específica. */
    campo: string | null;
    coluna: string | null;
    severidade: Severidade;
    estado: EstadoCampo;
    valorEncontrado: string | null;
    valorEsperado: string | null;
    mensagem: string;
}

export interface LinhaComProblema {
    numeroLinha: number;
    /** Células cruas, como vieram no CSV. */
    valores: Record<string, string | null>;
    problemas: ProblemaCelula[];
}

export interface PaginaProblemas {
    numero: number;
    tamanho: number;
    totalElementos: number;
}

export interface LinhaPreview {
    numeroLinha: number;
    manifesto: number | null;
    data: string | null;
    mesReferencia: string | null;
    motorista: string | null;
    cpf: string | null;
    agregado: string | null;
    veiculo: string | null;
    destino: string | null;
    valorFrete: number | null;
}

/** Uma coluna que o sistema espera, confrontada com o cabeçalho do arquivo. */
export interface MapeamentoColuna {
    campoSistema: string;
    obrigatoria: boolean;
    encontrada: boolean;
}

export interface ResultadoValidacao {
    importacaoId: number;
    arquivoNome: string;
    totalLinhas: number;
    linhasValidas: number;
    linhasInvalidas: number;
    /** Linhas com aviso que não impede a importação. */
    linhasComPendencia: number;
    /** Cabeçalho do arquivo, na ordem em que veio. */
    colunasDetectadas: string[];
    mapeamento: MapeamentoColuna[];
    pagina: PaginaProblemas;
    /** Só as linhas com problema (erro ou pendência), da página pedida. */
    problemas: LinhaComProblema[];
    amostra: LinhaPreview[];
}

export function obrigatoriasFaltando(validacao: ResultadoValidacao): string[] {
    return validacao.mapeamento.filter((m) => m.obrigatoria && !m.encontrada).map((m) => m.campoSistema);
}

/**
 * Regra do time: tudo ou nada. Uma linha inválida basta para recusar o arquivo —
 * dado parcial no banco é pior que importação adiada, porque depois ninguém sabe
 * quais manifestos ficaram de fora.
 */
export function podeExecutar(validacao: ResultadoValidacao): boolean {
    return (
        validacao.linhasInvalidas === 0 &&
        validacao.linhasValidas > 0 &&
        obrigatoriasFaltando(validacao).length === 0
    );
}

export function linhaBloqueia(linha: LinhaComProblema): boolean {
    return linha.problemas.some((p) => p.severidade === 'ERRO');
}

export interface ResumoImportacao {
    totalImportacoes: number;
    porStatus: Partial<Record<StatusImportacao, number>>;
}

/**
 * Sobe o arquivo e para por aí — o backend apenas guarda. Nenhuma viagem é
 * gravada até `executarImportacao`.
 */
export async function enviarArquivo(
    arquivo: File,
    aoProgredir?: (porcentagem: number) => void,
): Promise<ImportacaoCriada> {
    const formData = new FormData();
    formData.append('arquivo', arquivo);

    const { data } = await api.post<ImportacaoCriada>('/importacao/arquivos', formData, {
        onUploadProgress: (evento) => {
            if (!aoProgredir || !evento.total) return;
            aoProgredir(Math.round((evento.loaded * 100) / evento.total));
        },
    });

    return data;
}

/** Lê e confere o arquivo. Não grava nada — pode ser chamado quantas vezes precisar. */
export async function validarImportacao(id: number): Promise<ResultadoValidacao> {
    const { data } = await api.post<ResultadoValidacao>(`/importacao/arquivos/${id}/validar`);
    return data;
}

/** Grava as viagens. Única chamada do fluxo que escreve no banco. */
export async function executarImportacao(
    id: number,
): Promise<{ id: number; status: StatusImportacao; linhasGravadas: number }> {
    const { data } = await api.post(`/importacao/arquivos/${id}/executar`);
    return data;
}

export async function buscarImportacao(id: number): Promise<Importacao> {
    const { data } = await api.get<Importacao>(`/importacao/arquivos/${id}`);
    return data;
}

export async function buscarResumo(): Promise<ResumoImportacao> {
    const { data } = await api.get<ResumoImportacao>('/importacao/arquivos/resumo');
    return data;
}

export function mensagemDeErro(erro: unknown): string {
    const resposta = (erro as { response?: { status?: number; data?: { erro?: string } } })?.response;

    if (resposta?.data?.erro) return resposta.data.erro;
    if (resposta?.status === 401) return 'Sua sessão expirou. Faça login novamente.';
    if (resposta?.status === 409) return 'Esta importação já foi executada.';
    if (resposta?.status === 413) return 'Arquivo maior que o limite aceito pelo servidor.';
    if (resposta?.status) return `Falha na operação (erro ${resposta.status}).`;

    return 'Não foi possível falar com o servidor. Verifique se os serviços estão no ar.';
}

export function formatarTamanho(bytes: number): string {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}
