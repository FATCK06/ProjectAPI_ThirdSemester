import type { ReactNode } from 'react';
import type { LucideIcon } from 'lucide-react';
import './pageHeader.css'

type PageHeaderProps = {
    titulo: string;
    // Ícone antes do título, para reconhecer a tela sem ler (ex.: as importações)
    icone?: LucideIcon;
    tomIcone?: 'azul' | 'ambar' | 'cinza';
    // Ao lado do título, ex.: <SeloMes /> nas telas com dados de um mês
    selo?: ReactNode;
    subtitulo?: ReactNode;
    children?: ReactNode;
};

export function PageHeader({ titulo, icone: Icone, tomIcone = 'azul', selo, subtitulo, children }: PageHeaderProps){
    return (
        <header className="page-header" data-tour="cabecalho-pagina">
            <div className="page-header-titulo">
                {Icone && (
                    <span className="page-header-icone" data-tom={tomIcone} aria-hidden="true">
                        <Icone size={24} />
                    </span>
                )}
                <div>
                    <h1>{titulo}{selo}</h1>
                    {subtitulo && <p>{subtitulo}</p>}
                </div>
            </div>
            {children && <div className="page-header-acoes">{children}</div>}
        </header>
    );
}
