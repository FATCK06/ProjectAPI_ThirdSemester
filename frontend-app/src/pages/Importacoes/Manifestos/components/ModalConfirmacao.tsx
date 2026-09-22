import { useEffect } from 'react';
import { AlertCircle } from 'lucide-react';
import './modal.css';

interface PropsModal {
    titulo: string;
    mensagem: string;
    textoConfirmar: string;
    aoConfirmar: () => void;
    aoCancelar: () => void;
}

export function ModalConfirmacao({
    titulo,
    mensagem,
    textoConfirmar,
    aoConfirmar,
    aoCancelar,
}: PropsModal) {
    // Esc fecha: é o que se espera de um diálogo, e evita prender o usuário
    // numa tela cujo botão principal grava no banco.
    useEffect(() => {
        const aoTeclar = (e: KeyboardEvent) => {
            if (e.key === 'Escape') aoCancelar();
        };
        window.addEventListener('keydown', aoTeclar);
        return () => window.removeEventListener('keydown', aoTeclar);
    }, [aoCancelar]);

    return (
        <div className="modal-fundo" onClick={aoCancelar}>
            <div
                className="modal-caixa"
                role="dialog"
                aria-modal="true"
                aria-labelledby="modal-titulo"
                onClick={(e) => e.stopPropagation()}
            >
                <div className="modal-icone">
                    <AlertCircle size={24} />
                </div>

                <h3 id="modal-titulo" className="modal-titulo">
                    {titulo}
                </h3>
                <p className="modal-mensagem">{mensagem}</p>

                <div className="modal-acoes">
                    <button className="modal-btn-cancelar" onClick={aoCancelar}>
                        Cancelar
                    </button>
                    <button className="modal-btn-confirmar" onClick={aoConfirmar} autoFocus>
                        {textoConfirmar}
                    </button>
                </div>
            </div>
        </div>
    );
}
