import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import truckBg from '../../assets/images/truck-login.png';
import './login.css';
import { Eye, EyeOff } from 'lucide-react';
import api from '../../services/api';
import { useToast } from '../../components/Toast';

//criando interface que reflete o contrato com o backend
interface LoginResponse {
  token: string
}

export function Login() {
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const navigate = useNavigate();
  const toast = useToast();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      const resposta = await api.post('/auth/login', {
            email,
        senha
          });

          if (resposta.data && resposta.data.token) {
            localStorage.setItem('@Logistica:token', resposta.data.token);
            // Guardados para a interface decidir o que mostrar (ex.: a página de
            // status, só para Administrador). Não valem como segurança: quem manda
            // é o perfil dentro do token, que o gateway valida a cada requisição.
            localStorage.setItem('@Logistica:nome', resposta.data.nome ?? '');
            localStorage.setItem('@Logistica:perfil', resposta.data.perfil ?? '');
            navigate('/dashboard');
          }
        } catch (erro) {
          console.error('Erro ao conectar com o servidor:', erro);
          toast.erro('E-mail ou senha incorretos, ou servidor indisponível.', {
            titulo: 'Não foi possível entrar',
          });
        }
  };   

return (
  <div className="login-container">
    <div className="login-card">

      {/* Lado Esquerdo: Imagem do Caminhão */}
      <div className="login-image-wrapper">
        <img src={truckBg} alt="Caminhão Logística NEWE" className="login-image" />
      </div>

      {/* Lado Direito: Formulário */}
      <div className="login-form-wrapper">
        <div className="login-form-content">
          <div className="login-header">
            <h2>Faça seu Login</h2>
            <p>Por favor coloque seu e-mail e senha</p>
          </div>

          <form onSubmit={handleLogin}>
            <div className="input-group">
              <label htmlFor="email">E-mail</label>
              <input
                type="email"
                id="email"
                placeholder="admin@gmail.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>

            <div className="input-group">
              <label htmlFor="senha">Senha</label>
              <div className="password-input-container">
                <input
                  type="password"
                  id="senha"
                  placeholder="entre com sua senha"
                  value={senha}
                  onChange={(e) => setSenha(e.target.value)}
                  required
                />

                <Eye className="eye-icon" size={20} color="#888888" />
              </div>
            </div>

            <div className="forgot-password">
              <a href="#">Esqueceu sua senha?</a>
            </div>

            <button type="submit" className="btn-entrar">
              Login
            </button>
          </form>
        </div>
      </div>

    </div>
  </div>
);
}