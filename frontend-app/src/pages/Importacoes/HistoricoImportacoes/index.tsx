import { useEffect, useState } from "react";
import DataTable, { type ColunaTabela } from "../../../components/DataTable/DataTable";
import api from "../../../services/api";
import "./HistoricoImportacoes.css";
import { PageHeader } from "../../../components/PageHeader";

interface ImportacaoArquivo {
  id: string;
  nomeArquivo: string;
  tipo: string;
  dataImportacao: string;
  usuario: string;
  status: string;
}

const colunas: ColunaTabela<ImportacaoArquivo>[] = [
  { chave: "nomeArquivo", titulo: "Arquivo", render: (l) => l.nomeArquivo },
  {
    chave: "dataImportacao",
    titulo: "Data",
    render: (l) => new Date(l.dataImportacao).toLocaleString("pt-BR"),
  },
  { chave: "usuario", titulo: "Usuário", render: (l) => l.usuario },
  { chave: "status", titulo: "Status", render: (l) => l.status },
];

export function HistoricoImport() {
  const [importacoes, setImportacoes] = useState<ImportacaoArquivo[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    api
      .get<ImportacaoArquivo[]>("/importacao/arquivos/historico")
      .then((resposta) => setImportacoes(resposta.data))
      .catch(() => setErro("Não foi possível carregar o histórico"))
      .finally(() => setCarregando(false));
  }, []);

  // O cabeçalho fica sempre visível; só o conteúdo abaixo dele muda.
  return (
    <div className="historico-importacoes">
      <PageHeader
        titulo="Histórico de Importações"
        subtitulo="Arquivos importados, com data, responsável e status de cada envio"
      />
      {carregando && <p>Carregando...</p>}
      {!carregando && erro && <p>{erro}</p>}
      {!carregando && !erro && (
        <DataTable
          colunas={colunas}
          linhas={importacoes}
          chaveLinha={(linha) => linha.id}
          alturaMaxima="70vh"
          mensagemVazia="Nenhuma importação realizada até agora."
        />
      )}
    </div>
  );
}