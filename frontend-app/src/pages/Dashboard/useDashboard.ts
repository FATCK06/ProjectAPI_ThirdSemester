import { useEffect, useState } from "react";
import {
    buscarIndicadores,
    buscarIndicadoresPorModelo,
    type IndicadoresMes,
    type IndicadoresPorModelo,
} from "../../services/Dashboard";

interface DadosDashboard {
    indicadores: IndicadoresMes;
    porModelo: IndicadoresPorModelo[];
}

export function useDashboard(mesReferencia: string) {
    const [dados, setDados] = useState<DadosDashboard | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [erro, setErro] = useState<string | null>(null);

    useEffect(() => {
        let cancelado = false;
        setIsLoading(true);
        setErro(null);

        //As duas chamadas saem juntas, o loading só termina quando ambas voltam,
        //assim o skeleton fica até o card e os dois graficos estarem prontos
        Promise.all([
            buscarIndicadores(mesReferencia),
            buscarIndicadoresPorModelo(mesReferencia),
        ])
            .then(([indicadores, porModelo]) => {
                if (cancelado) return;
                setDados({ indicadores, porModelo });
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
    }, [mesReferencia]);

    return { dados, isLoading, erro };
}