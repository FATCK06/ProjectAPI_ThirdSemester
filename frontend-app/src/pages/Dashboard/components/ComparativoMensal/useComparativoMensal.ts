import { useEffect, useState } from "react";
import { buscarMesesImportados } from "../../../../services/Dashboard";

// Mesmo limite do back (de 2 a 12)
export const MINIMO_MESES = 2;
export const MAXIMO_MESES = 12;

// Independente do mês da página: tem seleção e chamadas próprias
export function useComparativoMensal() {
  const [meses, setMeses] = useState<string[] | null>(null);
  const [erroMeses, setErroMeses] = useState<string | null>(null);
  const [selecionados, setSelecionados] = useState<string[]>([]);

  useEffect(() => {
    let cancelado = false;
    buscarMesesImportados()
      .then((resposta) => {
        if (cancelado) return;
        setMeses(resposta);
        setSelecionados(resposta.slice(-MINIMO_MESES));
      })
      .catch(() => {
        if (!cancelado) setErroMeses("Não foi possível carregar os meses importados.");
      });
    return () => {
      cancelado = true;
    };
  }, []);

  const alternarMes = (mes: string) => {
    setSelecionados((atuais) => {
      if (atuais.includes(mes)) return atuais.filter((m) => m !== mes);
      if (atuais.length >= MAXIMO_MESES) return atuais;
      return [...atuais, mes].sort();
    });
  };

  return { meses, erroMeses, selecionados, alternarMes };
}
