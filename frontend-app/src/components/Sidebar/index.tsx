import { NavLink } from 'react-router-dom';
import { LayoutDashboard, PanelLeftClose, PanelLeftOpen } from 'lucide-react';
import neweLogo from '../../assets/image 3.png'; 
import './sidebar.css';

interface SidebarProps {
  isMinimized: boolean;
  toggleSidebar: () => void;
}

export function Sidebar({ isMinimized, toggleSidebar }: SidebarProps) {
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
      </nav>
    </aside>
  );
}