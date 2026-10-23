// Valor ausente vira "-": a API omite o campo quando não há dado (média sem
// viagem apurável, margem sem frete) ou quando o perfil não pode vê-lo.
export const SEM_VALOR = "-";

export function formatarMoeda(valor: number | null | undefined): string {
  if (valor === null || valor === undefined) return SEM_VALOR;
  return valor.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

/** Percentual de 0 a 100, como a API devolve. */
export function formatarPercentual(valor: number | null | undefined): string {
  if (valor === null || valor === undefined) return SEM_VALOR;
  return `${valor.toLocaleString("pt-BR", { maximumFractionDigits: 2 })}%`;
}
