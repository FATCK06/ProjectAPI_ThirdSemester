import { ShieldX } from 'lucide-react';
import { Link } from 'react-router-dom';
import './acessoNegado.css';

/** Ocupa o lugar de uma página que o perfil do usuário não pode ver. */
export function AcessoNegado() {
    return (
        <div className="acesso-negado" role="alert">
            <ShieldX size={40} aria-hidden="true" />
            <strong>Acesso negado</strong>
            <p>Seu perfil não tem permissão para ver esta página. Se precisar dela, fale com um administrador.</p>
            <Link to="/dashboard" className="acesso-negado-voltar">
                Voltar para o dashboard
            </Link>
        </div>
    );
}
