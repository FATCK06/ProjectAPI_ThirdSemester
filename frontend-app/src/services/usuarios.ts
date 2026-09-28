import api from './api';

export const PERFIS = ['Operador', 'Gestor', 'Administrador'] as const;

export type Perfil = (typeof PERFIS)[number];

export interface CriarUsuarioRequest {
    nome: string;
    email: string;
    senha: string;
    perfilAcesso: Perfil;
}

export interface UsuarioResponse {
    idUsuario: string;
    nome: string;
    email: string;
    perfilAcesso: Perfil;
    statusAtivo: boolean;
}

/**
 * A senha vai em texto na requisição e o backend aplica BCrypt antes de gravar.
 * Por isso criar usuário direto no Supabase não funciona: o login compara hash
 * com hash, e texto puro nunca bate.
 */
export async function criarUsuario(dados: CriarUsuarioRequest): Promise<UsuarioResponse> {
    const { data } = await api.post<UsuarioResponse>('/usuarios', dados);
    return data;
}

export function mensagemDeErro(erro: unknown): string {
    const resposta = (erro as { response?: { status?: number; data?: { erro?: string } } })?.response;

    // O backend manda { erro: "..." } em 400 e 409 — usa a mensagem dele quando vier.
    if (resposta?.data?.erro) return resposta.data.erro;
    if (resposta?.status === 409) return 'Já existe um usuário com esse e-mail.';
    if (resposta?.status) return `Falha no cadastro (erro ${resposta.status}).`;

    return 'Não foi possível falar com o servidor. Verifique se os serviços estão no ar.';
}
