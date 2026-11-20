import { useEffect, useMemo, useRef, useState } from 'react';
import { useLocation, useSearchParams } from 'react-router-dom';
import { baixarRankingMensalCsv, buscarRankingMensal, type RankingMensalMotorista } from '../../../services/Ranking';
import { TableSkeleton } from '../../../components/SkeletonLoader/TableSkeleton';
import { Aviso, AVISO_CUSTOS_NAO_INFORMADOS, AVISO_SEM_PERMISSAO_FINANCEIRO, AVISO_SEM_VIAGENS_MEDIA } from '../../../components/Aviso/Aviso';
import { podeVerDadosFinanceiros } from '../../../services/auth';
import { CampoMes } from '../../../components/CampoMes/CampoMes';
import { Loading } from '../../../components/Loading';
import { SeloMes } from '../../../components/SeloMes/SeloMes';
import { useMesDaUrl } from '../../../hooks/useMesDaUrl';
import { mesAtual } from '../../../utils/datas';
import { formatarMoeda, formatarPercentual, SEM_VALOR } from '../../../utils/formatacao';
import './ranking.css';
import { PageHeader } from '../../../components/PageHeader';
import { DataTable, type ColunaTabela, type OrdenacaoTabela } from '../../../components/DataTable/DataTable';
import { ExportarCsvButton } from '../../../components/ExportarCsvButton/ExportarCsvButton';

type Linha = RankingMensalMotorista;
type RestauracaoTabela = {
  pagina?: number;
  ordenacao?: OrdenacaoTabela | null;
  scrollY?: number;
};
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

// Sem ?mes na URL, espera o mês mais recente com dados antes de montar a tela
export function Ranking() {
  const mes = useMesDaUrl();
  return mes ? <RankingDoMes mes={mes} /> : <Loading texto="Carregando mês" />;
}

function RankingDoMes({ mes }: { mes: string }) {
  const location = useLocation();
  const restauracao = (location.state as { restauracao?: RestauracaoTabela } | null)?.restauracao;
  const [, setSearchParams] = useSearchParams();
  const financeiro = podeVerDadosFinanceiros();
  // Controlada aqui porque a exportação CSV precisa seguir a ordenação da tela
  const [ordenacao, setOrdenacao] = useState<OrdenacaoTabela>(
    restauracao?.ordenacao ?? { chave: financeiro ? 'rentabilidade' : 'numeroViagens', direcao: 'desc' },
  );
  const [tentativa, setTentativa] = useState(0);
  // Resultado com a chave do pedido: chave diferente da atual = ainda carregando
  const [resultado, setResultado] = useState<{ chave: string; linhas: Linha[] } | { chave: string; erro: true } | null>(null);
  const chave = `${mes}|${tentativa}`;
  const carregando = resultado?.chave !== chave;
  const erro = !carregando && resultado !== null && 'erro' in resultado;
  const linhas = useMemo(
    () => (resultado?.chave === chave && 'linhas' in resultado ? resultado.linhas : []),
    [resultado, chave],
  );
  const scrollRestaurado = useRef(false);

  useEffect(() => {
    let cancelado = false;
    buscarRankingMensal(mes)
      .then((dados) => { if (!cancelado) setResultado({ chave, linhas: dados }); })
      .catch(() => { if (!cancelado) setResultado({ chave, erro: true }); });
    return () => { cancelado = true; };
  }, [mes, chave]);

  useEffect(() => {
    if (carregando || restauracao?.scrollY === undefined || scrollRestaurado.current) return;
    scrollRestaurado.current = true;
    requestAnimationFrame(() => window.scrollTo(0, restauracao.scrollY ?? 0));
  }, [carregando, restauracao]);

  // Mês sem nenhuma viagem apurável: o back soma custo zero para todos.
  const custosNaoInformados = financeiro && linhas.length > 0 && linhas.every((linha) => !linha.custoTotal);
  const colunas = financeiro ? [...COLUNAS_OPERACIONAIS, ...COLUNAS_FINANCEIRAS] : COLUNAS_OPERACIONAIS;

  return (
    <div className="ranking-page" data-tour="ranking-pagina">
      <PageHeader titulo="Ranking Mensal" selo={<SeloMes mes={mes} />} subtitulo="Compare o desempenho dos motoristas no período selecionado">
        <div className="ranking-page-mes" data-tour="ranking-mes">
          <label htmlFor="ranking-page-mes">Mês de referência</label>
          <CampoMes id="ranking-page-mes" valor={mes} max={mesAtual()} onChange={(novoMes) => {
            setSearchParams((atual) => {
              const parametros = new URLSearchParams(atual);
              parametros.set('mes', novoMes);
              return parametros;
            });
          }} />
        </div>
        <ExportarCsvButton
          nomeArquivo={`ranking_${mes.replace("-", "_")}.csv`}
          baixarCsv={(aoProgredir) => baixarRankingMensalCsv(
            mes,
            ordenacao.chave,
            ordenacao.direcao,
            aoProgredir,
          )}
          disabled={carregando || erro || linhas.length === 0}
        />
      </PageHeader>

      {!financeiro && (
        <div data-tour="ranking-aviso-financeiro">
          <Aviso mensagem={AVISO_SEM_PERMISSAO_FINANCEIRO} />
        </div>
      )}
      {!carregando && !erro && custosNaoInformados && <Aviso mensagem={AVISO_CUSTOS_NAO_INFORMADOS} />}

      {carregando && <TableSkeleton linhas={10} colunas={colunas.length} />}
      {!carregando && erro && <div className="ranking-page-feedback"><p>Não foi possível carregar o ranking.</p><button type="button" onClick={() => setTentativa((valor) => valor + 1)}>Tentar novamente</button></div>}
      {!carregando && !erro && linhas.length === 0 && <div className="ranking-page-feedback"><p>Nenhum dado importado para este mês.</p></div>}
      {!carregando && !erro && linhas.length > 0 && (
        <DataTable
          colunas={colunas}
          linhas={linhas}
          chaveLinha={(linha) => linha.motoristaId}
          // O nome leva à ficha no mesmo mês; o "Voltar" de lá restaura página, ordem e rolagem
          linkLinha={(linha) => `/motoristas/${linha.motoristaId}?mes=${encodeURIComponent(mes)}`}
          estadoNavegacaoLinha={(_, estado) => ({
            retorno: {
              destino: `/ranking?mes=${encodeURIComponent(mes)}`,
              rotulo: 'Voltar para o Ranking Mensal',
              restauracao: { ...estado, scrollY: window.scrollY },
            },
          })}
          ordenacao={ordenacao}
          aoOrdenar={setOrdenacao}
          paginaInicial={restauracao?.pagina ?? 1}
          cardsNoMobile
          itensPorPagina={20}
        />
      )}
    </div>
  );
}
