import { Routes, Route, Navigate } from 'react-router-dom';
import { Login } from '../pages/Login';
import { Dashboard } from '../pages/Dashboard';
import { DefaultLayout } from '../layouts/DefaultLayout';
import { ManifestosImport } from '../pages/Importacoes/Manifestos';
import { CadastroUsuario } from '../pages/Usuarios/Cadastro';
import { StatusServicos } from '../pages/Status';

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      
      <Route element={<DefaultLayout />}>
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/importacoes/manifestos" element={<ManifestosImport />} />
        <Route path="/usuarios/novo" element={<CadastroUsuario />} />
        <Route path="/status" element={<StatusServicos />} />
      </Route>
      
      <Route path="/" element={<Navigate to="/dashboard" />} />
    </Routes>
  );
}