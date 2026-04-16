import { useNavigate, useLocation } from 'react-router-dom';
import useAuthStore from '../store/authStore';
import './Navbar.css';

export default function Navbar() {
  const { walletBalance, logout } = useAuthStore();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const NavLink = ({ path, label }) => {
    const isActive = location.pathname.startsWith(path);
    return (
      <span
        onClick={() => navigate(path)}
        className={`nav-link ${isActive ? 'active' : ''}`}
      >
        {label}
      </span>
    );
  };

  return (
    <nav className="navbar">
      <div className="nav-brand" onClick={() => navigate('/dashboard')}>
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M3 3V21H21" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
          <path d="M19 9L14 14L10 10L4 16" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
          <path d="M19 9V14H14" stroke="currentColor" fill="currentColor" strokeWidth="1" strokeLinecap="round" strokeLinejoin="round"/>
        </svg>
        TradeSim
      </div>

      <div className="nav-links">
        <NavLink path="/dashboard" label="Dashboard" />
        <NavLink path="/stocks" label="Market" />
        <NavLink path="/orders" label="Orders" />
        <NavLink path="/portfolio" label="Portfolio" />
      </div>

      <div className="nav-right">
        <div className="wallet-info">
          Wallet:
          <span className="wallet-balance">
            ₹{walletBalance?.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </span>
        </div>
        <button className="btn-logout" onClick={handleLogout}>
          Logout
        </button>
      </div>
    </nav>
  );
}
