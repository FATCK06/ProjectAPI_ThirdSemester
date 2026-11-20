import type { MontarPassos, PassoTour } from './tipos';

/** A rota /ranking usa pages/Frota/Ranking (pages/Ranking não está em AppRoutes). */
const ROTA = '/ranking';

export const passosRanking: MontarPassos = ({ financeiro }) => {
    const passos: PassoTour[] = [
        {
            id: 'ranking-cabecalho',
            rota: ROTA,
            alvo: 'cabecalho-pagina',
            titulo: '"Ranking Mensal"',
            texto: 'Todos os motoristas do mês em uma tabela, 20 por página, para comparar o desempenho.',
        },
        {
            id: 'ranking-mes',
            rota: ROTA,
            alvo: 'ranking-mes',
            lado: 'bottom',
            titulo: '"Mês de referência"',
            texto: 'Escolhe o mês do ranking. Ao abrir a tela pelo menu, vem o mês atual.',
        },
        {
            id: 'ranking-exportar',
            rota: ROTA,
            alvo: 'exportar-csv',
            lado: 'bottom',
            titulo: '"Exportar CSV"',
            texto:
                'Baixa o ranking do mês na mesma ordenação da tela. Fica desativado enquanto carrega ou quando o ' +
                'mês não tem dados.',
        },
        {
            id: 'ranking-ordenacao',
            rota: ROTA,
            alvo: 'ranking-pagina',
            titulo: 'Ordenação por coluna',
            texto:
                'Clique no título de uma coluna para ordenar; clique de novo para inverter. Ao abrir, a ordem é por ' +
                (financeiro
                    ? '"Rentabilidade", da maior para a menor.'
                    : '"Número de Viagens", do maior para o menor.'),
        },
        {
            id: 'ranking-operacionais',
            rota: ROTA,
            alvo: 'ranking-pagina',
            titulo: 'Colunas operacionais',
            texto:
                '"Motorista", "Tipo de veículo", "Número de Viagens" e "Utilização", que é dias em operação ÷ dias ' +
                'do mês × 100.',
        },
    ];

    passos.push(
        financeiro
            ? {
                  id: 'ranking-financeiras',
                  rota: ROTA,
                  alvo: 'ranking-pagina',
                  titulo: 'Colunas financeiras',
                  texto:
                      '"Valor dos fretes", "Custos", "Rentabilidade" (fretes − custos) e "Rentabilidade média" ' +
                      '(rentabilidade ÷ viagens com frete e custo). Se nenhum motorista tiver custo no mês, aparece ' +
                      '"Custos não informados para este período."',
              }
            : {
                  id: 'ranking-sem-financeiro',
                  rota: ROTA,
                  alvo: 'ranking-aviso-financeiro',
                  titulo: 'Dados financeiros',
                  texto:
                      'Seu perfil vê só as colunas operacionais. As colunas de fretes, custos e rentabilidade ' +
                      'aparecem para Gestor e Administrador.',
              },
    );

    return passos;
};
