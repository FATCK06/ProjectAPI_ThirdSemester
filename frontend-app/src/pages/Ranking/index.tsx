import { useEffect, useMemo, useState } from 'react';
import { buscarRankingMensal, type RankingMensalMotorista } from '../../services/Ranking';
import { CardSkeleton } from '../../components/SkeletonLoader/CardSkeleton';
import './ranking.css';

type Coluna = keyof Pick<RankingMensalMotorista, 'motorista' | 'tipoVeiculo' | 'viagens' | 'disponibilidade' | 'utilizacao' | 'valorFretes' | 'custos' | 'rentabilidade' | 'rentabilidadeMediaViagem'>;

function mesAtual() {
  const hoje = new Date();
  return `${hoje.getFullYear()}-${String(hoje.getMonth() + 1).padStart(2, '0')}`;
}

function moeda(valor: number) {
  return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}

function percentual(valor: number) {
  return `${valor.toLocaleString('pt-BR', { maximumFractionDigits: 2 })}%`;
}

export function Ranking() {
  const [mes, setMes] = useState(mesAtual);
  const [linhas, setLinhas] = useState<RankingMensalMotorista[]>([]);
  const [coluna, setColuna] = useState<Coluna>('rentabilidade');
  const [direcao, setDirecao] = useState<'asc' | 'desc'>('desc');
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState(false);
  const [tentativa, setTentativa] = useState(0);

  useEffect(() => {
    let cancelado = false;
    setCarregando(true);
    setErro(false);
    buscarRankingMensal(mes)
      .then((dados) => { if (!cancelado) setLinhas(dados); })
      .catch(() => { if (!cancelado) setErro(true); })
      .finally(() => { if (!cancelado) setCarregando(false); });
    return () => { cancelado = true; };
  }, [mes, tentativa]);

  const ordenadas = useMemo(() => [...linhas].sort((a, b) => {
    const primeiro = a[coluna];
    const segundo = b[coluna];
    const resultado = typeof primeiro === 'string' || primeiro === null
      ? String(primeiro ?? '').localeCompare(String(segundo ?? ''), 'pt-BR')
      : Number(primeiro) - Number(segundo);
    return direcao === 'asc' ? resultado : -resultado;
  }), [linhas, coluna, direcao]);

  function ordenar(novaColuna: Coluna) {
    if (coluna === novaColuna) setDirecao((atual) => atual === 'asc' ? 'desc' : 'asc');
    else { setColuna(novaColuna); setDirecao('asc'); }
  }

  function cabecalho(texto: string, chave: Coluna) {
    return <th><button type="button" onClick={() => ordenar(chave)}>{texto}<span>{coluna === chave ? (direcao === 'asc' ? ' ↑' : ' ↓') : ' ↕'}</span></button></th>;
  }

  return (
    <div className="ranking-page">
      <header className="ranking-page-header">
        <div><h1>Ranking mensal</h1><p>Compare o desempenho dos motoristas no período selecionado.</p></div>
        <label htmlFor="ranking-page-mes">Mês de referência
          <input id="ranking-page-mes" type="month" value={mes} onChange={(event) => setMes(event.target.value)} />
        </label>
      </header>

      {carregando && <div className="ranking-page-skeleton"><CardSkeleton isLoading /></div>}
      {!carregando && erro && <div className="ranking-page-feedback"><p>Não foi possível carregar o ranking.</p><button type="button" onClick={() => setTentativa((valor) => valor + 1)}>Tentar novamente</button></div>}
      {!carregando && !erro && ordenadas.length === 0 && <div className="ranking-page-feedback"><p>Nenhum dado importado para este mês.</p></div>}
      {!carregando && !erro && ordenadas.length > 0 && (
        <div className="ranking-page-table-wrap"><table className="ranking-page-table"><thead><tr>
          {cabecalho('Motorista', 'motorista')}{cabecalho('Tipo de veículo', 'tipoVeiculo')}{cabecalho('Viagens', 'viagens')}
          {cabecalho('Disponibilidade', 'disponibilidade')}{cabecalho('Utilização', 'utilizacao')}{cabecalho('Valor dos fretes', 'valorFretes')}
          {cabecalho('Custos', 'custos')}{cabecalho('Rentabilidade', 'rentabilidade')}{cabecalho('Rentabilidade média', 'rentabilidadeMediaViagem')}
        </tr></thead><tbody>{ordenadas.map((linha) => <tr key={linha.motoristaId}>
          <td className="ranking-page-name">{linha.motorista || '—'}</td><td>{linha.tipoVeiculo || '—'}</td><td>{linha.viagens}</td>
          <td>{percentual(linha.disponibilidade)}</td><td>{percentual(linha.utilizacao)}</td><td>{moeda(linha.valorFretes)}</td>
          <td>{moeda(linha.custos)}</td><td>{moeda(linha.rentabilidade)}</td><td>{moeda(linha.rentabilidadeMediaViagem)}</td>
        </tr>)}</tbody></table></div>
      )}
    </div>
  );
}
