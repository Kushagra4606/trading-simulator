import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import useAuthStore from '../store/authStore';
import NewsFeed from '../components/NewsFeed';
import './Dashboard.css';

export default function Dashboard() {
  const { user, walletBalance } = useAuthStore();
  const navigate = useNavigate();
  const [initialNews, setInitialNews] = useState([]);

  useEffect(() => {
    axios.get('http://localhost:8080/api/news')
      .then(res => setInitialNews(res.data))
      .catch(() => {}); // silently fail — live feed still works
  }, []);

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
            ₹{walletBalance?.toLocaleString('en-IN', {
              minimumFractionDigits: 2,
              maximumFractionDigits: 2
            })}
          </div>
        </div>
      </div>

      <div className="dashboard-actions">
        <button
          className="btn-primary"
          onClick={() => navigate('/stocks')}
        >
          Browse Market
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
            <path d="M5 12H19M19 12L12 5M19 12L12 19"
              stroke="currentColor" strokeWidth="2"
              strokeLinecap="round" strokeLinejoin="round"/>
          </svg>
        </button>
      </div>

      <div className="dashboard-news-section">
        <NewsFeed initialNews={initialNews} />
      </div>
    </div>
  );
}
