import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { Sidebar } from '../components/Sidebar/index';
import './layout.css';

export function DefaultLayout() {
    const [isMinimized, setIsMinimized] = useState(false);

    return (
        <div className="layout-wrapper">
            <Sidebar isMinimized={isMinimized} toggleSidebar={() => setIsMinimized(!isMinimized)} />

                <main className={`layout-content ${isMinimized ? 'minimized' : ''}`}>
                    <Outlet />
                </main>
        </div>
    );
}