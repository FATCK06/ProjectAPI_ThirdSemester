/** Mês corrente no formato "AAAA-MM", o mesmo de mesReferencia na API. */
export function mesAtual(): string {
  const hoje = new Date();
  return `${hoje.getFullYear()}-${String(hoje.getMonth() + 1).padStart(2, "0")}`;
}

/** "AAAA-MM" com mês de 01 a 12. */
export function ehMesValido(valor: string | null | undefined): valor is string {
  if (!valor || !/^\d{4}-\d{2}$/.test(valor)) return false;
  const mes = Number(valor.slice(5));
  return mes >= 1 && mes <= 12;
}

// Fixos em vez de toLocaleDateString: o abreviado do navegador vem como "mai."
const NOMES_MESES = [
  "janeiro", "fevereiro", "março", "abril", "maio", "junho",
  "julho", "agosto", "setembro", "outubro", "novembro", "dezembro",
];

function partes(mesReferencia: string): { ano: string; nome: string } | null {
  const [ano, mes] = mesReferencia.split("-");
  const nome = NOMES_MESES[Number(mes) - 1];
  return ano && nome ? { ano, nome } : null;
}

function capitalizar(texto: string): string {
  return texto.charAt(0).toUpperCase() + texto.slice(1);
}

/** "2026-06" -> "Jun. 2026": botões, chips e eixos de gráfico. */
export function formatarMesCurto(mesReferencia: string): string {
  const p = partes(mesReferencia);
  return p ? `${capitalizar(p.nome.slice(0, 3))}. ${p.ano}` : mesReferencia;
}

/** "2026-06" -> "Junho 2026": destaque, como o selo no título de um card. */
export function formatarMesTitulo(mesReferencia: string): string {
  const p = partes(mesReferencia);
  return p ? `${capitalizar(p.nome)} ${p.ano}` : mesReferencia;
}

/** "2026-06" -> "junho de 2026": texto corrido e listas de seleção. */
export function formatarMesExtenso(mesReferencia: string): string {
  const p = partes(mesReferencia);
  return p ? `${p.nome} de ${p.ano}` : mesReferencia;
}

/** "2026-06" -> "06/2026" */
export function formatarMesNumerico(mesReferencia: string): string {
  const [ano, mes] = mesReferencia.split("-");
  if (!ano || !mes) return mesReferencia;
  return `${mes}/${ano}`;
}

/** ("2026-04", "2026-05") -> "abril a maio de 2026"; anos diferentes repetem o ano nos dois. */
export function formatarIntervaloMeses(inicio: string, fim: string): string {
  if (inicio.slice(0, 4) !== fim.slice(0, 4)) {
    return `${formatarMesExtenso(inicio)} a ${formatarMesExtenso(fim)}`;
  }
  return `${partes(inicio)?.nome ?? inicio} a ${formatarMesExtenso(fim)}`;
}
