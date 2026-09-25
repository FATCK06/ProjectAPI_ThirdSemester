import { useCallback, useState } from 'react';
import { Lock, Hourglass, CheckCircle2, ArrowLeft, ArrowRight } from 'lucide-react';
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
    obrigatoriasFaltando,
    podeExecutar,
    type FiltroProblemas,
    type ImportacaoCriada,
    type ResultadoValidacao,
} from '../../../services/importacao';
import { useToast } from '../../../components/Toast';
import './manifestos.css';

const STEPS = [
    'Upload de Arquivo',
    'Mapeamento de Colunas',
    'Validação de Dados',
    'Revisão/Preview',
    'Confirmação e execução',
    'Resultado'
];

const PASSO_UPLOAD = 1;
const PASSO_MAPEAMENTO = 2;
const PASSO_VALIDACAO = 3;
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
    // Fica aqui, e não no passo 3, porque ele remonta ao ir e voltar: o filtro
    // precisa continuar batendo com a lista que está em `validacao`.
    const [filtroProblemas, setFiltroProblemas] = useState<FiltroProblemas>(null);
    const toast = useToast();

    /** O parse roda no servidor a cada chamada e não é persistido — pode repetir à vontade. */
    async function carregarValidacao(id: number, pagina = 0, filtro: FiltroProblemas = null) {
        setCarregando(true);
        try {
            setValidacao(await validarImportacao(id, pagina, filtro));
            setFiltroProblemas(filtro);
            return true;
        } catch (falha) {
            toast.erro(mensagemDeErro(falha), { titulo: 'Não foi possível conferir o arquivo' });
            return false;
        } finally {
            setCarregando(false);
        }
    }

    async function avancar() {
        if (passoAtual === PASSO_UPLOAD) {
            if (!importacao) return;
            // Vai para o passo 2 na hora e lê lá, com o Loading na tela. Limpar a
            // validação antiga garante que ele não mostre o arquivo anterior.
            setValidacao(null);
            setPassoAtual(PASSO_MAPEAMENTO);
            const ok = await carregarValidacao(importacao.id);
            // Sem leitura o passo 2 não tem o que mostrar: volta para o upload.
            if (!ok) setPassoAtual((p) => (p === PASSO_MAPEAMENTO ? PASSO_UPLOAD : p));
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
    const colunasOk = validacao !== null && obrigatoriasFaltando(validacao).length === 0;

    function rotuloBotao() {
        if (passoAtual === PASSO_REVISAO) return 'Enviar os Dados';
        return 'Continuar';
    }

    function podeAvancar() {
        if (carregando) return false;
        if (passoAtual === PASSO_UPLOAD) return importacao !== null;
        // Sem coluna obrigatória nenhuma linha passa: não adianta seguir para a validação.
        if (passoAtual === PASSO_MAPEAMENTO) return colunasOk;
        // Tudo ou nada: com erro, a prévia não serve para nada — o usuário fica onde vê os erros.
        if (passoAtual === PASSO_VALIDACAO || passoAtual === PASSO_REVISAO) return liberadoParaGravar;
        return passoAtual < STEPS.length;
    }

    function motivoBloqueio() {
        if (passoAtual === PASSO_UPLOAD && !importacao) return 'Envie um arquivo para continuar';
        if (passoAtual === PASSO_MAPEAMENTO && !colunasOk) {
            return 'Faltam colunas obrigatórias no arquivo — corrija e envie novamente';
        }
        if ((passoAtual === PASSO_VALIDACAO || passoAtual === PASSO_REVISAO) && !liberadoParaGravar) {
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

            <div className="step-content">
                {passoAtual === 1 && (
                    <Step1Upload
                        importacao={importacao}
                        aoImportar={setImportacao}
                        aoRemover={() => {
                            setImportacao(null);
                            setValidacao(null);
                        }}
                    />
                )}
                {passoAtual === 2 && <Step2Mapeamento validacao={validacao} />}
                {passoAtual === 3 && (
                    <Step3Validacao
                        validacao={validacao}
                        carregando={carregando}
                        filtro={filtroProblemas}
                        aoConsultar={(pagina, filtro) => importacao && carregarValidacao(importacao.id, pagina, filtro)}
                    />
                )}
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
