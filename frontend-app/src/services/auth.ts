const CHAVE_TOKEN = '@Logistica:token';
const CHAVE_NOME = '@Logistica:nome';
const CHAVE_PERFIL = '@Logistica:perfil';

export const PERFIL_ADMINISTRADOR = 'Administrador';

export interface UsuarioLogado {
    nome: string;
    perfil: string;
}

/**
 * Lê o payload do JWT (a parte do meio, entre os dois pontos).
 * O JWT usa Base64URL, que troca + por - e / por _. O atob só entende o
 * Base64 comum, então revertemos a troca antes de decodificar.
 */
function lerPayload(token: string): { exp?: number } | null {
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

/**
 * Único lugar que decide se existe uma sessão válida: o token existe, é legível
 * e ainda não expirou. Só serve para a interface; quem valida de verdade é o gateway.
 */

export function estaLogado(): boolean {
    const token = localStorage.getItem(CHAVE_TOKEN);
    if (!token) return false;

    const payload = lerPayload(token);
    return payload?.exp !== undefined && payload.exp > Date.now() / 1000;
}

export function usuarioLogado(): UsuarioLogado | null {
    if (!estaLogado()) return null;

    return {
        nome: localStorage.getItem(CHAVE_NOME) ?? '',
        perfil: localStorage.getItem(CHAVE_PERFIL) ?? '',
    };
}

/**
 * Serve só para a interface decidir o que desenhar. **Não é segurança:** o
 * localStorage é editável pelo próprio usuário. Quem de fato autoriza é o
 * gateway, que lê o perfil de dentro do token assinado a cada requisição.
 */
export function ehAdministrador(): boolean {
    const perfil = localStorage.getItem(CHAVE_PERFIL) ?? '';
    return perfil.toLowerCase() === PERFIL_ADMINISTRADOR.toLowerCase();
}

/**
 * Como o token é stateless (JWT), sair é apagar o que está guardado aqui — não
 * existe sessão no servidor para encerrar. O token continua tecnicamente válido
 * até expirar, em 2 horas.
 */
export function logout(): void {
    localStorage.removeItem(CHAVE_TOKEN);
    localStorage.removeItem(CHAVE_NOME);
    localStorage.removeItem(CHAVE_PERFIL);
}
