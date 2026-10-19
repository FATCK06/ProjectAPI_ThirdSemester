import { useEffect, useRef, useState } from "react";
import { CheckCircle2, AlertCircle } from "lucide-react";
import {
  executarImportacao,
  mensagemDeErro,
} from "../../../../services/importacao";
import { Loading } from "../../../../components/Loading";
import "./step5.css";

/** Tempo que o check fica visível antes de avançar, só para o usuário ver que deu certo. */
const PAUSA_APOS_SUCESSO_MS = 1200;

/** "45 s", "3 min 07 s". */
function formatarDecorrido(segundos: number): string {
  if (segundos < 60) return `${segundos} s`;
  const min = Math.floor(segundos / 60);
  const seg = String(segundos % 60).padStart(2, "0");
  return `${min} min ${seg} s`;
}

type Situacao = "enviando" | "sucesso" | "erro";

interface PropsPasso5 {
  importacaoId: number;
  aoConcluir: (linhasGravadas: number) => void;
  aoFalhar: (mensagem: string) => void;
}

export function Step5Execucao({
  importacaoId,
  aoConcluir,
  aoFalhar,
}: PropsPasso5) {
  const [situacao, setSituacao] = useState<Situacao>("enviando");
  const [erro, setErro] = useState<string | null>(null);
  // A gravação leva minutos (jul-ago: ~9 min). Sem algo andando na tela, parece
  // travada e o usuário reenvia o arquivo.
  const [decorrido, setDecorrido] = useState(0);
  // StrictMode monta o componente duas vezes em desenvolvimento; sem esta trava
  // a importação seria disparada em duplicidade.
  const jaDisparou = useRef(false);

  useEffect(() => {
    if (jaDisparou.current) return;
    jaDisparou.current = true;

    (async () => {
      try {
        const resposta = await executarImportacao(importacaoId);

        setSituacao("sucesso");
        setTimeout(() => {
          aoConcluir(resposta.linhasGravadas);
        }, PAUSA_APOS_SUCESSO_MS);
      } catch (falha) {
        const mensagem = mensagemDeErro(falha);
        setSituacao("erro");
        setErro(mensagem);
        aoFalhar(mensagem);
      }
    })();
  }, [importacaoId, aoConcluir, aoFalhar]);

  useEffect(() => {
    if (situacao !== "enviando") return;
    const inicio = Date.now();
    const relogio = setInterval(() => {
      setDecorrido(Math.floor((Date.now() - inicio) / 1000));
    }, 1000);
    return () => clearInterval(relogio);
  }, [situacao]);

  if (situacao === "enviando") {
    return (
      <Loading
        texto="Enviando para o banco"
        detalhe={`Arquivos grandes levam alguns minutos. Não feche esta janela nem envie o arquivo de novo. Tempo decorrido: ${formatarDecorrido(decorrido)}`}
      />
    );
  }

  return (
    <div className="execucao-wrap">
      {situacao === "sucesso" && (
        <>
          <div className="execucao-check">
            <CheckCircle2 size={56} />
          </div>
          <p className="execucao-texto">Dados gravados</p>
        </>
      )}

      {situacao === "erro" && (
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
