import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import truckBg from '../../assets/images/truck-login.png';
import './login.css';
import { Eye, EyeOff } from 'lucide-react';
import api from '../../services/api';

//criando interface que reflete o contrato com o backend
interface LoginResponse {
  token: string
}

export function Login() {
    const [email, setEmail] = useState('');
    const [senha,setSenha] = useState('');
    const navigate = useNavigate();

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();

        try {
          const resposta = await fetch('http://localhost:8081/api/auth/login', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({email, senha}),
          });

          if(resposta.ok){
            const dados = (await resposta.json()) as LoginResponse;

            localStorage.setItem('@Logistica:token', dados.token);
            navigate('/home');
          } else {
            alert('Email ou senha incorretos!');
          }
        } catch (erro) {
          console.log('Erro ao conectar com o servidor:', erro);
          alert('Servidor indisponível no momento.');
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