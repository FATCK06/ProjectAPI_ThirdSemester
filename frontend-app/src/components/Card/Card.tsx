import type { ReactNode } from "react";
import "./card.css";

interface PropsCard {
    titulo: string;
    subtitulo?:  string;
    acoes?: React.ReactNode;
    children: React.ReactNode;
}

export function Card({ titulo, subtitulo, acoes, children }: PropsCard) {
    return (
        <section className="ui-card">
            <header className="ui-card-header">
                <div>
                    <h2 className="ui-card-title">{titulo}</h2>
                    {subtitulo && <p className="ui-card-subtitle">{subtitulo}</p>}
                </div>
                {acoes}
            </header>
            {children}
        </section>
    );
}