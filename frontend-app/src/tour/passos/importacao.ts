import type { MontarPassos } from './tipos';

const MANIFESTOS = '/importacoes/manifestos';
const VEICULOS = '/importacoes/veiculos';
const HISTORICO = '/importacoes/historico';

/**
 * Regras conferidas em pages/Importacoes/* (ImportacaoWizard, Step1 a Step6,
 * ResultadoTratamento), services/importacao.ts (podeExecutar: tudo ou nada) e
 * StatusImportacaoEnum no backend. O tour não envia arquivo nem avança o assistente.
 */
export const passosImportacao: MontarPassos = () => [
    {
        id: 'manifestos-cabecalho',
        rota: MANIFESTOS,
        alvo: 'cabecalho-pagina',
        menu: 'importacao',
        titulo: '"Importação de Manifestos"',
        texto:
            'Traz o CSV de manifestos para o sistema: cada linha vira uma viagem, com motorista, veículo, data, ' +
            'frete e custo. É a base de todos os números das outras telas.',
    },
    {
        id: 'manifestos-upload',
        rota: MANIFESTOS,
        alvo: 'importacao-conteudo',
        titulo: 'Área de upload',
        texto:
            'Arraste o arquivo ou clique na área para escolher. Só arquivos .csv são aceitos. O tutorial não ' +
            'envia nada.',
    },
    {
        id: 'manifestos-passos',
        rota: MANIFESTOS,
        alvo: 'importacao-passos',
        lado: 'bottom',
        titulo: 'Os 6 passos',
        texto:
            '"Upload de Arquivo", "Mapeamento de Colunas", "Validação de Dados", "Revisão/Preview", "Confirmação e ' +
            'execução" e "Resultado". Cada um aparece como "Pendente", "Em progresso" ou "Concluído".',
    },
    {
        id: 'manifestos-mapeamento',
        rota: MANIFESTOS,
        alvo: 'importacao-passos',
        lado: 'bottom',
        titulo: '"Mapeamento de Colunas"',
        texto:
            'Liga cada coluna do CSV a um campo do sistema. O * marca as obrigatórias: faltando uma, não dá para ' +
            'seguir. Coluna opcional ausente só deixa o campo vazio.',
    },
    {
        id: 'manifestos-validacao',
        rota: MANIFESTOS,
        alvo: 'importacao-passos',
        lado: 'bottom',
        titulo: '"Validação de Dados"',
        texto:
            'Confere linha a linha e separa as linhas "sem problema", "com aviso" e "com erro". Linha com aviso é ' +
            'importada; uma única linha com erro bloqueia o arquivo inteiro, que precisa ser corrigido e enviado ' +
            'de novo.',
    },
    {
        id: 'manifestos-revisao',
        rota: MANIFESTOS,
        alvo: 'importacao-passos',
        lado: 'bottom',
        titulo: '"Revisão/Preview"',
        texto:
            'Mostra as primeiras linhas que serão importadas, para conferência. O "Mês" vem da data de cada linha: ' +
            'um arquivo trimestral gera viagens em meses diferentes.',
    },
    {
        id: 'manifestos-execucao',
        rota: MANIFESTOS,
        alvo: 'importacao-passos',
        lado: 'bottom',
        titulo: '"Confirmação e execução"',
        texto:
            'Só aqui algo é gravado. "Enviar os Dados" abre "Enviar os dados para o banco?" e, com "Sim, enviar", ' +
            'a gravação começa. Antes disso nada foi gravado. Só uma importação é gravada por vez.',
    },
    {
        id: 'manifestos-resultado',
        rota: MANIFESTOS,
        alvo: 'importacao-passos',
        lado: 'bottom',
        titulo: '"Resultado"',
        texto:
            'Mostra as viagens gravadas e as linhas lidas. Manifestos que já existiam são ignorados: reimportar o ' +
            'mesmo arquivo não duplica dados.',
    },
    {
        id: 'manifestos-rodape',
        rota: MANIFESTOS,
        alvo: 'importacao-rodape',
        lado: 'top',
        titulo: '"Anterior" e "Continuar"',
        texto:
            'Navegam entre os passos. "Continuar" fica travado até o passo atual estar resolvido (arquivo ' +
            'enviado, colunas obrigatórias presentes, nenhuma linha com erro); passe o mouse sobre ele para ver o ' +
            'motivo.',
    },
    {
        id: 'veiculos-cabecalho',
        rota: VEICULOS,
        alvo: 'cabecalho-pagina',
        menu: 'importacao',
        titulo: '"Importação de Veículos"',
        texto:
            'Os mesmos 6 passos, para o CSV da frota. Cada linha é um veículo, identificado pela placa, e o tipo do ' +
            'veículo alimenta os filtros e o gráfico por tipo.',
    },
    {
        id: 'veiculos-diferenca',
        rota: VEICULOS,
        alvo: 'importacao-passos',
        lado: 'bottom',
        titulo: 'Diferença para Manifestos',
        texto:
            'Na gravação, placas novas são criadas e as existentes são atualizadas. O "Resultado" traz o ' +
            '"Cruzamento com os manifestos": manifesto sem veículo na planilha aparece como "Não identificado", e ' +
            'veículo com tipo sem categoria, como "Não classificado".',
    },
    {
        id: 'historico-pagina',
        rota: HISTORICO,
        alvo: 'historico-pagina',
        menu: 'importacao',
        titulo: '"Histórico de Importações"',
        texto:
            'Lista cada envio com "Arquivo", "Tipo" (Manifestos ou Veículos), "Data", "Usuário" e "Status".',
    },
    {
        id: 'historico-status',
        rota: HISTORICO,
        alvo: 'historico-pagina',
        titulo: '"Status"',
        texto:
            'AGUARDANDO: arquivo recebido, nada lido. VALIDADO: conferido, nada gravado. EM_PROCESSAMENTO: gravando. ' +
            'CONCLUIDO: dados gravados. ERRO: falhou na leitura ou na gravação.',
    },
];
