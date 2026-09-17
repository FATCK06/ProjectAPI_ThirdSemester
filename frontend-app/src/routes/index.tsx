import { Routes, Route, Navigate } from 'react-router-dom';
import { Login } from '../pages/Login';
import { Dashboard } from '../pages/Dashboard';
import { DefaultLayout } from '../layouts/DefaultLayout';

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route element={<DefaultLayout />}>
        <Route path="/dashboard" element={<Dashboard />} />
      </Route>
      
      <Route path="/" element={<Navigate to="/dashboard" />} />
    </Routes>
  );
}