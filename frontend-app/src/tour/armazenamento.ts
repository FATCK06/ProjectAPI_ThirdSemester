// Marca de "já viu o tutorial", por usuário (sub do token). Toda leitura e
// escrita fica em try/catch: navegador sem localStorage (modo privado, bloqueio)
// não pode quebrar o app.

const PREFIXO = '@Logistica:tutorialVisto:';

/**
 * Sem conseguir ler, devolve true: como não daria para lembrar a resposta,
 * o convite apareceria em todo acesso. O botão "Tutorial" continua funcionando.
 */
export function tutorialJaVisto(usuarioId: string): boolean {
    try {
        return localStorage.getItem(PREFIXO + usuarioId) === '1';
    } catch {
        return true;
    }
}

export function marcarTutorialVisto(usuarioId: string): void {
    try {
        localStorage.setItem(PREFIXO + usuarioId, '1');
    } catch {
        // Sem armazenamento: só não fica lembrado
    }
}
