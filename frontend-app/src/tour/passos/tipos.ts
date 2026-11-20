import type { Alignment, Side } from 'driver.js';

/**
 * O que o usuário logado pode ver. Vem de temPermissao/podeVerDadosFinanceiros:
 * os passos perguntam pela permissão, nunca pelo perfil.
 */
export interface PermissoesTour {
    financeiro: boolean;
    gerenciarUsuarios: boolean;
    verStatus: boolean;
}

/** Como o menu lateral precisa estar para o passo: o tour só expande e abre, nunca clica. */
export type PreparoMenu = 'expandido' | 'importacao';

export interface PassoTour {
    id: string;
    /** Rota em que o passo acontece; o tour navega até ela antes de destacar. */
    rota?: string;
    /** Valor do atributo data-tour do alvo. Ausente: balão centralizado, sem destaque. */
    alvo?: string;
    titulo: string;
    /** Texto fixo (o driver.js monta o balão com innerHTML). */
    texto: string;
    lado?: Side;
    alinhamento?: Alignment;
    menu?: PreparoMenu;
}

/** Cada tela exporta uma função assim: recebe as permissões e devolve os passos dela. */
export type MontarPassos = (permissoes: PermissoesTour) => PassoTour[];
