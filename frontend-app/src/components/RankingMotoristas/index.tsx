import { useEffect, useState } from 'react';
import api from '../../services/api';
import '../SkeletonLoader/skeleton.css';
import './ranking.css';

interface RankingItem {
  posicao: number;
  motoristaId: string;
  nome: string | null;
  cpf: string | null;
  totalViagens: number;
  veiculo: string | null;
  distanciaMaximaKm: number | null;
}

type Status = 'loading' | 'success' | 'error';

function mesAtual() {
  const hoje = new Date();
  return `${hoje.getFullYear()}-${String(hoje.getMonth() + 1).padStart(2, '0')}`;
}

function formatarDocumento(valor: string | null) {
  if (!valor) return '—';
  const d = valor.replace(/\D/g, '');
  if (d.length === 11) return d.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
  if (d.length === 14) return d.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, '$1.$2.$3/$4-$5');
  return valor;
}

function formatarKm(valor: number | null) {
  return valor === null ? '—' : `${valor.toLocaleString('pt-BR')} km`;
}

export function RankingMotoristas() {
  const [mes, setMes] = useState(mesAtual());
  const [itens, setItens] = useState<RankingItem[]>([]);
  const [status, setStatus] = useState<Status>('loading');
  const [tentativa, setTentativa] = useState(0);

  useEffect(() => {
    if (!mes) return;

    let cancelado = false;

    api
      .get<RankingItem[]>('/dashboard/ranking-motoristas', {
        params: { mesReferencia: mes, limite: 5 },
      })
      .then((res) => {
        if (cancelado) return;
        setItens(res.data);
        setStatus('success');
      })
      .catch(() => {
        if (cancelado) return;
        setStatus('error');
      });

    return () => {
      cancelado = true;
    };
  }, [mes, tentativa]);

  return (
    <section className="ranking-card">
      <div className="ranking-header">
        <div>
          <h2 className="ranking-title">Ranking de Motoristas</h2>
          <p className="ranking-subtitle">Top 5 por quantidade de viagens</p>
        </div>

        <div className="ranking-filter">
          <label htmlFor="ranking-mes">Mês</label>
          <input
            id="ranking-mes"
            type="month"
            value={mes}
            onChange={(e) => {
              setStatus('loading');
              setMes(e.target.value);
            }}
          />
        </div>
      </div>

      {status === 'loading' && <div className="skeleton-base shimmer" style={{ height: '180px' }} />}

      {status === 'error' && (
        <div className="ranking-feedback">
          <p>Não foi possível carregar o ranking.</p>
          <button
            type="button"
            onClick={() => {
              setStatus('loading');
              setTentativa((t) => t + 1);
            }}
          >
            Tentar novamente
          </button>
        </div>
      )}

      {status === 'success' && itens.length === 0 && (
        <div className="ranking-feedback">
          <p>Nenhuma viagem importada para este mês.</p>
        </div>
      )}

      {status === 'success' && itens.length > 0 && (
        <div className="ranking-table-wrap">
          <table className="ranking-table">
            <thead>
              <tr>
                <th>#</th>
                <th>Motorista</th>
                <th>Viagens</th>
                <th>Veículo</th>
                <th>Viagem mais longa</th>
                <th>CPF</th>
              </tr>
            </thead>
            <tbody>
              {itens.map((item) => (
                <tr key={item.motoristaId}>
                  <td>
                    <span className={`ranking-position pos-${item.posicao}`}>{item.posicao}º</span>
                  </td>
                  <td className="ranking-name">{item.nome ? item.nome.toLowerCase() : '—'}</td>
                  <td>{item.totalViagens}</td>
                  <td>{item.veiculo || '—'}</td>
                  <td>{formatarKm(item.distanciaMaximaKm)}</td>
                  <td>{formatarDocumento(item.cpf)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
