import { useCallback, useEffect, useState } from 'react';
import { CheckCircle2, AlertCircle, RefreshCw } from 'lucide-react';
import { buscarStatus, mensagemDeErro, type StatusGeral } from '../../services/status';
import './status.css';

const INTERVALO_MS = 30_000;

export function StatusServicos() {
    const [dados, setDados] = useState<StatusGeral | null>(null);
    const [erro, setErro] = useState<string | null>(null);
    const [carregando, setCarregando] = useState(true);
    const [atualizadoEm, setAtualizadoEm] = useState<Date | null>(null);

    const consultar = useCallback(async () => {
        setCarregando(true);
        try {
            setDados(await buscarStatus());
            setErro(null);
        } catch (falha) {
            setDados(null);
            setErro(mensagemDeErro(falha));
        } finally {
            setCarregando(false);
            setAtualizadoEm(new Date());
        }
    }, []);

    useEffect(() => {
        consultar();
        const timer = setInterval(consultar, INTERVALO_MS);
        return () => clearInterval(timer);
    }, [consultar]);

    return (
        <div className="status-container">
            <div className="status-header">
                <div>
                    <h2>Status dos serviços</h2>
                    {atualizadoEm && (
                        <p className="status-atualizado">
                            Atualizado às {atualizadoEm.toLocaleTimeString('pt-BR')} · a cada 30s
                        </p>
                    )}
                </div>
                <button className="btn-atualizar" onClick={consultar} disabled={carregando}>
                    <RefreshCw size={16} className={carregando ? 'girando' : ''} />
                    Atualizar
                </button>
            </div>

            {erro && (
                <div className="status-alerta">
                    <AlertCircle size={18} />
                    <span>{erro}</span>
                </div>
            )}

            {dados && (
                <>
                    <div className={`status-resumo ${dados.geral === 'UP' ? 'ok' : 'falha'}`}>
                        {dados.geral === 'UP' ? <CheckCircle2 size={20} /> : <AlertCircle size={20} />}
                        <span>
                            {dados.geral === 'UP'
                                ? 'Todos os serviços estão no ar'
                                : 'Há serviço fora do ar'}
                        </span>
                    </div>

                    <div className="status-lista">
                        {dados.servicos.map((s) => (
                            <div key={s.servico} className="status-card">
                                <span className={`status-bolinha ${s.status === 'UP' ? 'ok' : 'falha'}`} />

                                <div className="status-info">
                                    <span className="status-nome">{s.servico}</span>
                                    {s.detalhe && <span className="status-detalhe">{s.detalhe}</span>}
                                </div>

                                <span className="status-tempo">{s.tempoMs} ms</span>
                                <span className={`status-rotulo ${s.status === 'UP' ? 'ok' : 'falha'}`}>
                                    {s.status === 'UP' ? 'No ar' : 'Fora'}
                                </span>
                            </div>
                        ))}
                    </div>

                    <p className="status-nota">
                        O estado considera a conexão com o banco: um serviço que responde na porta
                        mas não alcança o Supabase aparece como <strong>Fora</strong>.
                    </p>
                </>
            )}
        </div>
    );
}
