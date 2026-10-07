import { Navigate, Outlet } from "react-router-dom";

export function PrivateRoute(){
    const token = localStorage.getItem('@Logistica:token');

    const isTokenValid = (tokenStr: string | null): boolean => {
        if (!tokenStr) return false;

    try {
        const [, payloadBase64] = tokenStr.split('.');
        const payload = JSON.parse(atob(payloadBase64));

        const now = Math.floor(Date.now() / 1000);

        return payload.exp ? payload.exp > now : true;
    } catch {
        return false;
    }
};

    const autenticado = isTokenValid(token);

    if (!autenticado){
        localStorage.removeItem('@Logistica:token');
        localStorage.removeItem('@Logistica:nome');
        localStorage.removeItem('@Logistica:perfil');
}

    return <Outlet />;
}    