import { useEffect, useState } from "react";
import { buscarMesesImportados, buscarSerie, type SerieMes } from "../../../../services/Dashboard";

// Mesmo limite do back (de 2 a 12)
export const MINIMO_MESES = 2;
export const MAXIMO_MESES = 12;

// Independente do mês da página: tem seleção e chamadas próprias
export function useComparativoMensal() {
  const [meses, setMeses] = useState<string[] | null>(null);
  const [erroMeses, setErroMeses] = useState<string | null>(null);
  const [selecionados, setSelecionados] = useState<string[]>([]);
  const [serie, setSerie] = useState<SerieMes[] | null>(null);
  const [carregandoSerie, setCarregandoSerie] = useState(false);
  const [erroSerie, setErroSerie] = useState<string | null>(null);

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

  // Só com 2+ meses: o back recusa menos. A resposta de uma seleção antiga é
  // descartada para não sobrescrever a atual.
  const chaveSelecao = selecionados.join(",");
  useEffect(() => {
    const pedidos = chaveSelecao ? chaveSelecao.split(",") : [];
    if (pedidos.length < MINIMO_MESES) {
      setSerie(null);
      setCarregandoSerie(false);
      setErroSerie(null);
      return;
    }

    let cancelado = false;
    setCarregandoSerie(true);
    setErroSerie(null);
    buscarSerie(pedidos)
      .then((resposta) => {
        if (!cancelado) setSerie(resposta);
      })
      .catch(() => {
        if (!cancelado) {
          setSerie(null);
          setErroSerie("Não foi possível carregar o comparativo dos meses selecionados.");
        }
      })
      .finally(() => {
        if (!cancelado) setCarregandoSerie(false);
      });
    return () => {
      cancelado = true;
    };
  }, [chaveSelecao]);

  const alternarMes = (mes: string) => {
    setSelecionados((atuais) => {
      if (atuais.includes(mes)) return atuais.filter((m) => m !== mes);
      if (atuais.length >= MAXIMO_MESES) return atuais;
      return [...atuais, mes].sort();
    });
  };

  return { meses, erroMeses, selecionados, alternarMes, serie, carregandoSerie, erroSerie };
}
