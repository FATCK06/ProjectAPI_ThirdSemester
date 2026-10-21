/** Mês corrente no formato "AAAA-MM", o mesmo de mesReferencia na API. */
export function mesAtual(): string {
  const hoje = new Date();
  return `${hoje.getFullYear()}-${String(hoje.getMonth() + 1).padStart(2, "0")}`;
}
