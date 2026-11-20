import type { MontarPassos } from './tipos';

export const passosEncerramento: MontarPassos = () => [
    {
        id: 'encerramento',
        alvo: 'menu-tutorial',
        menu: 'expandido',
        lado: 'right',
        titulo: 'Fim do tutorial',
        texto:
            'Você viu todas as telas do seu perfil. Para refazer o tutorial quando quiser, clique em "Tutorial" ' +
            'no menu lateral, em qualquer tela.',
    },
];
