import { useContext } from 'react';
import { ToastContext, type ApiToast } from './ToastContext';

/**
 * Dispara notificações passageiras de qualquer tela:
 *   const toast = useToast();
 *   toast.sucesso('Usuário cadastrado');
 *   toast.erro(mensagemDeErro(falha), { titulo: 'Falha no envio' });
 */
export function useToast(): ApiToast {
    const contexto = useContext(ToastContext);
    if (!contexto) {
        throw new Error('useToast precisa estar dentro de <ToastProvider>');
    }
    return contexto;
}
