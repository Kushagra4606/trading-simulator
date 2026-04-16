// src/pages/StocksPage.jsx
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAllStocks } from '../api/stocks';
import './StocksPage.css';

const SECTOR_COLORS = {
  IT: '#3B82F6',
  Banking: '#10B981',
  Pharma: '#8B5CF6',
  Auto: '#F59E0B',
  FMCG: '#EC4899',
  Energy: '#EF4444',
};

// Simple helper to convert hex to rgba
const hexToRgba = (hex, alpha) => {
  const r = parseInt(hex.slice(1, 3), 16);
  const g = parseInt(hex.slice(3, 5), 16);
  const b = parseInt(hex.slice(5, 7), 16);
  return `rgba(${r}, ${g}, ${b}, ${alpha})`;
};

export default function StocksPage() {
  const [stocks, setStocks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [selectedSector, setSelectedSector] = useState('ALL');
  const navigate = useNavigate();

  useEffect(() => {
    getAllStocks()
      .then(res => setStocks(res.data))
      .finally(() => setLoading(false));
  }, []);

  const sectors = ['ALL', ...new Set(stocks.map(s => s.sector))];

  const filtered = stocks.filter(s => {
    const matchSector = selectedSector === 'ALL' || s.sector === selectedSector;
    const matchSearch = s.symbol.includes(search.toUpperCase()) ||
                        s.name.toLowerCase().includes(search.toLowerCase());
    return matchSector && matchSearch;
  });

  if (loading) return <div className="loading-container">Loading market data...</div>;

  return (
    <div className="stocks-page">
      <h1 className="page-title">Market Overview</h1>

      {/* Filters */}
      <div className="filters-bar">
        <input
          className="search-input"
          placeholder="Search symbol or company..."
          value={search}
          onChange={e => setSearch(e.target.value)}
        />
        {sectors.map(sector => (
          <button
            key={sector}
            onClick={() => setSelectedSector(sector)}
            className={`sector-btn ${selectedSector === sector ? 'active' : ''}`}
          >
            {sector}
          </button>
        ))}
      </div>

      {/* Table */}
      <div className="table-container">
        <table className="stocks-table">
          <thead>
            <tr>
              <th>Symbol & Name</th>
              <th>Sector</th>
              <th>Market Cap</th>
              <th style={{ textAlign: 'right' }}>LTP (₹)</th>
            </tr>
          </thead>
          <tbody>
            {filtered.map(stock => (
              <tr
                key={stock.symbol}
                onClick={() => navigate(`/stocks/${stock.symbol}`)}
              >
                <td>
                  <div className="symbol-name">{stock.symbol}</div>
                  <div className="company-name">{stock.name}</div>
                </td>
                <td>
                  <span 
                    className="sector-badge"
                    style={{
                      backgroundColor: hexToRgba(SECTOR_COLORS[stock.sector] || '#888888', 0.15),
                      color: SECTOR_COLORS[stock.sector] || '#888888',
                    }}
                  >
                    {stock.sector}
                  </span>
                </td>
                <td>{stock.marketCap}</td>
                <td className="price-cell">
                  ₹{parseFloat(stock.currentPrice).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <p className="footer-text">
        Showing {filtered.length} of {stocks.length} assets
      </p>
    </div>
  );
}