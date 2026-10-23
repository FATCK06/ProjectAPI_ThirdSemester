import { useEffect, useState } from 'react';
import { Outlet } from 'react-router-dom';
import { Sidebar } from '../components/Sidebar/index';
import { useToast } from '../components/Toast';
import { EVENTO_ACESSO_NEGADO } from '../services/api';
import './layout.css';

export function DefaultLayout() {
    const [isMinimized, setIsMinimized] = useState(false);
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
            <Sidebar isMinimized={isMinimized} toggleSidebar={() => setIsMinimized(!isMinimized)} />

                <main className={`layout-content ${isMinimized ? 'minimized' : ''}`}>
                    <Outlet />
                </main>
        </div>
    );
}
