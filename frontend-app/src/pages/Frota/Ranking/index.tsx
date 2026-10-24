import { useEffect, useState } from 'react';
import { buscarRankingMensal, type RankingMensalMotorista } from '../../../services/Ranking';
import { TableSkeleton } from '../../../components/SkeletonLoader/TableSkeleton';
import { Aviso, AVISO_CUSTOS_NAO_INFORMADOS, AVISO_SEM_PERMISSAO_FINANCEIRO, AVISO_SEM_VIAGENS_MEDIA } from '../../../components/Aviso/Aviso';
import { podeVerDadosFinanceiros } from '../../../services/auth';
import { mesAtual } from '../../../utils/datas';
import { formatarMoeda, formatarPercentual, SEM_VALOR } from '../../../utils/formatacao';
import './ranking.css';
import { PageHeader } from '../../../components/PageHeader';
import { DataTable, type ColunaTabela } from '../../../components/DataTable/DataTable';

type Linha = RankingMensalMotorista;
const NAO_INFORMADO = 'N\u00e3o informado';

const COLUNAS_OPERACIONAIS: ColunaTabela<Linha>[] = [
  { chave: 'nome', titulo: 'Motorista', render: (l) => l.nome || l.motorista || NAO_INFORMADO, ordenarPor: (l) => l.nome || l.motorista },
  { chave: 'tipoVeiculo', titulo: 'Tipo de veículo', render: (l) => l.tipoVeiculo || NAO_INFORMADO, ordenarPor: (l) => l.tipoVeiculo },
  { chave: 'numeroViagens', titulo: 'N\u00famero de Viagens', render: (l) => l.numeroViagens, ordenarPor: (l) => l.numeroViagens },
  { chave: 'utilizacao', titulo: 'Utilização', render: (l) => formatarPercentual(l.utilizacao), ordenarPor: (l) => l.utilizacao },
];

// Só para quem tem VER_DADOS_FINANCEIROS (o backend já manda esses campos nulos para os demais)
const COLUNAS_FINANCEIRAS: ColunaTabela<Linha>[] = [
  { chave: 'valorFrete', titulo: 'Valor dos fretes', render: (l) => formatarMoeda(l.valorFrete), ordenarPor: (l) => l.valorFrete },
  { chave: 'custoTotal', titulo: 'Custos', render: (l) => formatarMoeda(l.custoTotal), ordenarPor: (l) => l.custoTotal },
  { chave: 'rentabilidade', titulo: 'Rentabilidade', render: (l) => formatarMoeda(l.rentabilidade), ordenarPor: (l) => l.rentabilidade },
  {
    chave: 'rentabilidadeMediaViagem',
    titulo: 'Rentabilidade média',
    // Sem viagem apurável a média vem ausente: o DataTable a deixa no fim nas duas direções
    render: (l) => l.rentabilidadeMediaViagem === undefined
      ? <span className="ranking-page-sem-valor" title={AVISO_SEM_VIAGENS_MEDIA} aria-label={AVISO_SEM_VIAGENS_MEDIA}>{SEM_VALOR}</span>
      : formatarMoeda(l.rentabilidadeMediaViagem),
    ordenarPor: (l) => l.rentabilidadeMediaViagem,
  },
];

export function Ranking() {
  const financeiro = podeVerDadosFinanceiros();
  const [mes, setMes] = useState(mesAtual);
  const [linhas, setLinhas] = useState<Linha[]>([]);
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

  // Mês sem nenhuma viagem apurável: o back soma custo zero para todos.
  const custosNaoInformados = financeiro && linhas.length > 0 && linhas.every((linha) => !linha.custoTotal);
  const colunas = financeiro ? [...COLUNAS_OPERACIONAIS, ...COLUNAS_FINANCEIRAS] : COLUNAS_OPERACIONAIS;

  return (
    <div className="ranking-page">
      <PageHeader titulo="Ranking Mensal" subtitulo="Compare o desempenho dos motoristas no período selecionado">
        <label htmlFor="ranking-page-mes">Mês de referência
          <input id="ranking-page-mes" type="month" value={mes} onChange={(event) => setMes(event.target.value)} />
        </label>
      </PageHeader>

      {!financeiro && <Aviso mensagem={AVISO_SEM_PERMISSAO_FINANCEIRO} />}
      {!carregando && !erro && custosNaoInformados && <Aviso mensagem={AVISO_CUSTOS_NAO_INFORMADOS} />}

      {carregando && <TableSkeleton linhas={10} colunas={colunas.length} />}
      {!carregando && erro && <div className="ranking-page-feedback"><p>Não foi possível carregar o ranking.</p><button type="button" onClick={() => setTentativa((valor) => valor + 1)}>Tentar novamente</button></div>}
      {!carregando && !erro && linhas.length === 0 && <div className="ranking-page-feedback"><p>Nenhum dado importado para este mês.</p></div>}
      {!carregando && !erro && linhas.length > 0 && (
        <DataTable
          colunas={colunas}
          linhas={linhas}
          chaveLinha={(linha) => linha.motoristaId}
          ordenacaoInicial={{ chave: financeiro ? 'rentabilidade' : 'numeroViagens', direcao: 'desc' }}
          cardsNoMobile
          itensPorPagina={20}
        />
      )}
    </div>
  );
}
