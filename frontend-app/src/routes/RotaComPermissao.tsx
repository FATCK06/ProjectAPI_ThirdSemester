import { Outlet } from 'react-router-dom';
import { temPermissao, type Permissao } from '../services/auth';
import { AcessoNegado } from '../components/AcessoNegado';

/**
 * Envolve rotas que exigem uma permissão. Sem ela, mostra o aviso no lugar da
 * página (mantendo a URL), em vez de redirecionar sem explicar.
 */
export function RotaComPermissao({ permissao }: { permissao: Permissao }) {
    return temPermissao(permissao) ? <Outlet /> : <AcessoNegado />;
}
