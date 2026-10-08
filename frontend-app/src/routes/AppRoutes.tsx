import { Routes, Route, Navigate } from 'react-router-dom';
import { Login } from '../pages/Login';
import { Dashboard } from '../pages/Dashboard';
import { DefaultLayout } from '../layouts/DefaultLayout';
import { ManifestosImport } from '../pages/Importacoes/Manifestos';
import { CadastroUsuario } from '../pages/Usuarios/Cadastro';
import { StatusServicos } from '../pages/Status';
import { Motoristas } from '../pages/Motoristas';
import { PrivateRoute } from './PrivateRoute';

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
          <Route path="/motoristas" element={<Motoristas />} />
          <Route path="/importacoes/manifestos" element={<ManifestosImport />} />
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