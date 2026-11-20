import { passosBoasVindas } from './boasVindas';
import { passosDashboard } from './dashboard';
import { passosEncerramento } from './encerramento';
import { passosImportacao } from './importacao';
import { passosMenu } from './menu';
import { passosMotoristas } from './motoristas';
import { passosOperacaoMensal } from './operacaoMensal';
import { passosRanking } from './ranking';
import type { MontarPassos, PassoTour, PermissoesTour } from './tipos';

/** Na ordem do menu lateral. */
const TELAS: MontarPassos[] = [
    passosBoasVindas,
    passosMenu,
    passosDashboard,
    passosOperacaoMensal,
    passosMotoristas,
    passosRanking,
    passosImportacao,
    passosEncerramento,
];

export function montarPassos(permissoes: PermissoesTour): PassoTour[] {
    return TELAS.flatMap((tela) => tela(permissoes));
}

export type { PassoTour, PermissoesTour } from './tipos';
