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

export function formatarCpf(valor: string | null | undefined): string {
  if (!valor) return "Não informado";
  const digitos = valor.replace(/\D/g, "");
  if (digitos.length !== 11) return valor;
  return digitos.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, "$1.$2.$3-$4");
}

export function formatarCep(valor: string | null | undefined): string {
  if (!valor) return "Não informado";
  const digitos = valor.replace(/\D/g, "");
  if (digitos.length !== 8) return valor;
  return digitos.replace(/(\d{5})(\d{3})/, "$1-$2");
}
