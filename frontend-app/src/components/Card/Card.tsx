import "./card.css";

interface PropsCard {
    titulo: string;
    subtitulo?: string;
    sobretitulo?: string
    acoes?: React.ReactNode;
    className?: string;
    children: React.ReactNode;
}

export function Card({
    titulo,
    subtitulo,
    sobretitulo,
    acoes,
    className,
    children
}: PropsCard) {
    return (
        <section className={`ui-card ${className ?? ""}`.trim()}>
            <header className="ui-card-header">
                <div>
                    {sobretitulo && <p className="ui-card-eyebrow">{sobretitulo}</p>}
                    <h2 className="ui-card-title">{titulo}</h2>
                    {subtitulo && <p className="ui-card-subtitle">{subtitulo}</p>}
                </div>
                {acoes}
            </header>
            {children}
        </section>
    );
}
