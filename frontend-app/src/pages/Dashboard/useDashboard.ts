import { useEffect, useState } from "react";
import {
    buscarHistorico,
    buscarIndicadores,
    buscarIndicadoresPorModelo,
    type HistoricoMes,
    type IndicadoresMes,
    type IndicadoresPorModelo,
} from "../../services/Dashboard";
import { MAXIMO_MESES_COMPARACAO } from "./components/IndicadoresCard/selecao";

// Mês selecionado + o maior período de comparação. Vem tudo de uma vez: trocar o
// período no card só recorta, sem nova chamada.
const MESES_HISTORICO = MAXIMO_MESES_COMPARACAO + 1;

interface DadosDashboard {
    indicadores: IndicadoresMes;
    historico: HistoricoMes[];
    porModelo: IndicadoresPorModelo[];
}

interface OpcoesDashboard {
    // Com o gráfico por modelo desligado, não há por que chamar o endpoint
    buscarPorModelo: boolean;
}

// mesReferencia null: o mês ainda não chegou do servidor, o skeleton continua
export function useDashboard(mesReferencia: string | null, { buscarPorModelo }: OpcoesDashboard) {
    const [dados, setDados] = useState<DadosDashboard | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [erro, setErro] = useState<string | null>(null);

    useEffect(() => {
        if (!mesReferencia) return;

        let cancelado = false;
        setIsLoading(true);
        setErro(null);

        //As chamadas saem juntas, o loading só termina quando todas voltam,
        //assim o skeleton fica até o card e os dois graficos estarem prontos
        Promise.all([
            buscarIndicadores(mesReferencia),
            buscarHistorico(mesReferencia, MESES_HISTORICO),
            buscarPorModelo
                ? buscarIndicadoresPorModelo(mesReferencia)
                : Promise.resolve<IndicadoresPorModelo[]>([]),
        ])
            .then(([indicadores, historico, porModelo]) => {
                if (cancelado) return;
                setDados({ indicadores, historico, porModelo });
            })
            .catch(() => {
                if (cancelado) return;
                setErro("Não foi possivel carregar os indicadores deste mês.");
            })
            .finally(() => {
                if (!cancelado) setIsLoading(false);
            })

        return () => {
            cancelado = true;
        };
    }, [mesReferencia, buscarPorModelo]);

    return { dados, isLoading, erro };
}
