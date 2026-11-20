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
  // Resultado com a chave da seleção que o pediu: chave diferente = ainda carregando
  const [resultadoSerie, setResultadoSerie] = useState<
    { chave: string; serie: SerieMes[] } | { chave: string; erro: string } | null
  >(null);

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
  const suficiente = selecionados.length >= MINIMO_MESES;
  const atual = suficiente && resultadoSerie?.chave === chaveSelecao ? resultadoSerie : null;
  const carregandoSerie = suficiente && atual === null;
  const serie = atual && "serie" in atual ? atual.serie : null;
  const erroSerie = atual && "erro" in atual ? atual.erro : null;

  useEffect(() => {
    const pedidos = chaveSelecao ? chaveSelecao.split(",") : [];
    if (pedidos.length < MINIMO_MESES) return;

    let cancelado = false;
    buscarSerie(pedidos)
      .then((resposta) => {
        if (!cancelado) setResultadoSerie({ chave: chaveSelecao, serie: resposta });
      })
      .catch(() => {
        if (!cancelado) {
          setResultadoSerie({
            chave: chaveSelecao,
            erro: "Não foi possível carregar o comparativo dos meses selecionados.",
          });
        }
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
