import { Navigate, Outlet } from "react-router-dom";
import { estaLogado } from "../services/auth";

export function PrivateRoute() {
    return estaLogado() ? <Outlet /> : <Navigate to="/login" replace />;
}
