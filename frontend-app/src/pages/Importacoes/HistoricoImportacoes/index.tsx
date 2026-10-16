import { useEffect, useState } from "react";
import DataTable, { type ColunaTabela } from "../../../components/DataTable/DataTable";
import api from "../../../services/api";
import "./HistoricoImportacoes.css";

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

  if (carregando) return <p>Carregando...</p>;
  if (erro) return <p>{erro}</p>;

  return (
    <div className="historico-importacoes">
      <h2>Histórico de Importações de Arquivo</h2>
      <DataTable
        colunas={colunas}
        linhas={importacoes}
        chaveLinha={(linha) => linha.id}
        alturaMaxima="70vh"
      />
    </div>
  );
}