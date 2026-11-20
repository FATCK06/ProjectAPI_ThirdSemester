import api from './api';

/** Tipo cadastrado que não aponta para nenhuma das sete categorias ("Não classificado"). */
export interface TipoSemCategoria {
    tipo: string;
    quantidade: number;
    placas: string[];
}

/** Viagem cujo veículo não veio na planilha de veículos ("Não identificado"). */
export interface ManifestoSemVeiculo {
    manifesto: number;
    placa: string | null;
    /** AAAA-MM-DD */
    data: string;
}

/** Cruzamento entre manifestos e planilha de veículos, sobre o estado atual do banco. */
export interface ResultadoTratamentoVeiculos {
    veiculosEmManifestos: number;
    veiculosComCategoria: number;
    tiposSemCategoria: TipoSemCategoria[];
    manifestosSemVeiculo: ManifestoSemVeiculo[];
}

export async function buscarTratamentoVeiculos(): Promise<ResultadoTratamentoVeiculos> {
    const { data } = await api.get<ResultadoTratamentoVeiculos>('/importacao/veiculos/tratamento');
    return data;
}
