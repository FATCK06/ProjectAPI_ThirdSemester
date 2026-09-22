import { useEffect, useRef, useState } from 'react';
import { CheckCircle2, AlertCircle } from 'lucide-react';
import { executarImportacao, mensagemDeErro } from '../../../../services/importacao';
import './step5.css';

/** Tempo que o check fica visível antes de avançar, só para o usuário ver que deu certo. */
const PAUSA_APOS_SUCESSO_MS = 1200;

type Situacao = 'enviando' | 'sucesso' | 'erro';

interface PropsPasso5 {
    importacaoId: number;
    aoConcluir: (linhasGravadas: number) => void;
    aoFalhar: (mensagem: string) => void;
}

export function Step5Execucao({ importacaoId, aoConcluir, aoFalhar }: PropsPasso5) {
    const [situacao, setSituacao] = useState<Situacao>('enviando');
    const [erro, setErro] = useState<string | null>(null);
    // StrictMode monta o componente duas vezes em desenvolvimento; sem esta trava
    // a importação seria disparada em duplicidade.
    const jaDisparou = useRef(false);

    useEffect(() => {
        if (jaDisparou.current) return;
        jaDisparou.current = true;

        let cancelado = false;

        (async () => {
            try {
                const resposta = await executarImportacao(importacaoId);
                if (cancelado) return;

                setSituacao('sucesso');
                setTimeout(() => {
                    if (!cancelado) aoConcluir(resposta.linhasGravadas);
                }, PAUSA_APOS_SUCESSO_MS);
            } catch (falha) {
                if (cancelado) return;

                const mensagem = mensagemDeErro(falha);
                setSituacao('erro');
                setErro(mensagem);
                aoFalhar(mensagem);
            }
        })();

        return () => {
            cancelado = true;
        };
    }, [importacaoId, aoConcluir, aoFalhar]);

    return (
        <div className="execucao-wrap">
            {situacao === 'enviando' && (
                <>
                    <div className="execucao-spinner" />
                    <p className="execucao-texto">Enviando para o banco</p>
                    <p className="execucao-aviso">Não feche esta janela.</p>
                </>
            )}

            {situacao === 'sucesso' && (
                <>
                    <div className="execucao-check">
                        <CheckCircle2 size={56} />
                    </div>
                    <p className="execucao-texto">Dados gravados</p>
                </>
            )}

            {situacao === 'erro' && (
                <>
                    <div className="execucao-falha">
                        <AlertCircle size={56} />
                    </div>
                    <p className="execucao-texto">Não foi possível gravar</p>
                    <p className="execucao-aviso">{erro}</p>
                </>
            )}
        </div>
    );
}
