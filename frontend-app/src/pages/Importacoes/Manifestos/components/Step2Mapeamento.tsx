import { CheckCircle2, AlertCircle, ArrowRight } from 'lucide-react';
import type { MapeamentoColuna, ResultadoValidacao } from '../../../../services/importacao';
import { Loading } from '../../../../components/Loading';
import './step2.css';

interface PropsPasso2 {
    validacao: ResultadoValidacao | null;
}

/** 0 = obrigatória ausente, 1 = opcional ausente, 2 = reconhecida. */
function prioridade(linha: MapeamentoColuna): number {
    if (linha.encontrada) return 2;
    return linha.obrigatoria ? 0 : 1;
}

export function Step2Mapeamento({ validacao }: PropsPasso2) {
    if (!validacao) {
        return <Loading texto="Lendo o arquivo" detalhe="Conferindo as colunas da planilha." />;
    }

    const encontradas = validacao.mapeamento.filter((m) => m.encontrada).length;
    const faltandoObrigatoria = validacao.mapeamento.filter((m) => m.obrigatoria && !m.encontrada);
    // Problemas primeiro: o que impede a importação não pode ficar escondido no fim da lista.
    // O sort é estável, então dentro de cada grupo vale a ordem do sistema.
    const linhas = [...validacao.mapeamento].sort((a, b) => prioridade(a) - prioridade(b));

    return (
        <div className="mapeamento-wrap">
            <div className="mapeamento-resumo">
                <span>
                    <strong>{encontradas}</strong> de {validacao.mapeamento.length} colunas reconhecidas
                </span>
                <span className="mapeamento-separador">·</span>
                <span>{validacao.colunasDetectadas.length} colunas no arquivo</span>
            </div>

            {faltandoObrigatoria.length > 0 && (
                <div className="mapeamento-alerta">
                    <AlertCircle size={18} />
                    <span>
                        Faltam colunas obrigatórias:{' '}
                        <strong>{faltandoObrigatoria.map((m) => m.campoSistema).join(', ')}</strong>.
                        Sem elas o arquivo não pode ser importado.
                    </span>
                </div>
            )}

            <div className="mapeamento-cartao">
                <div className="mapeamento-grade mapeamento-cabecalho">
                    <div className="mapeamento-esquerda">
                        <span className="mapeamento-status">Status</span>
                        <span className="mapeamento-titulo-destino">Campo do Sistema</span>
                    </div>
                    <span />
                    <div className="mapeamento-direita">Coluna na sua planilha</div>
                </div>

                <div className="mapeamento-rolagem">
                    {linhas.map((linha) => (
                        <div key={linha.campoSistema} className="mapeamento-grade mapeamento-linha">
                            <div className="mapeamento-esquerda">
                                <div className="mapeamento-status">
                                    {linha.encontrada ? (
                                        <CheckCircle2 size={18} className="icone-ok" />
                                    ) : (
                                        <AlertCircle
                                            size={18}
                                            className={linha.obrigatoria ? 'icone-erro' : 'icone-aviso'}
                                        />
                                    )}
                                </div>
                                <div className="mapeamento-destino">
                                    {linha.campoSistema}
                                    {linha.obrigatoria && <span className="mapeamento-obrigatorio">*</span>}
                                </div>
                            </div>
    
                            <div className="mapeamento-seta">
                                <ArrowRight size={16} className="seta-h" />
                            </div>
    
                            <div className="mapeamento-direita">
                                <div
                                    className={`mapeamento-caixa ${
                                        linha.encontrada ? '' : linha.obrigatoria ? 'caixa-erro' : 'caixa-ausente'
                                    }`}
                                    title={linha.colunaArquivo ?? undefined}
                                >
                                    {linha.encontrada ? linha.colunaArquivo ?? linha.campoSistema : 'não encontrada'}
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            <p className="mapeamento-nota">
                Colunas opcionais ausentes não impedem a importação — os campos ficam vazios.
                O <strong>*</strong> marca as obrigatórias.
            </p>
        </div>
    );
}
