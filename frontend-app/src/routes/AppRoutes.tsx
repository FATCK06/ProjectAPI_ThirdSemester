import { lazy } from 'react';
import { Routes, Route, Navigate, useParams } from 'react-router-dom';
import { Login } from '../pages/Login';
import { DefaultLayout } from '../layouts/DefaultLayout';
import { PrivateRoute } from './PrivateRoute';
import { RotaComPermissao } from './RotaComPermissao';

// Cada página vira um arquivo próprio no build e só é baixada quando aberta: o
// login não carrega Chart.js nem a importação de manifestos. O Suspense que
// mostra o carregamento fica no DefaultLayout, para a sidebar não piscar.
const Dashboard = lazy(() => import('../pages/Dashboard/Dashboard').then((m) => ({ default: m.Dashboard })));
const Ranking = lazy(() => import('../pages/Frota/Ranking').then((m) => ({ default: m.Ranking })));
const OperacaoMensal = lazy(() => import('../pages/Frota/OperacaoMensal').then((m) => ({ default: m.OperacaoMensal })));
const Motoristas = lazy(() => import('../pages/Frota/Motoristas').then((m) => ({ default: m.Motoristas })));
const FichaCadastralMotorista = lazy(() =>
  import('../pages/Frota/OperacaoMensal/FichaMotorista').then((m) => ({ default: m.FichaMotorista })),
);
const ManifestosImport = lazy(() =>
  import('../pages/Importacoes/Manifestos').then((m) => ({ default: m.ManifestosImport })),
);
const VeiculosImport = lazy(() => import('../pages/Importacoes/Veiculos').then((m) => ({ default: m.VeiculosImport })));
const HistoricoImport = lazy(() =>
  import('../pages/Importacoes/HistoricoImportacoes').then((m) => ({ default: m.HistoricoImport })),
);
const CadastroUsuario = lazy(() => import('../pages/Usuarios/Cadastro').then((m) => ({ default: m.CadastroUsuario })));
const StatusServicos = lazy(() => import('../pages/Status').then((m) => ({ default: m.StatusServicos })));

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
