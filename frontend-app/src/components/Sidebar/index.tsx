import { NavLink } from 'react-router-dom';
import { LayoutDashboard, PanelLeftClose, PanelLeftOpen, UploadCloud, ChevronRight, ChevronDown, FileText } from 'lucide-react';
import neweLogo from '../../assets/image 3.png'; 
import './sidebar.css';
import { useState } from 'react';

interface SidebarProps {
  isMinimized: boolean;
  toggleSidebar: () => void;
}

export function Sidebar({ isMinimized, toggleSidebar }: SidebarProps) {
  const [isImportOpen, setIsImportOpen] = useState(false);

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
                {isImportOpen ? <ChevronDown size={16} /> : <ChevronRight size={16} />}
              </>
            )}
          </button>

          {isImportOpen && !isMinimized && (
            <div className="accordion-content">
              <NavLink
                to="/importacoes/manifestos"
                className={({ isActive }) => (isActive ? 'nav-sub-item active' : 'nav-sub-item')}
              >
                <FileText size={18} />
                <span>Manifestos</span>
              </NavLink>
            </div>
          )}
        </div>

      </nav>
    </aside>
  );
}