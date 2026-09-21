import axios from 'axios';

// Aponta para o API Gateway, que roteia para cada microsserviço:
//   /api/auth/**        -> ms-usuarios   :8081
//   /api/motoristas/**  -> ms-frota      :8082
//   /api/importacao/**  -> ms-operacoes  :8083
// O front não conhece essas portas — só o gateway.
const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api',
});

// Anexa o token em toda requisição que sair
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('@Logistica:token');

        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        return config;
    },
    (error) => Promise.reject(error),
);

export default api;
