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

// Linha de /dashboard/indicadores; valores em R$ e percentuais de 0 a 100
interface IndicadoresMotorista {
  motoristaId: string;
  utilizacao: number;
  disponibilidade: number;
  valorFrete: number;
  custoTotal: number;
  rentabilidade: number;
  rentabilidadeMediaViagem: number;
}

interface IndicadoresMes {
  motoristas: IndicadoresMotorista[];
}

type LinhaRanking = RankingItem & { indicadores?: IndicadoresMotorista };

type Status = 'loading' | 'success' | 'error';

function mesAtual() {
  const hoje = new Date();
  return `${hoje.getFullYear()}-${String(hoje.getMonth() + 1).padStart(2, '0')}`;
}

function formatarMoeda(valor: number | undefined) {
  return valor === undefined ? '—' : valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}

function formatarPercentual(valor: number | undefined) {
  return valor === undefined ? '—' : `${valor.toLocaleString('pt-BR', { maximumFractionDigits: 2 })}%`;
}

export function RankingMotoristas() {
  const [mes, setMes] = useState(mesAtual());
  const [itens, setItens] = useState<LinhaRanking[]>([]);
  const [status, setStatus] = useState<Status>('loading');
  const [tentativa, setTentativa] = useState(0);

  useEffect(() => {
    if (!mes) return;

    let cancelado = false;

    // A ordem vem do ranking (top 5 por viagens); os indicadores completam cada linha
    Promise.all([
      api.get<RankingItem[]>('/dashboard/ranking-motoristas', {
        params: { mesReferencia: mes, limite: 5 },
      }),
      api.get<IndicadoresMes>('/dashboard/indicadores', {
        params: { mesReferencia: mes },
      }),
    ])
      .then(([ranking, indicadores]) => {
        if (cancelado) return;
        const porMotorista = new Map(indicadores.data.motoristas.map((m) => [m.motoristaId, m]));
        setItens(ranking.data.map((item) => ({ ...item, indicadores: porMotorista.get(item.motoristaId) })));
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
                <th>Motorista</th>
                <th>Tipo de veículo</th>
                <th>Nº de viagens</th>
                <th>Disponibilidade</th>
                <th>Utilização</th>
                <th>Valor dos fretes</th>
                <th>Custos</th>
                <th>Rentabilidade</th>
                <th>Rentab. média por viagem</th>
              </tr>
            </thead>
            <tbody>
              {itens.map((item) => (
                <tr key={item.motoristaId}>
                  <td className="ranking-name">{item.nome ? item.nome.toLowerCase() : '—'}</td>
                  <td>{item.veiculo || '—'}</td>
                  <td>{item.totalViagens}</td>
                  <td>{formatarPercentual(item.indicadores?.disponibilidade)}</td>
                  <td>{formatarPercentual(item.indicadores?.utilizacao)}</td>
                  <td>{formatarMoeda(item.indicadores?.valorFrete)}</td>
                  <td>{formatarMoeda(item.indicadores?.custoTotal)}</td>
                  <td>{formatarMoeda(item.indicadores?.rentabilidade)}</td>
                  <td>{formatarMoeda(item.indicadores?.rentabilidadeMediaViagem)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
