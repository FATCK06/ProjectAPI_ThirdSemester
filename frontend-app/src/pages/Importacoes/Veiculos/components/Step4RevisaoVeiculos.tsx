import type { ResultadoValidacao, VeiculoPreview } from '../../../../services/importacao';
import { DataTable, type ColunaTabela } from '../../../../components/DataTable/DataTable';
import '../../Manifestos/components/step4.css';

function textoLongo(valor: string | null) {
    return <span title={valor ?? undefined}>{valor ?? '—'}</span>;
}

const COLUNAS: ColunaTabela<VeiculoPreview>[] = [
    { chave: 'placa', titulo: 'Placa', largura: 96, className: 'celula-mono', render: (v) => v.placa },
    { chave: 'marca', titulo: 'Marca', render: (v) => textoLongo(v.marca) },
    { chave: 'modelo', titulo: 'Modelo', render: (v) => textoLongo(v.modelo) },
    { chave: 'tipo', titulo: 'Tipo', render: (v) => textoLongo(v.tipoVeiculo) },
    { chave: 'ano', titulo: 'Ano fab.', largura: 80, render: (v) => v.anoFabricacao ?? '—' },
    {
        chave: 'cidade',
        titulo: 'Licenciamento',
        render: (v) => textoLongo(v.cidadeLicenciamento && `${v.cidadeLicenciamento} - ${v.ufLicenciamento}`),
    },
    { chave: 'agregado', titulo: 'Agregado', render: (v) => textoLongo(v.agregado) },
    { chave: 'status', titulo: 'Status', largura: 80, render: (v) => v.status ?? '—' },
];

export function Step4RevisaoVeiculos({ validacao }: { validacao: ResultadoValidacao | null }) {
    if (!validacao) {
        return <p className="revisao-vazio">Preparando a prévia...</p>;
    }
    // O backend manda a amostra no formato do tipo da importação.
    const amostra = validacao.amostra as unknown as VeiculoPreview[];

    return (
        <div className="revisao-wrap">
            <div className="revisao-cabecalho">
                <h4>Prévia dos dados</h4>
                <p>
                    Primeiros {amostra.length} de <strong>{validacao.linhasValidas}</strong> veículos que serão
                    gravados. Confira antes de enviar.
                </p>
            </div>

            <DataTable
                colunas={COLUNAS}
                linhas={amostra}
                chaveLinha={(linha) => String(linha.numeroLinha)}
                layoutFixo
                larguraMinima={760}
            />

            <p className="revisao-nota">
                Placa que já existe é <strong>atualizada</strong> só nos campos preenchidos no arquivo. Campo vazio
                não apaga o que já está cadastrado.
            </p>
        </div>
    );
}
