import axios from 'axios';

// Cria a instância base do Axios apontando para o seu Spring Boot
const api = axios.create({
    baseURL: 'http://localhost:8081/api'
});

// Configura o Interceptador: antes de qualquer requisição sair, ele faz isso:
api.interceptors.request.use((config) => {
    // Busca o crachá que salvamos no navegador
    const token = localStorage.getItem('@Logistica:token');
    
    // Se o crachá existir, anexa no cabeçalho
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    
    return config;
}, (error) => {
    return Promise.reject(error);
});

export default api;