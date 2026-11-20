import { Suspense, useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import { Sidebar } from '../components/Sidebar/index';
import { useToast } from '../components/Toast';
import { Loading } from '../components/Loading';
import { EVENTO_ACESSO_NEGADO } from '../services/api';
import { TourProvider } from '../tour/TourProvider';
import { useTour } from '../tour/useTour';
import './layout.css';

export function DefaultLayout() {
    // O estado do menu mora no TourProvider para o tutorial poder expandi-lo
    return (
        <TourProvider>
            <LayoutConteudo />
        </TourProvider>
    );
}

function LayoutConteudo() {
    const { menuMinimizado: isMinimized, alternarMenu } = useTour();
    const toast = useToast();

    // Qualquer 403 vindo da API (ver api.ts) vira um aviso, para o usuário
    // entender por que algo não carregou.
    useEffect(() => {
        const avisar = () =>
            toast.aviso('Seu perfil não tem permissão para acessar este recurso.', { titulo: 'Acesso negado' });
        window.addEventListener(EVENTO_ACESSO_NEGADO, avisar);
        return () => window.removeEventListener(EVENTO_ACESSO_NEGADO, avisar);
    }, [toast]);

    return (
        <div className="layout-wrapper">
            <Sidebar isMinimized={isMinimized} toggleSidebar={alternarMenu} />

            <main className={`layout-content ${isMinimized ? 'minimized' : ''}`}>
                {/* As páginas são carregadas sob demanda (ver AppRoutes) */}
                <Suspense fallback={<Loading texto="Carregando página" />}>
                    <Outlet />
                </Suspense>
            </main>
        </div>
    );
}
