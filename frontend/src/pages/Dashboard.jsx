import { useNavigate } from 'react-router-dom';
import useAuthStore from '../store/authStore';
import './Dashboard.css';

export default function Dashboard() {
  const { user, walletBalance } = useAuthStore();
  const navigate = useNavigate();

  return (
    <div className="dashboard-page">
      <div className="dashboard-header">
        <h1 className="dashboard-title">Welcome back, {user?.name}</h1>
        <p className="dashboard-subtitle">Here is your trading overview.</p>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-label">Available Buying Power</div>
          <div className="stat-value">
            ₹{walletBalance?.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </div>
        </div>
        {/* We can add more stat cards here later like Total Portfolio Value, Day's P&L */}
      </div>

      <div className="dashboard-actions">
        <button 
          className="btn-primary"
          onClick={() => navigate('/stocks')}
        >
          Browse Market
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M5 12H19M19 12L12 5M19 12L12 19" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
          </svg>
        </button>
      </div>
    </div>
  );
}