import { useContext } from 'react';
import { TourContext, type ValorTour } from './TourContext';

export function useTour(): ValorTour {
    const valor = useContext(TourContext);
    if (!valor) throw new Error('useTour precisa estar dentro de <TourProvider>');
    return valor;
}
