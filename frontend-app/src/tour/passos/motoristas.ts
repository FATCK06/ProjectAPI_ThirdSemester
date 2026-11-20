import type { MontarPassos } from './tipos';

const ROTA = '/motoristas';

/**
 * Regras conferidas em pages/Frota/Motoristas, pages/Frota/OperacaoMensal/FichaMotorista
 * (rota /motoristas/:id) e ComparativoMotorista. A ficha depende de um motorista
 * real: o tour só a descreve.
 */
export const passosMotoristas: MontarPassos = ({ financeiro }) => [
    {
        id: 'motoristas-pagina',
        rota: ROTA,
        alvo: 'motoristas-pagina',
        titulo: '"Motoristas"',
        texto: 'Lista os motoristas do mês de referência. Cada nome abre a ficha cadastral do motorista.',
    },
    {
        id: 'motoristas-busca',
        rota: ROTA,
        alvo: 'motoristas-busca',
        lado: 'bottom',
        titulo: '"Buscar motorista"',
        texto: 'Filtra pelo nome os motoristas da página que está na tela.',
    },
    {
        id: 'motoristas-mes',
        rota: ROTA,
        alvo: 'motoristas-mes',
        lado: 'bottom',
        titulo: '"Mês de referência"',
        texto: 'Troca o mês da lista e volta para a primeira página. Ao abrir uma ficha, o mês vai junto.',
    },
    {
        id: 'motoristas-lista',
        rota: ROTA,
        alvo: 'motoristas-pagina',
        titulo: 'Lista de fichas e paginação',
        texto:
            'Cada nome é um link para a ficha. Abaixo da lista, "Anterior" e "Próxima" trocam de página, com a ' +
            'contagem de motoristas da página e "Página X de Y".',
    },
    {
        id: 'motoristas-ficha',
        rota: ROTA,
        alvo: 'motoristas-pagina',
        titulo: 'Ficha cadastral',
        texto:
            'Traz o seletor "Período", "Dados do veículo no período" (placa e tipo), "Dados pessoais" (CPF, PIS e ' +
            'data de nascimento) e "Endereço". No fim, a "Comparação dos últimos três meses" mostra utilização e ' +
            'efetividade' +
            (financeiro
                ? ', fretes, custos, rentabilidade, rentabilidade média por viagem e margem.'
                : '; os valores financeiros aparecem só para Gestor e Administrador.'),
    },
];
