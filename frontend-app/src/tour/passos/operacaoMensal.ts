import type { MontarPassos } from './tipos';

const ROTA = '/operacao-mensal';

/**
 * Regras conferidas em pages/Frota/OperacaoMensal e, no backend,
 * ViagemRepository.motoristasPorMes e MotoristasMesService (dias disponíveis = dias do mês).
 */
export const passosOperacaoMensal: MontarPassos = () => [
    {
        id: 'operacao-cabecalho',
        rota: ROTA,
        alvo: 'cabecalho-pagina',
        titulo: '"Operação Mensal"',
        texto:
            'Uma linha por motorista e veículo no mês: quem dirigiu dois veículos aparece em duas linhas. ' +
            'Os filtros ficam ao lado do título.',
    },
    {
        id: 'operacao-busca',
        rota: ROTA,
        alvo: 'operacao-busca',
        lado: 'bottom',
        titulo: '"Buscar motorista ou placa"',
        texto: 'Filtra a tabela pelo nome do motorista ou pela placa enquanto você digita e volta para a página 1.',
    },
    {
        id: 'operacao-tipo',
        rota: ROTA,
        alvo: 'operacao-tipo',
        lado: 'bottom',
        titulo: '"Tipo de veículo"',
        texto:
            'Mostra só os veículos de um tipo. As opções vêm das categorias cadastradas na frota; "Todos" ' +
            'tira o filtro.',
    },
    {
        id: 'operacao-mes',
        rota: ROTA,
        alvo: 'operacao-mes',
        lado: 'bottom',
        titulo: '"Mês"',
        texto: 'Escolhe o mês exibido. Ao abrir a tela pelo menu, vem o mês atual.',
    },
    {
        id: 'operacao-exportar',
        rota: ROTA,
        alvo: 'exportar-csv',
        lado: 'bottom',
        titulo: '"Exportar CSV"',
        texto:
            'Baixa a planilha do mês com a busca e o tipo aplicados. Fica desativado enquanto a tabela carrega ' +
            'ou se a consulta falhar.',
    },
    {
        id: 'operacao-tabela',
        rota: ROTA,
        alvo: 'operacao-pagina',
        titulo: 'Tabela do mês',
        texto:
            'Clique no título de uma coluna para ordenar; clique de novo para inverter. A paginação fica abaixo ' +
            'da tabela, com o total de motoristas. "Viagens" conta os manifestos e "Dias em operação", as datas ' +
            'diferentes com viagem.',
    },
    {
        id: 'operacao-calculos',
        rota: ROTA,
        alvo: 'operacao-pagina',
        titulo: '"Disponibilidade", "Utilização" e "Efetividade"',
        texto:
            'O CSV não informa folgas, então os dias disponíveis são os dias do mês: "Disponibilidade" sai 100% ' +
            'e "Utilização" = dias em operação ÷ dias do mês × 100. "Efetividade" é a média do percentual de ' +
            'efetividade das viagens.',
    },
    {
        id: 'operacao-ficha',
        rota: ROTA,
        alvo: 'operacao-pagina',
        titulo: 'Ficha do motorista',
        texto:
            'As linhas desta tabela não abrem a ficha. A ficha cadastral abre pela tela "Motoristas", e o ' +
            'histórico do mês, por uma linha do "Ranking de Motoristas" no Dashboard.',
    },
];
