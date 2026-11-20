import { useCallback, useEffect, useMemo, useRef, useState, type ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';
import type { Driver } from 'driver.js';
import { idUsuarioAtual, podeVerDadosFinanceiros, temPermissao } from '../services/auth';
import { marcarTutorialVisto, tutorialJaVisto } from './armazenamento';
import { criarTour, oferecerTour, type Tour } from './engine';
import { montarPassos } from './passos';
import { TourContext, type ValorTour } from './TourContext';

/** Dá tempo da primeira tela montar antes do convite do primeiro acesso. */
const ATRASO_CONVITE_MS = 800;

function marcarVisto() {
    const usuarioId = idUsuarioAtual();
    if (usuarioId) marcarTutorialVisto(usuarioId);
}

export function TourProvider({ children }: { children: ReactNode }) {
    const navigate = useNavigate();
    const [menuMinimizado, setMenuMinimizado] = useState(false);
    const [importacaoAberta, setImportacaoAberta] = useState(false);
    const tourRef = useRef<Tour | null>(null);
    const ofertaRef = useRef<Driver | null>(null);

    const iniciarTour = useCallback(() => {
        if (ofertaRef.current?.isActive()) ofertaRef.current.destroy();
        ofertaRef.current = null;
        tourRef.current?.encerrar();
        // Começar já conta como visto: concluir, pular ou fechar não reabre o convite
        marcarVisto();

        // Permissões lidas na hora: o tour é montado para o perfil de quem está logado
        const passos = montarPassos({
            financeiro: podeVerDadosFinanceiros(),
            gerenciarUsuarios: temPermissao('GERENCIAR_USUARIOS'),
            verStatus: temPermissao('VER_STATUS_SERVICO'),
        });
        const tour = criarTour(
            passos,
            {
                rotaAtual: () => window.location.pathname,
                navegar: (rota) => navigate(rota),
                expandirMenu: () => setMenuMinimizado(false),
                abrirImportacao: () => {
                    setMenuMinimizado(false);
                    setImportacaoAberta(true);
                },
            },
            () => {
                if (tourRef.current === tour) tourRef.current = null;
            },
        );
        tourRef.current = tour;
        tour.iniciar();
    }, [navigate]);

    // Primeiro acesso deste usuário neste navegador: oferece o tour uma vez
    useEffect(() => {
        const usuarioId = idUsuarioAtual();
        if (!usuarioId || tutorialJaVisto(usuarioId)) return;

        const temporizador = window.setTimeout(() => {
            if (tourRef.current) return;
            ofertaRef.current = oferecerTour({
                aoIniciar: iniciarTour,
                aoFechar: () => {
                    ofertaRef.current = null;
                    marcarTutorialVisto(usuarioId);
                },
            });
        }, ATRASO_CONVITE_MS);
        return () => window.clearTimeout(temporizador);
    }, [iniciarTour]);

    // Saiu do layout (ex.: "Sair"): nada do tour pode ficar na tela
    useEffect(
        () => () => {
            if (ofertaRef.current?.isActive()) ofertaRef.current.destroy();
            tourRef.current?.encerrar();
        },
        [],
    );

    const valor = useMemo<ValorTour>(
        () => ({
            iniciarTour,
            menuMinimizado,
            alternarMenu: () => setMenuMinimizado((atual) => !atual),
            importacaoAberta,
            setImportacaoAberta,
        }),
        [iniciarTour, menuMinimizado, importacaoAberta],
    );

    return <TourContext.Provider value={valor}>{children}</TourContext.Provider>;
}
