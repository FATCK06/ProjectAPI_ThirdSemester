// Motor do tour sobre o driver.js. O driver.js desenha o escurecimento, o
// destaque e o balão; aqui fica o que ele não sabe fazer sozinho: trocar de
// rota, preparar o menu lateral e esperar o alvo da nova tela aparecer.
//
// O tour é somente leitura: só navega, expande o menu e abre o acordeão.
// Nunca clica, envia formulário ou chama a API.

import { driver, type Driver, type DriveStep, type PopoverDOM } from 'driver.js';
import 'driver.js/dist/driver.css';
import './tour.css';
import type { PassoTour } from './passos';

/** Quanto esperar o alvo depois de navegar; passado isso, o passo vai centralizado. */
const ESPERA_MAXIMA_MS = 5000;
/** Duração das transições do menu (largura 0,3 s, acordeão 0,25 s), com folga. */
const ESPERA_TRANSICAO_MS = 350;

const CONFIG_BALAO = {
    popoverClass: 'tour-balao',
    overlayColor: '#0f172a',
    overlayOpacity: 0.6,
    stagePadding: 6,
    stageRadius: 10,
    smoothScroll: true,
    allowClose: true,
    allowKeyboardControl: true,
    // Clique fora do destaque não faz nada; dentro dele, também não (somente leitura)
    overlayClickBehavior: 'none' as const,
    disableActiveInteraction: true,
    closeBtnLabel: 'Fechar tutorial',
};

export interface ControleTour {
    rotaAtual: () => string;
    navegar: (rota: string) => void;
    expandirMenu: () => void;
    abrirImportacao: () => void;
}

export interface Tour {
    iniciar: () => void;
    encerrar: () => void;
}

function visivel(elemento: Element): boolean {
    const caixa = elemento.getBoundingClientRect();
    return caixa.width > 0 && caixa.height > 0;
}

/** O primeiro elemento visível com o data-tour (no celular, parte do menu fica oculta). */
function encontrarAlvo(alvo: string): Element | undefined {
    return Array.from(document.querySelectorAll(`[data-tour="${alvo}"]`)).find(visivel);
}

/** Resolve quando o alvo aparece, quando o tempo acaba ou quando o tour é cancelado. */
function esperarAlvo(alvo: string | undefined, cancelado: () => boolean): Promise<void> {
    return new Promise((resolve) => {
        if (!alvo || encontrarAlvo(alvo)) {
            resolve();
            return;
        }
        let observador: MutationObserver | undefined;
        let intervalo: number | undefined;
        let limite: number | undefined;
        const terminar = () => {
            observador?.disconnect();
            window.clearInterval(intervalo);
            window.clearTimeout(limite);
            resolve();
        };
        const conferir = () => {
            if (cancelado() || encontrarAlvo(alvo)) terminar();
        };
        observador = new MutationObserver(conferir);
        observador.observe(document.body, { childList: true, subtree: true, attributes: true });
        // Transição de CSS não muda o DOM: o intervalo pega o alvo que só ficou visível
        intervalo = window.setInterval(conferir, 150);
        limite = window.setTimeout(terminar, ESPERA_MAXIMA_MS);
    });
}

function botao(texto: string, classe: string, aoClicar: () => void): HTMLButtonElement {
    const elemento = document.createElement('button');
    elemento.type = 'button';
    elemento.className = `driver-popover-footer-btn ${classe}`;
    elemento.textContent = texto;
    elemento.addEventListener('click', aoClicar);
    return elemento;
}

/** Balão enquanto a próxima tela carrega: botões travados e o aviso no lugar do contador. */
function mostrarCarregando(popover: PopoverDOM | undefined) {
    if (!popover) return;
    popover.wrapper.classList.add('tour-carregando');
    popover.wrapper.setAttribute('aria-busy', 'true');
    popover.progress.textContent = 'Carregando a tela...';
    popover.progress.style.display = 'block';
    popover.wrapper.querySelectorAll('button').forEach((b) => (b.disabled = true));
}

export function criarTour(passos: PassoTour[], controle: ControleTour, aoEncerrar: () => void): Tour {
    let ocupado = false;
    let encerrado = false;
    let transicao: number | undefined;

    const passosDriver: DriveStep[] = passos.map((passo) => ({
        // Sem alvo (ou alvo que não apareceu): o driver.js centraliza o balão sem destaque
        element: passo.alvo
            ? ((() => encontrarAlvo(passo.alvo!)) as () => Element)
            : undefined,
        popover: {
            title: passo.titulo,
            description: passo.texto,
            side: passo.lado,
            align: passo.alinhamento,
        },
    }));

    const instancia: Driver = driver({
        ...CONFIG_BALAO,
        steps: passosDriver,
        showProgress: true,
        progressText: '{{current}} de {{total}}',
        nextBtnText: 'Próximo',
        prevBtnText: 'Anterior',
        doneBtnText: 'Concluir',
        onNextClick: () => {
            const atual = instancia.getActiveIndex() ?? 0;
            if (atual >= passos.length - 1) instancia.destroy();
            else void irPara(atual + 1);
        },
        onPrevClick: () => {
            void irPara((instancia.getActiveIndex() ?? 0) - 1);
        },
        onDoneClick: () => instancia.destroy(),
        onPopoverRender: (popover, { index }) => {
            popover.wrapper.setAttribute('aria-modal', 'true');
            if (index !== undefined && index < passos.length - 1) {
                popover.footer.insertBefore(
                    botao('Pular tutorial', 'tour-pular', () => instancia.destroy()),
                    popover.footer.firstChild,
                );
            }
        },
        onHighlighted: () => {
            // O menu pode ainda estar abrindo: reposiciona destaque e balão no fim da transição
            window.clearTimeout(transicao);
            transicao = window.setTimeout(() => {
                if (instancia.isActive()) instancia.refresh();
            }, ESPERA_TRANSICAO_MS);
        },
        onDestroyed: () => {
            encerrado = true;
            window.clearTimeout(transicao);
            aoEncerrar();
        },
    });

    async function preparar(passo: PassoTour) {
        if (passo.menu === 'expandido') controle.expandirMenu();
        if (passo.menu === 'importacao') controle.abrirImportacao();
        if (passo.rota && controle.rotaAtual() !== passo.rota) controle.navegar(passo.rota);
        await esperarAlvo(passo.alvo, () => encerrado);
    }

    async function irPara(indice: number) {
        if (ocupado || encerrado || indice < 0 || indice >= passos.length) return;
        ocupado = true;
        mostrarCarregando(instancia.getState('popover') as PopoverDOM | undefined);
        try {
            await preparar(passos[indice]);
        } finally {
            ocupado = false;
        }
        if (encerrado) return;
        if (instancia.isActive()) instancia.moveTo(indice);
        else instancia.drive(indice);
    }

    return {
        iniciar: () => void irPara(0),
        encerrar: () => {
            encerrado = true;
            if (instancia.isActive()) instancia.destroy();
        },
    };
}

interface OpcoesOferta {
    aoIniciar: () => void;
    aoFechar: () => void;
}

/** Convite do primeiro acesso: balão centralizado com "Iniciar tutorial" e "Agora não". */
export function oferecerTour({ aoIniciar, aoFechar }: OpcoesOferta): Driver {
    let iniciou = false;
    const oferta = driver({
        ...CONFIG_BALAO,
        popoverClass: 'tour-balao tour-oferta',
        showButtons: ['close'],
        onPopoverRender: (popover) => {
            const acoes = document.createElement('div');
            acoes.className = 'tour-oferta-acoes';
            acoes.append(
                botao('Agora não', 'tour-secundario', () => oferta.destroy()),
                botao('Iniciar tutorial', 'tour-primario', () => {
                    iniciou = true;
                    oferta.destroy();
                    aoIniciar();
                }),
            );
            popover.wrapper.append(acoes);
            popover.wrapper.setAttribute('aria-modal', 'true');
            acoes.querySelector<HTMLButtonElement>('.tour-primario')?.focus();
        },
        // Fechar, "Agora não" e Esc também contam como visto
        onDestroyed: () => {
            if (!iniciou) aoFechar();
        },
    });
    oferta.highlight({
        popover: {
            title: 'Primeiro acesso?',
            description:
                'Um tutorial rápido mostra cada tela do sistema, o que cada elemento faz e como os números são ' +
                'calculados. Você pode refazê-lo depois pelo item "Tutorial" do menu lateral.',
        },
    });
    return oferta;
}
