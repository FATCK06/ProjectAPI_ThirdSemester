import { Routes, Route, Navigate, useParams } from 'react-router-dom';
import { Login } from '../pages/Login';
import { Dashboard } from '../pages/Dashboard/Dashboard';
import { DefaultLayout } from '../layouts/DefaultLayout';
import { ManifestosImport } from '../pages/Importacoes/Manifestos';
import { VeiculosImport } from '../pages/Importacoes/Veiculos';
import { HistoricoImport } from '../pages/Importacoes/HistoricoImportacoes';
import { CadastroUsuario } from '../pages/Usuarios/Cadastro';
import { StatusServicos } from '../pages/Status';
import { OperacaoMensal } from '../pages/Frota/OperacaoMensal';
import { PrivateRoute } from './PrivateRoute';
import { Ranking } from '../pages/Frota/Ranking';
import { HistoricoMotorista } from '../pages/Frota/HistoricoMotorista';
import { RotaComPermissao } from './RotaComPermissao';
import { Motoristas } from '../pages/Frota/Motoristas';
import { FichaMotorista as FichaCadastralMotorista } from '../pages/Frota/OperacaoMensal/FichaMotorista';

function RedirecionarFichaMotorista() {
  const { motoristaId } = useParams();
  return <Navigate to={motoristaId ? `/motoristas/${motoristaId}` : '/motoristas'} replace />;
}

export function AppRoutes() {
  const token = localStorage.getItem('@Logistica:token');

  return (
    <Routes>
      {/* Rota Pública */}
      <Route path="/login" element={<Login />} />

      {/* Rotas Privadas protegidas pelo PrivateRoute */}
      <Route element={<PrivateRoute />}>
        <Route element={<DefaultLayout />}>
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/ranking" element={<Ranking />} />
          <Route path="/operacao-mensal" element={<OperacaoMensal />} />
          <Route path="/motoristas" element={<Motoristas />} />
          <Route path="/motoristas/:motoristaId" element={<FichaCadastralMotorista />} />
          <Route path="/operacao-mensal/motoristas/:motoristaId" element={<RedirecionarFichaMotorista />} />
          <Route path="/motoristas/:motoristaId/historico" element={<HistoricoMotorista />} />
          <Route path="/importacoes/manifestos" element={<ManifestosImport />} />
          <Route path="/importacoes/veiculos" element={<VeiculosImport />} />
          <Route path="/importacoes/historico" element={<HistoricoImport />} />

          <Route element={<RotaComPermissao permissao="GERENCIAR_USUARIOS" />}>
            <Route path="/usuarios/novo" element={<CadastroUsuario />} />
          </Route>
          <Route element={<RotaComPermissao permissao="VER_STATUS_SERVICO" />}>
            <Route path="/status" element={<StatusServicos />} />
          </Route>

        </Route>
      </Route>

      <Route
        path="/"
        element={<Navigate to={token ? "/dashboard" : "/login"} replace />}
      />

      {/* Rota coringa para capturar URLs inexistentes */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );     
}  
