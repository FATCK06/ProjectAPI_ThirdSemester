// src/pages/Home/index.tsx
import { useNavigate } from 'react-router-dom';

export function Home() {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem('@Logistica:token');
    navigate('/login');
  };

  return (
    <div style={{ padding: '40px', fontFamily: 'Montserrat' }}>
      <h1>Dashboard Logístico</h1>
      <p>Você está logado com sucesso! (Página em construção)</p>
      
      <button 
        onClick={handleLogout}
        style={{ marginTop: '20px', padding: '10px 20px', backgroundColor: '#dc2626', color: '#fff', border: 'none', borderRadius: '8px', cursor: 'pointer' }}
      >
        Sair (Logout)
      </button>
    </div>
  );
}