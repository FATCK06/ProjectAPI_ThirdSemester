import { useEffect, useMemo, useState } from 'react';
import { buscarRankingMensal, type RankingMensalMotorista } from '../../../services/Ranking';
import { CardSkeleton } from '../../../components/SkeletonLoader/CardSkeleton';
import { Aviso, AVISO_CUSTOS_NAO_INFORMADOS, AVISO_SEM_PERMISSAO_FINANCEIRO, AVISO_SEM_VIAGENS_MEDIA } from '../../../components/Aviso/Aviso';
import { podeVerDadosFinanceiros } from '../../../services/auth';
import { mesAtual } from '../../../utils/datas';
import { formatarMoeda, formatarPercentual, SEM_VALOR } from '../../../utils/formatacao';
import './ranking.css';

type Coluna = Exclude<keyof RankingMensalMotorista, 'motoristaId'>;

export function Ranking() {
  const financeiro = podeVerDadosFinanceiros();
  const [mes, setMes] = useState(mesAtual);
  const [linhas, setLinhas] = useState<RankingMensalMotorista[]>([]);
  const [coluna, setColuna] = useState<Coluna>(financeiro ? 'rentabilidade' : 'numeroViagens');
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

  // Valor ausente (média sem viagem apurável) fica no fim nas duas direções.
  const ordenadas = useMemo(() => [...linhas].sort((a, b) => {
    const primeiro = a[coluna];
    const segundo = b[coluna];
    if (primeiro === undefined || primeiro === null) return segundo === undefined || segundo === null ? 0 : 1;
    if (segundo === undefined || segundo === null) return -1;
    const resultado = typeof primeiro === 'string'
      ? primeiro.localeCompare(String(segundo), 'pt-BR')
      : Number(primeiro) - Number(segundo);
    return direcao === 'asc' ? resultado : -resultado;
  }), [linhas, coluna, direcao]);

  // Mês sem nenhuma viagem apurável: o back soma custo zero para todos.
  const custosNaoInformados = financeiro && linhas.length > 0 && linhas.every((linha) => !linha.custoTotal);

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

      {!financeiro && <Aviso mensagem={AVISO_SEM_PERMISSAO_FINANCEIRO} />}
      {!carregando && !erro && custosNaoInformados && <Aviso mensagem={AVISO_CUSTOS_NAO_INFORMADOS} />}

      {carregando && <div className="ranking-page-skeleton"><CardSkeleton isLoading /></div>}
      {!carregando && erro && <div className="ranking-page-feedback"><p>Não foi possível carregar o ranking.</p><button type="button" onClick={() => setTentativa((valor) => valor + 1)}>Tentar novamente</button></div>}
      {!carregando && !erro && ordenadas.length === 0 && <div className="ranking-page-feedback"><p>Nenhum dado importado para este mês.</p></div>}
      {!carregando && !erro && ordenadas.length > 0 && (
        <div className="ranking-page-table-wrap"><table className="ranking-page-table"><thead><tr>
          {cabecalho('Motorista', 'nome')}{cabecalho('Tipo de veículo', 'tipoVeiculo')}{cabecalho('Viagens', 'numeroViagens')}
          {cabecalho('Disponibilidade', 'disponibilidade')}{cabecalho('Utilização', 'utilizacao')}
          {financeiro && <>{cabecalho('Valor dos fretes', 'valorFrete')}{cabecalho('Custos', 'custoTotal')}{cabecalho('Rentabilidade', 'rentabilidade')}{cabecalho('Rentabilidade média', 'rentabilidadeMediaViagem')}</>}
        </tr></thead><tbody>{ordenadas.map((linha) => <tr key={linha.motoristaId}>
          <td className="ranking-page-name">{linha.nome || SEM_VALOR}</td><td>{linha.tipoVeiculo || SEM_VALOR}</td><td>{linha.numeroViagens}</td>
          <td>{formatarPercentual(linha.disponibilidade)}</td><td>{formatarPercentual(linha.utilizacao)}</td>
          {financeiro && <><td>{formatarMoeda(linha.valorFrete)}</td><td>{formatarMoeda(linha.custoTotal)}</td><td>{formatarMoeda(linha.rentabilidade)}</td>
            {linha.rentabilidadeMediaViagem === undefined
              ? <td className="ranking-page-sem-valor" title={AVISO_SEM_VIAGENS_MEDIA} aria-label={AVISO_SEM_VIAGENS_MEDIA}>{SEM_VALOR}</td>
              : <td>{formatarMoeda(linha.rentabilidadeMediaViagem)}</td>}</>}
        </tr>)}</tbody></table></div>
      )}
    </div>
  );
}
