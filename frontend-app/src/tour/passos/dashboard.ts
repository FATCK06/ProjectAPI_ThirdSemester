import type { MontarPassos, PassoTour } from './tipos';

const ROTA = '/dashboard';

/**
 * Regras conferidas em IndicadoresCard/selecao.ts, ComparativoMensal/*,
 * RankingMotoristas e, no backend, CalculoIndicadores/IndicadoresService/ViagemRepository.
 */
export const passosDashboard: MontarPassos = ({ financeiro }) => {
    const passos: PassoTour[] = [
        {
            id: 'dashboard-mes',
            rota: ROTA,
            alvo: 'dashboard-mes',
            lado: 'bottom',
            titulo: 'Mês de referência',
            texto:
                '"Mês atual" e "Mês anterior" trocam o mês com um clique; "Selecionar mês" abre o calendário para ' +
                'qualquer mês até o atual. O mês escolhido vale para os indicadores, os gráficos e o ranking. ' +
                'O "Comparativo mensal" tem seleção própria.',
        },
        {
            id: 'dashboard-indicadores',
            rota: ROTA,
            alvo: 'dashboard-indicadores',
            titulo: 'Indicadores do mês',
            texto:
                'O card "Indicadores de" + mês soma todos os motoristas. "Nº de viagens" conta as viagens importadas ' +
                'no mês. "Dias em operação" soma, para cada motorista, as datas diferentes em que ele teve manifesto.',
        },
    ];

    if (financeiro) {
        passos.push({
            id: 'dashboard-indicadores-financeiros',
            rota: ROTA,
            alvo: 'dashboard-indicadores',
            titulo: 'Indicadores financeiros',
            texto:
                '"Valor total dos fretes" soma a coluna Valor Fretes do CSV e "Custo total" soma o Vale frete. ' +
                '"Rentabilidade total" = fretes − custo; "Rentabilidade média/viagem" = rentabilidade ÷ viagens com ' +
                'frete e custo; "Margem" = rentabilidade ÷ fretes × 100.',
        });
    }

    passos.push(
        {
            id: 'dashboard-indicadores-pendentes',
            rota: ROTA,
            alvo: 'dashboard-indicadores',
            titulo: '"Dias disponíveis" e "% de utilização"',
            texto:
                'Em desenvolvimento: o CSV de manifestos não informa férias, afastamentos nem manutenção, então os ' +
                'dois aparecem como "-". A utilização será dias em operação ÷ dias disponíveis × 100.',
        },
        {
            id: 'dashboard-filtros',
            rota: ROTA,
            alvo: 'dashboard-indicadores',
            titulo: 'Escolher os indicadores',
            texto: financeiro
                ? 'O botão "Filtros" lista os indicadores por categoria (Operacional e Financeiro) para marcar quais ' +
                  'aparecem; o número no botão é quantos estão visíveis. A escolha se mantém ao trocar de mês.'
                : 'O botão "Filtros" lista os indicadores operacionais para marcar quais aparecem; o número no botão ' +
                  'é quantos estão visíveis. A escolha se mantém ao trocar de mês.',
        },
        {
            id: 'dashboard-comparar',
            rota: ROTA,
            alvo: 'dashboard-indicadores',
            titulo: '"Comparar com"',
            texto:
                'Compara o mês exibido com o "Mês anterior" ou com a média dos "Últimos 2 meses" ou "Últimos 3 meses". ' +
                'Só ficam liberados os períodos em que todos os meses têm viagens importadas. Com 2 ou 3 meses, cada ' +
                'indicador ganha uma linha de tendência.',
        },
        {
            id: 'dashboard-variacao',
            rota: ROTA,
            alvo: 'dashboard-indicadores',
            titulo: 'Seta de variação',
            texto: financeiro
                ? 'Mostra a variação percentual contra o período escolhido: verde quando melhora, vermelha quando ' +
                  'piora. Em "Custo total", subir é piora. Com a base igual a zero, aparece só o valor de referência.'
                : 'Mostra a variação percentual contra o período escolhido: verde quando melhora, vermelha quando ' +
                  'piora. Com a base igual a zero, aparece só o valor de referência.',
        },
        {
            id: 'dashboard-asterisco',
            rota: ROTA,
            alvo: 'dashboard-indicadores',
            titulo: 'Asterisco e "-"',
            texto: financeiro
                ? 'O * marca um valor aproximado; passe o mouse sobre ele para ver o motivo. Em "Dias em operação", ' +
                  'viagem de vários dias conta 1 dia. Nos financeiros, ele aparece quando alguma viagem veio sem frete ' +
                  'ou custo no CSV e ficou fora das somas. O "-" indica que não há dado para calcular.'
                : 'O * marca um valor aproximado; passe o mouse sobre ele para ver o motivo. Em "Dias em operação", ' +
                  'viagem de vários dias conta 1 dia, pois a coluna Diária do CSV vem vazia. O "-" indica que não há ' +
                  'dado para calcular.',
        },
    );

    if (financeiro) {
        passos.push(
            {
                id: 'dashboard-grafico-motoristas',
                rota: ROTA,
                alvo: 'dashboard-grafico-motoristas',
                titulo: '"Rentabilidade por Motorista"',
                texto:
                    'Os 10 motoristas mais rentáveis do mês (fretes − custo de cada um). Passe o mouse sobre a barra ' +
                    'para ver o nome completo e o valor.',
            },
            {
                id: 'dashboard-grafico-tipos',
                rota: ROTA,
                alvo: 'dashboard-grafico-tipos',
                titulo: '"Rentabilidade por Tipo de Veículo"',
                texto:
                    'Soma a rentabilidade do mês por tipo de veículo, do mais ao menos rentável. Manifesto sem veículo ' +
                    'na frota entra em "Não identificado"; veículo com tipo sem categoria, em "Não classificado".',
            },
        );
    } else {
        passos.push({
            id: 'dashboard-sem-financeiro',
            rota: ROTA,
            alvo: 'dashboard-aviso-financeiro',
            titulo: 'Dados financeiros',
            texto:
                'Seu perfil não vê valores financeiros: fretes, custos, rentabilidade e margem não aparecem nos ' +
                'indicadores, gráficos nem tabelas, e este aviso ocupa o lugar dos gráficos. Gestor e Administrador ' +
                'veem esses dados.',
        });
    }

    passos.push(
        {
            id: 'dashboard-comparativo',
            rota: ROTA,
            alvo: 'dashboard-comparativo',
            titulo: '"Comparativo mensal"',
            texto:
                'Compara os totais de todos os motoristas entre os meses já importados. Cada botão é um mês: marque ' +
                'de 2 a 12. Ao abrir, vêm marcados os dois meses mais recentes.',
        },
        {
            id: 'dashboard-comparativo-cards',
            rota: ROTA,
            alvo: 'dashboard-comparativo',
            titulo: 'Cards do comparativo',
            texto:
                'Mostram o mês mais recente da seleção contra o mês anterior, com seta de variação. Se o anterior vale ' +
                '0, aparece só a diferença, sem percentual. Mês sem importação mostra "Sem dados para MM/AAAA."',
        },
        {
            id: 'dashboard-comparativo-graficos',
            rota: ROTA,
            alvo: 'dashboard-comparativo',
            titulo: 'Gráficos de evolução',
            texto: financeiro
                ? 'Linhas mês a mês dos meses marcados: "Nº de viagens", "% de utilização" e "Valores (R$)", com ' +
                  'fretes, custos e rentabilidade.'
                : 'Linhas mês a mês dos meses marcados: "Nº de viagens" e "% de utilização". O gráfico de valores ' +
                  'fica para Gestor e Administrador.',
        },
        {
            id: 'dashboard-ranking',
            rota: ROTA,
            alvo: 'dashboard-ranking',
            titulo: '"Ranking de Motoristas"',
            texto:
                'Os 5 motoristas com mais viagens no mês de referência. Clique em uma linha para abrir o histórico do ' +
                'motorista naquele mês. "Disponibilidade" e "Utilização" mostram "-" até os dias disponíveis serem ' +
                'informados.' +
                (financeiro ? ' As colunas de fretes, custos e rentabilidade completam cada linha.' : ''),
        },
    );

    return passos;
};
