const CHAVE_TOKEN = '@Logistica:token';
const CHAVE_NOME = '@Logistica:nome';
/** Não é mais gravada (o perfil vem do token); só limpamos a de logins antigos. */
const CHAVE_PERFIL_LEGADA = '@Logistica:perfil';

/** Códigos que o backend usa no token e na API. */
export const PERFIS = ['OPERADOR', 'GESTOR', 'ADMINISTRADOR'] as const;

export type Perfil = (typeof PERFIS)[number];

const ROTULO_PERFIL: Record<Perfil, string> = {
    OPERADOR: 'Operador',
    GESTOR: 'Gestor',
    ADMINISTRADOR: 'Administrador',
};

/**
 * Espelho das permissões do backend (Permissao.java no gateway e no ms-operacoes).
 * A tela pergunta pela permissão, nunca pelo perfil: um perfil novo muda só aqui.
 */
export const PERMISSOES = {
    GERENCIAR_USUARIOS: ['ADMINISTRADOR'],
    VER_STATUS_SERVICO: ['ADMINISTRADOR'],
    VER_DADOS_FINANCEIROS: ['ADMINISTRADOR', 'GESTOR'],
} as const satisfies Record<string, readonly Perfil[]>;

export type Permissao = keyof typeof PERMISSOES;

export interface UsuarioLogado {
    nome: string;
    perfil: Perfil | null;
}

/** O que existe dentro do token. */
interface Payload {
    exp?: number;
    perfil?: string;
    sub?: string;
}

/**
 * Lê o payload do JWT (a parte do meio, entre os dois pontos).
 * O JWT usa Base64URL, que troca + por - e / por _. O atob só entende o
 * Base64 comum, então revertemos a troca antes de decodificar.
 */
function lerPayload(token: string): Payload | null {
    try {
        const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
        const json = decodeURIComponent(
            atob(base64)
                .split('')
                .map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
                .join(''),
        );
        return JSON.parse(json);
    } catch {
        return null;
    }
}

/** Existe token, é legível e não expirou. Só para a interface; quem valida é o gateway. */
export function estaLogado(): boolean {
    const token = localStorage.getItem(CHAVE_TOKEN);
    if (!token) return false;

    const payload = lerPayload(token);
    return payload?.exp !== undefined && payload.exp > Date.now() / 1000;
}

/**
 * Perfil lido de dentro do token. null se não houver login válido ou se o perfil
 * for desconhecido. Aceita qualquer caixa: tokens antigos trazem "Administrador".
 */
export function perfilAtual(): Perfil | null {
    if (!estaLogado()) return null;
    const perfil = lerPayload(localStorage.getItem(CHAVE_TOKEN)!)?.perfil?.trim().toUpperCase();
    return PERFIS.find((p) => p === perfil) ?? null;
}

/** O usuário logado tem a permissão? Só decide o que desenhar: quem barra é o gateway. */
export function temPermissao(permissao: Permissao): boolean {
    const perfil = perfilAtual();
    return perfil !== null && (PERMISSOES[permissao] as readonly Perfil[]).includes(perfil);
}

/** "ADMINISTRADOR" → "Administrador". Valor desconhecido volta como veio. */
export function rotuloPerfil(perfil: string | null | undefined): string {
    if (!perfil) return '';
    return ROTULO_PERFIL[perfil.toUpperCase() as Perfil] ?? perfil;
}

export function usuarioLogado(): UsuarioLogado | null {
    if (!estaLogado()) return null;

    return {
        nome: localStorage.getItem(CHAVE_NOME) ?? '',
        perfil: perfilAtual(),
    };
}

/** Atalho para temPermissao('VER_DADOS_FINANCEIROS'): a regra continua em PERMISSOES. */
export function podeVerDadosFinanceiros(): boolean {
    return temPermissao('VER_DADOS_FINANCEIROS');
}

<<<<<<< HEAD
/** JWT é stateless: sair é apagar o que está guardado aqui. */
=======
/**
 * Espelha o PerfilResolver do ms-operacoes: gestor e administrador veem frete,
 * custo e rentabilidade; perfil ausente ou desconhecido fica de fora. Também só
 * decide o que desenhar: para os outros perfis a API já não manda esses valores.
 */
export function podeVerDadosFinanceiros(): boolean {
    const perfil = (localStorage.getItem(CHAVE_PERFIL) ?? '').toLowerCase();
    return perfil === 'gestor' || perfil === PERFIL_ADMINISTRADOR.toLowerCase();
}

/**
 * Como o token é stateless (JWT), sair é apagar o que está guardado aqui. Não
 * existe sessão no servidor para encerrar. O token continua tecnicamente válido
 * até expirar, em 2 horas.
 */
>>>>>>> origin/develop
export function logout(): void {
    localStorage.removeItem(CHAVE_TOKEN);
    localStorage.removeItem(CHAVE_NOME);
    localStorage.removeItem(CHAVE_PERFIL_LEGADA);
}
