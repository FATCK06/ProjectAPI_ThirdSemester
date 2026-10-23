/** Mês corrente no formato "AAAA-MM", o mesmo de mesReferencia na API. */
export function mesAtual(): string {
  const hoje = new Date();
  return `${hoje.getFullYear()}-${String(hoje.getMonth() + 1).padStart(2, "0")}`;
}
<<<<<<< HEAD

function formatarMes(mesReferencia: string, month: "short" | "long"): string {
  const [ano, mes] = mesReferencia.split("-").map(Number);
  if (!ano || !mes) return mesReferencia;
  return new Date(ano, mes - 1, 1).toLocaleDateString("pt-BR", { month, year: "numeric" });
}

/** "2026-06" -> "jun. de 2026" */
export function formatarMesCurto(mesReferencia: string): string {
  return formatarMes(mesReferencia, "short");
}

/** "2026-06" -> "junho de 2026" */
export function formatarMesExtenso(mesReferencia: string): string {
  return formatarMes(mesReferencia, "long");
}

/** ("2026-04", "2026-05") -> "abr. a mai. de 2026"; anos diferentes repetem o ano nos dois. */
export function formatarIntervaloMeses(inicio: string, fim: string): string {
  const anoInicio = inicio.slice(0, 4);
  if (anoInicio !== fim.slice(0, 4)) {
    return `${formatarMesCurto(inicio)} a ${formatarMesCurto(fim)}`;
  }
  const [ano, mes] = inicio.split("-").map(Number);
  const mesInicio = new Date(ano, mes - 1, 1).toLocaleDateString("pt-BR", { month: "short" });
  return `${mesInicio} a ${formatarMesCurto(fim)}`;
}
=======
>>>>>>> origin/develop
