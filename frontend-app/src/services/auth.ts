const CHAVE_TOKEN = '@Logistica:token';
const CHAVE_NOME = '@Logistica:nome';
const CHAVE_PERFIL = '@Logistica:perfil';

export const PERFIL_ADMINISTRADOR = 'Administrador';

export interface UsuarioLogado {
    nome: string;
    perfil: string;
}

export function estaLogado(): boolean {
    return localStorage.getItem(CHAVE_TOKEN) !== null;
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
