import type { ReactNode } from 'react';
import './pageHeader.css'

type PageHeaderProps = {
    titulo: string;
    subtitulo?: ReactNode;
    children?: ReactNode;
};

export function PageHeader({ titulo, subtitulo, children }: PageHeaderProps){
    return (
        <header className="page-header">
            <div>
                <h1>{titulo}</h1>
                {subtitulo && <p>{subtitulo}</p>}
            </div>
            {children && <div className="page-header-acoes">{children}</div>}
        </header>       
    );
}