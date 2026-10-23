import type { ResultadoValidacao } from '../../../../services/importacao';
import { DataTable, type ColunaTabela } from '../../../../components/DataTable/DataTable';
import { formatarMoeda } from '../../../../utils/formatacao';
import './step4.css';

type LinhaAmostra = ResultadoValidacao['amostra'][number];

interface PropsPasso4 {
    validacao: ResultadoValidacao | null;
}

function formatarData(iso: string | null): string {
    if (!iso) return '-';
    const [ano, mes, dia] = iso.split('-');
    return `${dia}/${mes}/${ano}`;
}

/** Mostra só os 3 primeiros dígitos: a tela não precisa do CPF inteiro. */
function mascararCpf(cpf: string | null): string {
    if (!cpf) return '-';
    return `${cpf.slice(0, 3)}.***.***-**`;
}

/** O title mostra o texto inteiro quando a célula corta com reticências. */
function textoLongo(valor: string | null) {
    return <span title={valor ?? undefined}>{valor ?? '—'}</span>;
}

// Larguras fixas nas colunas curtas; as de texto dividem o resto
const COLUNAS: ColunaTabela<LinhaAmostra>[] = [
    { chave: 'manifesto', titulo: 'Manifesto', largura: 96, render: (l) => l.manifesto ?? '—' },
    { chave: 'data', titulo: 'Data', largura: 96, render: (l) => formatarData(l.data) },
    { chave: 'mes', titulo: 'Mês', largura: 80, render: (l) => l.mesReferencia ?? '—' },
    { chave: 'motorista', titulo: 'Motorista', render: (l) => textoLongo(l.motorista) },
    { chave: 'cpf', titulo: 'CPF', largura: 128, className: 'celula-mono', render: (l) => mascararCpf(l.cpf) },
    { chave: 'agregado', titulo: 'Agregado', render: (l) => textoLongo(l.agregado) },
    { chave: 'veiculo', titulo: 'Veículo', largura: 96, className: 'celula-mono', render: (l) => l.veiculo ?? '—' },
    { chave: 'destino', titulo: 'Destino', render: (l) => textoLongo(l.destino) },
    { chave: 'valor', titulo: 'Frete agregado', largura: 116, alinhamento: 'direita', render: (l) => formatarMoeda(l.valorFrete) },
];

export function Step4Revisao({ validacao }: PropsPasso4) {
    if (!validacao) {
        return <p className="revisao-vazio">Preparando a prévia...</p>;
    }

    return (
        <div className="revisao-wrap">
            <div className="revisao-cabecalho">
                <h4>Prévia dos dados</h4>
                <p>
                    Primeiras {validacao.amostra.length} de <strong>{validacao.linhasValidas}</strong> linhas
                    que serão importadas. Confira antes de enviar.
                </p>
            </div>

            {/* 9 colunas: cabem na largura da tela cortando os textos longos; a rolagem só aparece em telas estreitas */}
            <DataTable
                colunas={COLUNAS}
                linhas={validacao.amostra}
                chaveLinha={(linha) => String(linha.numeroLinha)}
                layoutFixo
                larguraMinima={760}
            />

            <p className="revisao-nota">
                A coluna <strong>Mês</strong> vem da data de cada linha, não do arquivo. Por isso, um
                arquivo trimestral gera viagens em meses diferentes.
            </p>
        </div>
    );
}
