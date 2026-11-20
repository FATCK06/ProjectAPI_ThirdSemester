import { idUsuarioAtual } from "../services/auth";

/**
 * Preferências de tela guardadas no navegador, separadas por usuário: quem
 * divide o computador não herda os filtros do outro. São só conveniência, então
 * qualquer falha (modo privado, storage cheio, JSON corrompido) cai no padrão.
 *
 * As "de sessão" ficam no sessionStorage: valem enquanto a aba estiver aberta
 * (ex.: o mês do Dashboard, que não deve abrir dias depois num mês antigo).
 */
function chave(nome: string): string | null {
  const usuario = idUsuarioAtual();
  return usuario ? `@Logistica:preferencia:${usuario}:${nome}` : null;
}

function ler(storage: () => Storage, nome: string): unknown {
  const k = chave(nome);
  if (!k) return undefined;
  try {
    const texto = storage().getItem(k);
    return texto === null ? undefined : JSON.parse(texto);
  } catch {
    return undefined;
  }
}

function gravar(storage: () => Storage, nome: string, valor: unknown): void {
  const k = chave(nome);
  if (!k) return;
  try {
    storage().setItem(k, JSON.stringify(valor));
  } catch {
    // Sem storage disponível a tela segue funcionando, só não lembra a escolha
  }
}

// Funções e não o objeto: acessar localStorage pode lançar exceção (site bloqueado)
const local = () => localStorage;
const sessao = () => sessionStorage;

export const lerPreferencia = (nome: string) => ler(local, nome);
export const gravarPreferencia = (nome: string, valor: unknown) => gravar(local, nome, valor);
export const lerPreferenciaSessao = (nome: string) => ler(sessao, nome);
export const gravarPreferenciaSessao = (nome: string, valor: unknown) => gravar(sessao, nome, valor);
