import { NavLink, useNavigate } from 'react-router-dom';
import { LayoutDashboard, PanelLeftClose, PanelLeftOpen, UploadCloud, ChevronRight, LogOut, UserPlus, Activity, Gauge, TrendingUp, Truck, CircleHelp } from 'lucide-react';
import neweLogo from '../../assets/image 3.png';
import './sidebar.css';
import { logout, rotuloPerfil, temPermissao, usuarioLogado } from '../../services/auth';
import { useTour } from '../../tour/useTour';

interface SidebarProps {
  isMinimized: boolean;
  toggleSidebar: () => void;
}

export function Sidebar({ isMinimized, toggleSidebar }: SidebarProps) {
  // No contexto do tour (TourProvider) para o tutorial poder abrir o acordeão
  const { importacaoAberta: isImportOpen, setImportacaoAberta: setIsImportOpen, iniciarTour } = useTour();
  const navigate = useNavigate();
  const usuario = usuarioLogado();
  const podeGerenciarUsuarios = temPermissao('GERENCIAR_USUARIOS');
  const podeVerStatus = temPermissao('VER_STATUS_SERVICO');

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
    <aside className={`sidebar-container ${isMinimized ? 'minimized' : ''}`} data-tour="menu-lateral">
      <div className="sidebar-header">
        
        {!isMinimized && <img src={neweLogo} alt="Logo Newe" className="sidebar-logo" />}
        
        <button className="btn-toggle" onClick={toggleSidebar} data-tour="menu-recolher">
          {isMinimized ? <PanelLeftOpen size={20} /> : <PanelLeftClose size={20} />}
        </button>
      </div>

      <nav className="sidebar-nav">
        <NavLink
          to="/dashboard"
          className={({ isActive }) => (isActive ? 'nav-item active' : 'nav-item')}
          title="Dashboard"
          data-tour="menu-dashboard"
        >
          <LayoutDashboard size={20} />
          {!isMinimized && <span>Dashboard</span>}
        </NavLink>

        <NavLink
          to="/operacao-mensal"
          className={({ isActive }) => (isActive ? 'nav-item active' : 'nav-item')}
          title="Operação Mensal"
          data-tour="menu-operacao"
        >
          <Gauge size={20} />
          {!isMinimized && <span>Operação Mensal</span>}
        </NavLink>

        <NavLink
          to="/motoristas"
          className={({ isActive }) => (isActive ? 'nav-item active' : 'nav-item')}
          title="Motoristas"
          data-tour="menu-motoristas"
        >
          <Truck size={20} />
          {!isMinimized && <span>Motoristas</span>}
        </NavLink>

        <NavLink to="/ranking" className={({ isActive }) => (isActive ? 'nav-item active' : 'nav-item')} title="Ranking mensal" data-tour="menu-ranking">
          <TrendingUp size={20} />
          {!isMinimized && <span>Ranking Mensal</span>}
        </NavLink>

        <div className="nav-accordion" data-tour="menu-importacao">
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
                  data-tour="menu-manifestos"
                  className={({ isActive }) => (isActive ? 'nav-sub-item active' : 'nav-sub-item')}
                >
                  <span>Manifestos</span>
                </NavLink>
              </div>
            </div>
          )}

          {!isMinimized && (
            <div className={`accordion-content ${isImportOpen ? 'aberto' : ''}`} inert={!isImportOpen}>
              <div className="accordion-itens">
                <NavLink
                  to="/importacoes/veiculos"
                  data-tour="menu-veiculos"
                  className={({ isActive }) => (isActive ? 'nav-sub-item active' : 'nav-sub-item')}
                >
                  <span>Veículos</span>
                </NavLink>
              </div>
            </div>
          )}

          {!isMinimized && (
            <div className={`accordion-content ${isImportOpen ? 'aberto' : ''}`} inert={!isImportOpen}>
              <div className="accordion-itens">
                <NavLink
                  to="/importacoes/historico"
                  data-tour="menu-historico"
                  className={({ isActive }) => (isActive ? 'nav-sub-item active' : 'nav-sub-item')}
                >
                  <span>Histórico de Importações</span>
                </NavLink>
              </div>
            </div>
          )}
        </div>

        {/* Itens de administração. Esconder é só conveniência visual — quem
            barra de fato é o gateway, que confere o perfil dentro do token. */}
        {podeGerenciarUsuarios && (
          <NavLink
            to="/usuarios/novo"
            className={({ isActive }) => (isActive ? 'nav-item active' : 'nav-item')}
            title="Cadastrar usuário"
            data-tour="menu-usuarios"
          >
            <UserPlus size={20} />
            {!isMinimized && <span>Cadastrar usuário</span>}
          </NavLink>
        )}

        {podeVerStatus && (
          <NavLink
            to="/status"
            className={({ isActive }) => (isActive ? 'nav-item active' : 'nav-item')}
            title="Status dos serviços"
            data-tour="menu-status"
          >
            <Activity size={20} />
            {!isMinimized && <span>Status dos serviços</span>}
          </NavLink>
        )}

        {/* Último da navegação, para todos os perfis: inicia o tour, não navega */}
        <button
          type="button"
          className="nav-item btn-tutorial"
          onClick={iniciarTour}
          title="Tutorial"
          data-tour="menu-tutorial"
        >
          <CircleHelp size={20} />
          {!isMinimized && <span>Tutorial</span>}
        </button>

      </nav>

      <div className="sidebar-footer" data-tour="menu-rodape">
        {!isMinimized && usuario && (
          <div className="usuario-info">
            <span className="usuario-nome">{usuario.nome || 'Usuário'}</span>
            <span className="usuario-perfil">{rotuloPerfil(usuario.perfil)}</span>
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
