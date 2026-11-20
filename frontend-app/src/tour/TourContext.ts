import { createContext } from 'react';

/**
 * Estado do menu lateral, antes local no DefaultLayout (isMinimized) e no
 * Sidebar (isImportOpen), mais o início do tour. Fica num contexto para o tour
 * conseguir expandir o menu e abrir o acordeão sem clicar em nada.
 */
export interface ValorTour {
    iniciarTour: () => void;
    menuMinimizado: boolean;
    alternarMenu: () => void;
    importacaoAberta: boolean;
    setImportacaoAberta: (aberta: boolean) => void;
}

export const TourContext = createContext<ValorTour | null>(null);
