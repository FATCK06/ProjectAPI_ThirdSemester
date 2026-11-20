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
    // Resultado com a chave do pedido: chave diferente da atual = ainda carregando
    const [resultado, setResultado] = useState<
        { chave: string; dados: DadosDashboard } | { chave: string; erro: string } | null
    >(null);
    const chave = `${mesReferencia}|${buscarPorModelo}`;
    const atual = resultado?.chave === chave ? resultado : null;
    const isLoading = atual === null;
    const dados = atual && "dados" in atual ? atual.dados : null;
    const erro = atual && "erro" in atual ? atual.erro : null;

    useEffect(() => {
        if (!mesReferencia) return;

        let cancelado = false;

        //As chamadas saem juntas, o loading só termina quando todas voltam,
        //assim o skeleton fica até o card e os dois graficos estarem prontos
        Promise.all([
            buscarIndicadores(mesReferencia),
            // O histórico só alimenta comparações. Se essa rota estiver
            // indisponível, ainda mostramos os indicadores do mês.
            buscarHistorico(mesReferencia, MESES_HISTORICO).catch(() => []),
            buscarPorModelo
                ? buscarIndicadoresPorModelo(mesReferencia).catch(() => [])
                : Promise.resolve<IndicadoresPorModelo[]>([]),
        ])
            .then(([indicadores, historico, porModelo]) => {
                if (!cancelado) setResultado({ chave, dados: { indicadores, historico, porModelo } });
            })
            .catch(() => {
                if (!cancelado) setResultado({ chave, erro: "Não foi possível carregar os indicadores deste mês." });
            });

        return () => {
            cancelado = true;
        };
    }, [mesReferencia, buscarPorModelo, chave]);

    return { dados, isLoading, erro };
}
