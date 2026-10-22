import { Routes, Route, Navigate } from 'react-router-dom';
import { Login } from '../pages/Login';
import { Dashboard } from '../pages/Dashboard/Dashboard';
import { DefaultLayout } from '../layouts/DefaultLayout';
import { ManifestosImport } from '../pages/Importacoes/Manifestos';
import { VeiculosImport } from '../pages/Importacoes/Veiculos';
import { HistoricoImport } from '../pages/Importacoes/HistoricoImportacoes';
import { CadastroUsuario } from '../pages/Usuarios/Cadastro';
import { StatusServicos } from '../pages/Status';
import { Motoristas } from '../pages/Frota/Motoristas';
import { FichaMotorista } from '../pages/Frota/Motoristas/FichaMotorista';
import { PrivateRoute } from './PrivateRoute';
import { Ranking } from '../pages/Frota/Ranking';

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
          <Route path="/motoristas" element={<Motoristas />} />
          <Route path="/motoristas/:motoristaId" element={<FichaMotorista />} />
          <Route path="/importacoes/manifestos" element={<ManifestosImport />} />
          <Route path="/importacoes/veiculos" element={<VeiculosImport />} />
          <Route path="/importacoes/historico" element={<HistoricoImport />} />
          <Route path="/usuarios/novo" element={<CadastroUsuario />} />
          <Route path="/status" element={<StatusServicos />} />  
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
