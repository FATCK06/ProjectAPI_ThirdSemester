import type { ReactNode } from "react";
import "./DataTable.css";

export interface ColunaTabela<T> {
  chave: string;
  titulo: string;
  render: (linha: T) => ReactNode;
  className?: string; // aplicada no <td> da coluna
}

interface PropsDataTable<T> {
  colunas: ColunaTabela<T>[];
  linhas: T[];
  chaveLinha: (linha: T) => string;
  alturaMaxima?: number | string;
}

export function DataTable<T>({
  colunas,
  linhas,
  chaveLinha,
  alturaMaxima = 320,
}: PropsDataTable<T>) {
  return (
    <div className="data-table-wrap" style={{ maxHeight: alturaMaxima }}>
      <table className="data-table">
        <thead>
          <tr>
            {colunas.map((coluna) => (
              <th key={coluna.chave} scope="col">
                {coluna.titulo}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {linhas.map((linha) => (
            <tr key={chaveLinha(linha)}>
              {colunas.map((coluna) => (
                <td key={coluna.chave} className={coluna.className}>
                  {coluna.render(linha)}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default DataTable;
