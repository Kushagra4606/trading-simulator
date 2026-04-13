// src/pages/StocksPage.jsx
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAllStocks } from '../api/stocks';

const SECTOR_COLORS = {
  IT: '#3B82F6',
  Banking: '#10B981',
  Pharma: '#8B5CF6',
  Auto: '#F59E0B',
  FMCG: '#EC4899',
  Energy: '#EF4444',
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

  if (loading) return <div className="loading">Loading stocks...</div>;

  return (
    <div style={{ padding: '24px', maxWidth: '1200px', margin: '0 auto' }}>
      <h1 style={{ marginBottom: '24px' }}>Market</h1>

      {/* Filters */}
      <div style={{ display: 'flex', gap: '12px', marginBottom: '20px', flexWrap: 'wrap' }}>
        <input
          placeholder="Search symbol or name..."
          value={search}
          onChange={e => setSearch(e.target.value)}
          style={{ padding: '8px 12px', borderRadius: '8px', border: '1px solid #ccc', width: '240px' }}
        />
        {sectors.map(sector => (
          <button
            key={sector}
            onClick={() => setSelectedSector(sector)}
            style={{
              padding: '8px 16px',
              borderRadius: '8px',
              border: 'none',
              cursor: 'pointer',
              background: selectedSector === sector ? '#1d4ed8' : '#e5e7eb',
              color: selectedSector === sector ? '#fff' : '#374151',
              fontWeight: '500'
            }}
          >
            {sector}
          </button>
        ))}
      </div>

      {/* Table */}
      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr style={{ background: '#f9fafb', textAlign: 'left' }}>
            <th style={thStyle}>Symbol</th>
            <th style={thStyle}>Name</th>
            <th style={thStyle}>Sector</th>
            <th style={thStyle}>Market Cap</th>
            <th style={{ ...thStyle, textAlign: 'right' }}>LTP (₹)</th>
          </tr>
        </thead>
        <tbody>
          {filtered.map(stock => (
            <tr
              key={stock.symbol}
              onClick={() => navigate(`/stocks/${stock.symbol}`)}
              style={{ cursor: 'pointer', borderBottom: '1px solid #e5e7eb' }}
              onMouseEnter={e => e.currentTarget.style.background = '#f3f4f6'}
              onMouseLeave={e => e.currentTarget.style.background = 'transparent'}
            >
              <td style={tdStyle}>
                <strong style={{ color: '#1d4ed8' }}>{stock.symbol}</strong>
              </td>
              <td style={tdStyle}>{stock.name}</td>
              <td style={tdStyle}>
                <span style={{
                  background: SECTOR_COLORS[stock.sector] + '22',
                  color: SECTOR_COLORS[stock.sector],
                  padding: '2px 10px',
                  borderRadius: '12px',
                  fontSize: '13px',
                  fontWeight: '500'
                }}>
                  {stock.sector}
                </span>
              </td>
              <td style={tdStyle}>{stock.marketCap}</td>
              <td style={{ ...tdStyle, textAlign: 'right', fontWeight: '600' }}>
                ₹{parseFloat(stock.currentPrice).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <p style={{ marginTop: '16px', color: '#6b7280', fontSize: '14px' }}>
        Showing {filtered.length} of {stocks.length} stocks
      </p>
    </div>
  );
}

const thStyle = { padding: '12px 16px', fontWeight: '600', fontSize: '14px', color: '#374151' };
const tdStyle = { padding: '12px 16px', fontSize: '14px', color: '#111827' };