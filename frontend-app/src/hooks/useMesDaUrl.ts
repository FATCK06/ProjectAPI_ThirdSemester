import { useEffect } from "react";
import { useSearchParams } from "react-router-dom";
import { buscarMesesImportados } from "../services/Dashboard";
import { ehMesValido, mesAtual } from "../utils/datas";

/**
 * Mês de referência das telas mensais, guardado no ?mes= da URL (F5, voltar e
 * link compartilhado mantêm o mês). Sem ?mes (ou com um inválido ou futuro),
 * completa a URL com o mês mais recente que tem viagens importadas; se a lista
 * falhar ou vier vazia, usa o mês atual.
 *
 * Devolve null enquanto esse mês padrão ainda está sendo buscado.
 */
export function useMesDaUrl(): string | null {
  const [searchParams, setSearchParams] = useSearchParams();
  const parametro = searchParams.get("mes");
  const mes = ehMesValido(parametro) && parametro <= mesAtual() ? parametro : null;

  useEffect(() => {
    if (mes) return;
    let cancelado = false;
    buscarMesesImportados()
      .catch(() => [] as string[])
      .then((importados) => {
        if (cancelado) return;
        const padrao = importados[importados.length - 1] ?? mesAtual();
        // replace: completar a URL não deve criar um passo a mais no "voltar"
        setSearchParams(
          (atual) => {
            const parametros = new URLSearchParams(atual);
            parametros.set("mes", padrao);
            return parametros;
          },
          { replace: true },
        );
      });
    return () => {
      cancelado = true;
    };
  }, [mes, setSearchParams]);

  return mes;
}
