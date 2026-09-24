import type { ResultadoValidacao } from '../../../../services/importacao';
import './step4.css';

interface PropsPasso4 {
    validacao: ResultadoValidacao | null;
}

function formatarData(iso: string | null): string {
    if (!iso) return '—';
    const [ano, mes, dia] = iso.split('-');
    return `${dia}/${mes}/${ano}`;
}

function formatarMoeda(valor: number | null): string {
    if (valor === null || valor === undefined) return '—';
    return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}

/** Mostra só os 3 primeiros dígitos: a tela não precisa do CPF inteiro. */
function mascararCpf(cpf: string | null): string {
    if (!cpf) return '—';
    return `${cpf.slice(0, 3)}.***.***-**`;
}

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

            <div className="revisao-tabela-area">
                <table className="revisao-tabela">
                    {/* Larguras fixas nas colunas curtas; as de texto dividem o resto e cortam com reticências */}
                    <colgroup>
                        <col className="col-manifesto" />
                        <col className="col-data" />
                        <col className="col-mes" />
                        <col />
                        <col className="col-cpf" />
                        <col />
                        <col className="col-veiculo" />
                        <col />
                        <col className="col-valor" />
                    </colgroup>
                    <thead>
                        <tr>
                            <th>Manifesto</th>
                            <th>Data</th>
                            <th>Mês</th>
                            <th>Motorista</th>
                            <th>CPF</th>
                            <th>Agregado</th>
                            <th>Veículo</th>
                            <th>Destino</th>
                            <th className="alinhar-direita">Valor frete</th>
                        </tr>
                    </thead>
                    <tbody>
                        {validacao.amostra.map((linha) => (
                            <tr key={linha.numeroLinha}>
                                <td className="celula-forte">{linha.manifesto ?? '—'}</td>
                                <td>{formatarData(linha.data)}</td>
                                <td>{linha.mesReferencia ?? '—'}</td>
                                <td title={linha.motorista ?? undefined}>{linha.motorista ?? '—'}</td>
                                <td className="celula-mono">{mascararCpf(linha.cpf)}</td>
                                <td title={linha.agregado ?? undefined}>{linha.agregado ?? '—'}</td>
                                <td className="celula-mono">{linha.veiculo ?? '—'}</td>
                                <td title={linha.destino ?? undefined}>{linha.destino ?? '—'}</td>
                                <td className="alinhar-direita">{formatarMoeda(linha.valorFrete)}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            <p className="revisao-nota">
                A coluna <strong>Mês</strong> vem da data de cada linha, não do arquivo — por isso um
                arquivo trimestral gera viagens em meses diferentes.
            </p>
        </div>
    );
}
