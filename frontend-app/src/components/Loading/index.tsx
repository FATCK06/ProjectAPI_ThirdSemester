import './loading.css';

interface LoadingProps {
    /** Frase principal, ex.: "Lendo o arquivo". */
    texto?: string;
    /** Linha menor abaixo do texto, ex.: "Não feche esta janela." */
    detalhe?: string;
    /** 'md' (56px) para tela cheia; 'sm' (32px) para dentro de cards e seções. */
    tamanho?: 'sm' | 'md';
}

/**
 * Spinner centralizado com texto opcional, para esperas de tela inteira ou de
 * uma seção. Para esperas curtas dentro de listas, prefira o SkeletonLoader.
 */
export function Loading({ texto, detalhe, tamanho = 'md' }: LoadingProps) {
    return (
        <div className={`loading-wrap loading-${tamanho}`} role="status" aria-live="polite">
            <div className="loading-spinner" aria-hidden="true" />
            {texto && <p className="loading-texto">{texto}</p>}
            {detalhe && <p className="loading-detalhe">{detalhe}</p>}
            {/* Leitor de tela precisa de algo para anunciar mesmo sem texto visível */}
            {!texto && <span className="loading-sr">Carregando</span>}
        </div>
    );
}
