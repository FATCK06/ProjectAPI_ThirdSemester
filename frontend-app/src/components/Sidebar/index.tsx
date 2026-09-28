import { NavLink, useNavigate } from 'react-router-dom';
import { LayoutDashboard, PanelLeftClose, PanelLeftOpen, UploadCloud, ChevronRight, LogOut, UserPlus, Activity } from 'lucide-react';
import neweLogo from '../../assets/image 3.png';
import './sidebar.css';
import { useState } from 'react';
import { ehAdministrador, logout, usuarioLogado } from '../../services/auth';

interface SidebarProps {
  isMinimized: boolean;
  toggleSidebar: () => void;
}

export function Sidebar({ isMinimized, toggleSidebar }: SidebarProps) {
  const [isImportOpen, setIsImportOpen] = useState(false);
  const navigate = useNavigate();
  const usuario = usuarioLogado();
  const admin = ehAdministrador();

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  const handleImportClick = () => {
    setIsImportOpen(!isImportOpen);
    if (isMinimized) {
      toggleSidebar();
      setIsImportOpen(true);
    }
  };

  return (
    <aside className={`sidebar-container ${isMinimized ? 'minimized' : ''}`}>
      <div className="sidebar-header">
        
        {!isMinimized && <img src={neweLogo} alt="Logo Newe" className="sidebar-logo" />}
        
        <button className="btn-toggle" onClick={toggleSidebar}>
          {isMinimized ? <PanelLeftOpen size={20} /> : <PanelLeftClose size={20} />}
        </button>
      </div>

      <nav className="sidebar-nav">
        <NavLink
          to="/dashboard"
          className={({ isActive }) => (isActive ? 'nav-item active' : 'nav-item')}
          title="Dashboard" 
        >
          <LayoutDashboard size={20} />
          {!isMinimized && <span>Dashboard</span>}
        </NavLink>

        <div className="nav-accordion">
          <button
            className="nav-item btn-accordion"
            onClick={handleImportClick}
            title="Importação de Dados"
          >
            <UploadCloud size={20} />
            {!isMinimized && (
              <>
                <span className="accordion-title">Importação de Dados</span>
                <ChevronRight size={16} className={`accordion-seta ${isImportOpen ? 'aberta' : ''}`} />
              </>
            )}
          </button>

          {/* Fica sempre montado para a abertura e o fechamento poderem animar;
              inert tira os links do Tab enquanto a gaveta está fechada. */}
          {!isMinimized && (
            <div className={`accordion-content ${isImportOpen ? 'aberto' : ''}`} inert={!isImportOpen}>
              <div className="accordion-itens">
                <NavLink
                  to="/importacoes/manifestos"
                  className={({ isActive }) => (isActive ? 'nav-sub-item active' : 'nav-sub-item')}
                >
                  <span>Manifestos</span>
                </NavLink>
              </div>
            </div>
          )}
        </div>

        {/* Itens de administração. Esconder é só conveniência visual — quem
            barra de fato é o gateway, que confere o perfil dentro do token. */}
        {admin && (
          <>
            <NavLink
              to="/usuarios/novo"
              className={({ isActive }) => (isActive ? 'nav-item active' : 'nav-item')}
              title="Cadastrar usuário"
            >
              <UserPlus size={20} />
              {!isMinimized && <span>Cadastrar usuário</span>}
            </NavLink>

            <NavLink
              to="/status"
              className={({ isActive }) => (isActive ? 'nav-item active' : 'nav-item')}
              title="Status dos serviços"
            >
              <Activity size={20} />
              {!isMinimized && <span>Status dos serviços</span>}
            </NavLink>
          </>
        )}

      </nav>

      <div className="sidebar-footer">
        {!isMinimized && usuario && (
          <div className="usuario-info">
            <span className="usuario-nome">{usuario.nome || 'Usuário'}</span>
            <span className="usuario-perfil">{usuario.perfil}</span>
          </div>
        )}

        <button className="nav-item btn-sair" onClick={handleLogout} title="Sair">
          <LogOut size={20} />
          {!isMinimized && <span>Sair</span>}
        </button>
      </div>
    </aside>
  );
}