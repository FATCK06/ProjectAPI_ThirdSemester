import axios, { type AxiosResponse } from 'axios';

export interface RespostaErroApi {
    erro?: string;
    campos?: Record<string, string>;
}

const NOMES_CAMPOS: Record<string, string> = {
    arquivo: 'Arquivo',
    email: 'E-mail',
    id: 'Identificador da importação',
    nome: 'Nome',
    ordenarPor: 'Ordenar por',
    pagina: 'Página',
    perfilAcesso: 'Perfil de acesso',
    senha: 'Senha',
    severidade: 'Tipo de problema',
    situacao: 'Situação',
    size: 'Tamanho da página',
    sort: 'Ordenação',
    tamanho: 'Quantidade por página',
};

export function obterRespostaErro(erro: unknown): AxiosResponse<RespostaErroApi> | undefined {
    return axios.isAxiosError<RespostaErroApi>(erro) ? erro.response : undefined;
}

export function formatarCamposErro(campos: Record<string, string>): string {
    return Object.entries(campos)
        .map(([campo, motivo]) => `${nomeDoCampo(campo)}: ${motivo}`)
        .join('; ');
}

function nomeDoCampo(campo: string): string {
    const nomeConhecido = NOMES_CAMPOS[campo];
    if (nomeConhecido) return nomeConhecido;

    const nomeLegivel = campo
        .replace(/([a-z0-9])([A-Z])/g, '$1 $2')
        .replace(/[._-]+/g, ' ')
        .trim();

    return nomeLegivel ? nomeLegivel[0].toLocaleUpperCase('pt-BR') + nomeLegivel.slice(1) : 'Campo';
}
