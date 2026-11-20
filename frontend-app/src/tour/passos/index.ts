import { passosBoasVindas } from './boasVindas';
import { passosDashboard } from './dashboard';
import { passosEncerramento } from './encerramento';
import { passosMenu } from './menu';
import type { MontarPassos, PassoTour, PermissoesTour } from './tipos';

/** Na ordem do menu lateral. */
const TELAS: MontarPassos[] = [passosBoasVindas, passosMenu, passosDashboard, passosEncerramento];

export function montarPassos(permissoes: PermissoesTour): PassoTour[] {
    return TELAS.flatMap((tela) => tela(permissoes));
}

export type { PassoTour, PermissoesTour } from './tipos';
