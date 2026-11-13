import { useEffect, useState } from 'react';
import { AlertTriangle, CheckCircle2 } from 'lucide-react';
import {
    buscarTratamentoVeiculos,
    type ResultadoTratamentoVeiculos,
} from '../../../../services/tratamentoVeiculos';
import './resultadoTratamento.css';

type Estado =
    | { status: 'carregando' }
    | { status: 'erro' }
    | { status: 'ok'; dados: ResultadoTratamentoVeiculos };

/** A lista de manifestos pode ser longa: mostra o começo e conta o resto. */
const LIMITE_MANIFESTOS = 50;

function formatarData(iso: string): string {
    const [ano, mes, dia] = iso.split('-');
    return `${dia}/${mes}/${ano}`;
}

/**
 * Passo 6 da importação de veículos: como os veículos cruzaram com os manifestos.
 * Consulta o estado atual do banco, então reflete também importações anteriores.
 */
export function ResultadoTratamento() {
    const [estado, setEstado] = useState<Estado>({ status: 'carregando' });

    useEffect(() => {
        let ativo = true;
        buscarTratamentoVeiculos()
            .then((dados) => ativo && setEstado({ status: 'ok', dados }))
            .catch(() => ativo && setEstado({ status: 'erro' }));
        return () => {
            ativo = false;
        };
    }, []);

    if (estado.status === 'carregando') {
        return <p className="tratamento-info">Cruzando veículos com os manifestos...</p>;
    }
    if (estado.status === 'erro') {
        return (
            <p className="tratamento-info">
                Os veículos foram gravados, mas não foi possível carregar o cruzamento com os manifestos agora.
            </p>
        );
    }

    const { veiculosEmManifestos, veiculosComCategoria, tiposSemCategoria, manifestosSemVeiculo } = estado.dados;
    const veiculosSemCategoria = tiposSemCategoria.reduce((soma, t) => soma + t.quantidade, 0);
    const tudoCruzou = veiculosSemCategoria === 0 && manifestosSemVeiculo.length === 0;

    return (
        <section className="tratamento">
            <h4 className="tratamento-titulo">Cruzamento com os manifestos</h4>

            <div className="resultado-numeros">
                <div className="resultado-card">
                    <span className="resultado-valor">{veiculosEmManifestos}</span>
                    <span className="resultado-rotulo">veículos em manifestos</span>
                </div>
                <div className="resultado-card destaque">
                    <span className="resultado-valor">{veiculosComCategoria}</span>
                    <span className="resultado-rotulo">com categoria</span>
                </div>
            </div>

            {tudoCruzou && (
                <p className="tratamento-ok">
                    <CheckCircle2 size={16} /> Todos os veículos dos manifestos têm categoria.
                </p>
            )}

            {manifestosSemVeiculo.length > 0 && (
                <details className="tratamento-aviso">
                    <summary>
                        <AlertTriangle size={16} />
                        {manifestosSemVeiculo.length} manifestos sem veículo correspondente na planilha de veículos.
                    </summary>
                    <p className="tratamento-explica">Aparecem como "Não identificado" na listagem e no ranking.</p>
                    <table className="tratamento-tabela">
                        <thead>
                            <tr><th>Manifesto</th><th>Placa</th><th>Data</th></tr>
                        </thead>
                        <tbody>
                            {manifestosSemVeiculo.slice(0, LIMITE_MANIFESTOS).map((m) => (
                                <tr key={m.manifesto}>
                                    <td>{m.manifesto}</td>
                                    <td className="celula-mono">{m.placa ?? 'Sem placa'}</td>
                                    <td>{formatarData(m.data)}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                    {manifestosSemVeiculo.length > LIMITE_MANIFESTOS && (
                        <p className="tratamento-explica">
                            E mais {manifestosSemVeiculo.length - LIMITE_MANIFESTOS} manifestos.
                        </p>
                    )}
                </details>
            )}

            {veiculosSemCategoria > 0 && (
                <details className="tratamento-aviso">
                    <summary>
                        <AlertTriangle size={16} />
                        {veiculosSemCategoria} veículos usados em manifestos estão com tipo sem categoria.
                    </summary>
                    <p className="tratamento-explica">Aparecem como "Não classificado" na listagem e no ranking.</p>
                    <table className="tratamento-tabela">
                        <thead>
                            <tr><th>Tipo na planilha</th><th>Veículos</th><th>Placas</th></tr>
                        </thead>
                        <tbody>
                            {tiposSemCategoria.map((t) => (
                                <tr key={t.tipo}>
                                    <td>{t.tipo}</td>
                                    <td>{t.quantidade}</td>
                                    <td className="celula-mono">{t.placas.join(', ')}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </details>
            )}
        </section>
    );
}
