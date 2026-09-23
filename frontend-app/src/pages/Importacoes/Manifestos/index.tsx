import { useCallback, useState } from 'react';
import { Lock, Hourglass, CheckCircle2, ArrowLeft, ArrowRight, AlertCircle } from 'lucide-react';
import { Step1Upload } from './components/Step1Upload';
import { Step2Mapeamento } from './components/Step2Mapeamento';
import { Step3Validacao } from './components/Step3Validacao';
import { Step4Revisao } from './components/Step4Revisao';
import { Step5Execucao } from './components/Step5Execucao';
import { Step6Resultado } from './components/Step6Resultado';
import { ModalConfirmacao } from './components/ModalConfirmacao';
import {
    validarImportacao,
    mensagemDeErro,
    podeExecutar,
    type ImportacaoCriada,
    type ResultadoValidacao,
} from '../../../services/importacao';
import './manifestos.css';

const STEPS = [
    'Upload de Arquivo',
    'Mapeamento de Títulos de Colunas',
    'Validação de Dados',
    'Revisão/Preview',
    'Confirmação e execução',
    'Resultado'
];

const PASSO_UPLOAD = 1;
const PASSO_REVISAO = 4;
const PASSO_EXECUCAO = 5;
const PASSO_RESULTADO = 6;

export function ManifestosImport() {
    const [passoAtual, setPassoAtual] = useState(PASSO_UPLOAD);
    const [importacao, setImportacao] = useState<ImportacaoCriada | null>(null);
    const [validacao, setValidacao] = useState<ResultadoValidacao | null>(null);
    const [carregando, setCarregando] = useState(false);
    const [erro, setErro] = useState<string | null>(null);
    const [linhasGravadas, setLinhasGravadas] = useState(0);
    const [confirmando, setConfirmando] = useState(false);

    /** O parse roda no servidor a cada chamada e não é persistido — pode repetir à vontade. */
    async function carregarValidacao(id: number) {
        setCarregando(true);
        setErro(null);
        try {
            setValidacao(await validarImportacao(id));
            return true;
        } catch (falha) {
            setErro(mensagemDeErro(falha));
            return false;
        } finally {
            setCarregando(false);
        }
    }

    async function avancar() {
        if (passoAtual === PASSO_UPLOAD) {
            if (!importacao) return;
            const ok = await carregarValidacao(importacao.id);
            if (ok) setPassoAtual(2);
            return;
        }

        // A revisão é o último ponto antes de gravar: daqui o botão pede confirmação.
        if (passoAtual === PASSO_REVISAO) {
            setConfirmando(true);
            return;
        }

        setPassoAtual((p) => Math.min(p + 1, STEPS.length));
    }

    function voltar() {
        setErro(null);
        setPassoAtual((p) => Math.max(p - 1, PASSO_UPLOAD));
    }

    function recomecar() {
        setPassoAtual(PASSO_UPLOAD);
        setImportacao(null);
        setValidacao(null);
        setLinhasGravadas(0);
        setErro(null);
    }

    const concluirExecucao = useCallback((gravadas: number) => {
        setLinhasGravadas(gravadas);
        setPassoAtual(PASSO_RESULTADO);
    }, []);

    const falharExecucao = useCallback((mensagem: string) => {
        setErro(mensagem);
    }, []);

    const liberadoParaGravar = validacao !== null && podeExecutar(validacao);

    function rotuloBotao() {
        if (passoAtual === PASSO_REVISAO) return 'Enviar os Dados';
        return 'Continuar';
    }

    function podeAvancar() {
        if (carregando) return false;
        if (passoAtual === PASSO_UPLOAD) return importacao !== null;
        if (passoAtual === PASSO_REVISAO) return liberadoParaGravar;
        return passoAtual < STEPS.length;
    }

    function motivoBloqueio() {
        if (passoAtual === PASSO_UPLOAD && !importacao) return 'Envie um arquivo para continuar';
        if (passoAtual === PASSO_REVISAO && !liberadoParaGravar) {
            return 'Corrija os erros do arquivo antes de enviar';
        }
        return undefined;
    }

    // Durante a execução o usuário não age: a tela é só progresso.
    const executando = passoAtual === PASSO_EXECUCAO && erro === null;
    const mostrarRodape = passoAtual !== PASSO_RESULTADO && !executando;

    return (
        <div className="importacao-container">
            <div className="stepper-wrapper">
                {STEPS.map((step, index) => {
                    const stepNumber = index + 1;
                    const isActive = stepNumber === passoAtual;
                    const isConcluido = stepNumber < passoAtual;
                    const isPending = stepNumber > passoAtual;

                    return (
                        <div
                            key={step}
                            className={`step-item ${isActive ? 'active' : ''} ${isConcluido ? 'concluido' : ''} ${isPending ? 'pending' : ''}`}
                        >
                            <div className="step-icon-wrapper">
                                {isConcluido ? <CheckCircle2 size={20} /> : isActive ? <Hourglass size={20} /> : <Lock size={20} />}
                            </div>
                            <span className="step-title">{step}</span>
                            <span className="step-badge">
                                {isConcluido ? 'Concluído' : isActive ? 'Em progresso' : 'Pendente'}
                            </span>
                            {stepNumber < STEPS.length && <div className="step-connector"></div>}
                        </div>
                    );
                })}
            </div>

            {erro && passoAtual !== PASSO_EXECUCAO && (
                <div className="importacao-erro">
                    <AlertCircle size={18} />
                    <span>{erro}</span>
                </div>
            )}

            <div className="step-content">
                {passoAtual === 1 && <Step1Upload importacao={importacao} aoImportar={setImportacao} />}
                {passoAtual === 2 && <Step2Mapeamento validacao={validacao} />}
                {passoAtual === 3 && <Step3Validacao validacao={validacao} />}
                {passoAtual === 4 && <Step4Revisao validacao={validacao} />}
                {passoAtual === 5 && importacao && (
                    <Step5Execucao
                        importacaoId={importacao.id}
                        aoConcluir={concluirExecucao}
                        aoFalhar={falharExecucao}
                    />
                )}
                {passoAtual === 6 && importacao && (
                    <Step6Resultado
                        importacao={importacao}
                        validacao={validacao}
                        linhasGravadas={linhasGravadas}
                        aoRecomecar={recomecar}
                    />
                )}
            </div>

            {mostrarRodape && (
                <div className="step-footer">
                    <button className="btn-voltar" onClick={voltar} disabled={passoAtual === PASSO_UPLOAD}>
                        <ArrowLeft size={20} />
                        Anterior
                    </button>

                    <button
                        className={`btn-continuar ${passoAtual === PASSO_REVISAO ? 'btn-gravar' : ''}`}
                        onClick={avancar}
                        disabled={!podeAvancar()}
                        title={motivoBloqueio()}
                    >
                        {carregando ? 'Lendo arquivo...' : rotuloBotao()}
                        <ArrowRight size={20} />
                    </button>
                </div>
            )}

            {confirmando && validacao && (
                <ModalConfirmacao
                    titulo="Enviar os dados para o banco?"
                    mensagem={`${validacao.linhasValidas} viagens serão gravadas a partir de ${validacao.arquivoNome}. Até aqui nada foi gravado — esta é a etapa que grava.`}
                    textoConfirmar="Sim, enviar"
                    aoConfirmar={() => {
                        setConfirmando(false);
                        setErro(null);
                        setPassoAtual(PASSO_EXECUCAO);
                    }}
                    aoCancelar={() => setConfirmando(false)}
                />
            )}
        </div>
    );
}
