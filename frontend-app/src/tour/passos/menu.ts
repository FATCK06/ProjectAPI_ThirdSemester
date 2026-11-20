import type { MontarPassos, PassoTour } from './tipos';

export const passosMenu: MontarPassos = ({ gerenciarUsuarios, verStatus }) => {
    const passos: PassoTour[] = [
        {
            id: 'menu-lateral',
            alvo: 'menu-lateral',
            menu: 'expandido',
            lado: 'right',
            titulo: 'Menu lateral',
            texto:
                'Dá acesso a todas as telas do sistema. O item da tela aberta fica destacado em azul.',
        },
        {
            id: 'menu-recolher',
            alvo: 'menu-recolher',
            menu: 'expandido',
            lado: 'right',
            titulo: 'Recolher e expandir o menu',
            texto:
                'Este botão alterna o menu entre a versão completa e a versão só com ícones, liberando espaço ' +
                'para as tabelas. Com o menu recolhido, passe o mouse sobre um ícone para ver o nome do item. ' +
                'Em telas estreitas, o menu fica sempre só com ícones.',
        },
        {
            id: 'menu-dashboard',
            alvo: 'menu-dashboard',
            menu: 'expandido',
            lado: 'right',
            titulo: '"Dashboard"',
            texto:
                'Visão geral de um mês: indicadores da operação, gráficos de rentabilidade, comparativo entre ' +
                'meses importados e o ranking dos 5 motoristas com mais viagens.',
        },
        {
            id: 'menu-operacao',
            alvo: 'menu-operacao',
            menu: 'expandido',
            lado: 'right',
            titulo: '"Operação Mensal"',
            texto:
                'Tabela do mês com os números de cada motorista, com busca por motorista ou placa, filtro por ' +
                'tipo de veículo e exportação em CSV.',
        },
        {
            id: 'menu-motoristas',
            alvo: 'menu-motoristas',
            menu: 'expandido',
            lado: 'right',
            titulo: '"Motoristas"',
            texto:
                'Lista de fichas dos motoristas. Cada ficha traz os dados cadastrais e a comparação dos últimos ' +
                'três meses.',
        },
        {
            id: 'menu-ranking',
            alvo: 'menu-ranking',
            menu: 'expandido',
            lado: 'right',
            titulo: '"Ranking Mensal"',
            texto:
                'Todos os motoristas do mês em uma tabela ordenável por coluna, com exportação em CSV.',
        },
        {
            id: 'menu-importacao',
            alvo: 'menu-importacao',
            menu: 'importacao',
            lado: 'right',
            titulo: '"Importação de Dados"',
            texto:
                'Abre e fecha o grupo das telas de importação. É por aqui que os CSVs entram no sistema: ' +
                'sem importação, as outras telas ficam sem dados.',
        },
        {
            id: 'menu-manifestos',
            alvo: 'menu-manifestos',
            menu: 'importacao',
            lado: 'right',
            titulo: '"Manifestos"',
            texto: 'Importa o CSV de manifestos: as viagens do mês, com fretes e custos.',
        },
        {
            id: 'menu-veiculos',
            alvo: 'menu-veiculos',
            menu: 'importacao',
            lado: 'right',
            titulo: '"Veículos"',
            texto: 'Importa o CSV de veículos da frota, identificados pela placa.',
        },
        {
            id: 'menu-historico',
            alvo: 'menu-historico',
            menu: 'importacao',
            lado: 'right',
            titulo: '"Histórico de Importações"',
            texto: 'Lista as importações já feitas, com o status e o resultado de cada uma.',
        },
    ];

    if (gerenciarUsuarios) {
        passos.push({
            id: 'menu-usuarios',
            alvo: 'menu-usuarios',
            menu: 'expandido',
            lado: 'right',
            titulo: '"Cadastrar usuário"',
            texto: 'Cria o acesso de uma nova pessoa e define o perfil dela. Só aparece para o seu perfil.',
        });
    }

    if (verStatus) {
        passos.push({
            id: 'menu-status',
            alvo: 'menu-status',
            menu: 'expandido',
            lado: 'right',
            titulo: '"Status dos serviços"',
            texto: 'Mostra se cada serviço do sistema está no ar. Só aparece para o seu perfil.',
        });
    }

    passos.push(
        {
            id: 'menu-tutorial',
            alvo: 'menu-tutorial',
            menu: 'expandido',
            lado: 'right',
            titulo: '"Tutorial"',
            texto: 'Reabre este tutorial a qualquer momento, em qualquer tela.',
        },
        {
            id: 'menu-rodape',
            alvo: 'menu-rodape',
            menu: 'expandido',
            lado: 'right',
            alinhamento: 'end',
            titulo: 'Usuário logado e "Sair"',
            texto:
                'Mostra o nome e o perfil (Operador, Gestor ou Administrador) de quem está logado. Só Gestor e ' +
                'Administrador veem valores financeiros; só Administrador cadastra usuários e vê o status dos ' +
                'serviços. "Sair" apaga a sessão deste navegador e volta para o login.',
        },
    );

    return passos;
};
