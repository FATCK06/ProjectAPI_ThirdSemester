import type { MontarPassos } from './tipos';

export const passosBoasVindas: MontarPassos = () => [
    {
        id: 'boas-vindas',
        titulo: 'Boas-vindas ao sistema',
        texto:
            'Este tutorial percorre as telas na ordem do menu lateral e mostra o que cada elemento faz, ' +
            'como os números são calculados e o que muda conforme o perfil. ' +
            'Ele só navega entre as telas: nada é clicado, enviado ou gravado.',
    },
    {
        id: 'boas-vindas-controles',
        titulo: 'Como navegar no tutorial',
        texto:
            'Use "Próximo" e "Anterior" ou as setas do teclado. "Pular tutorial", o X ou a tecla Esc ' +
            'encerram a qualquer momento. Enquanto o tutorial está aberto, o resto da tela fica bloqueado.',
    },
];
