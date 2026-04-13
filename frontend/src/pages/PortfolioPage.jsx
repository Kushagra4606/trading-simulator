import { useState, useEffect } from 'react';
import { getHoldings } from '../api/orders';
import './PortfolioPage.css';

export default function PortfolioPage({ useAuthStore }) {
  const [holdings, setHoldings] = useState([]);
  const [ltpMap, setLtpMap] = useState({});
  const [loading, setLoading] = useState(true);

  // Fetch holdings then fetch LTP for each symbol
  const fetchData = async () => {
    try {
      const res = await getHoldings();
      const data = res.data;
      setHoldings(data);

      // Fetch LTP for every symbol in holdings
      const ltps = {};
      await Promise.all(
        data.map(async (h) => {
          try {
            const r = await fetch(`http://localhost:8080/api/stocks/${h.symbol}/ltp`);
            const text = await r.text();
            ltps[h.symbol] = parseFloat(text);
          } catch {
            ltps[h.symbol] = h.avgBuyPrice; // fallback to avg price
          }
        })
      );
      setLtpMap(ltps);
    } catch (err) {
      console.error('Failed to fetch portfolio:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchData(); }, []);

  // Compute per-holding metrics
  const enriched = holdings.map((h) => {
    const ltp = ltpMap[h.symbol] || h.avgBuyPrice;
    const currentValue = ltp * h.quantity;
    const investedValue = h.avgBuyPrice * h.quantity;
    const pnl = currentValue - investedValue;
    const pnlPct = ((pnl / investedValue) * 100).toFixed(2);
    return { ...h, ltp, currentValue, investedValue, pnl, pnlPct };
  });

  const totalInvested = enriched.reduce((s, h) => s + h.investedValue, 0);
  const totalCurrent  = enriched.reduce((s, h) => s + h.currentValue, 0);
  const totalPnL      = totalCurrent - totalInvested;
  const totalPnLPct   = totalInvested > 0
    ? ((totalPnL / totalInvested) * 100).toFixed(2)
    : '0.00';

  const fmt = (n) =>
    new Intl.NumberFormat('en-IN', { minimumFractionDigits: 2 }).format(n);

  return (
    <div className="pp-page">
      {/* Header */}
      <div className="pp-header">
        <div>
          <h1 className="pp-title">Portfolio</h1>
          <p className="pp-subtitle">Your holdings and P&amp;L</p>
        </div>
        <button className="pp-refresh" onClick={fetchData}>↻ Refresh</button>
      </div>

      {loading ? (
        <div className="pp-loading">
          <div className="pp-spinner" />
          <span>Loading portfolio...</span>
        </div>
      ) : (
        <>
          {/* Summary Cards */}
          <div className="pp-summary">
            <div className="pp-card">
              <span className="pp-card-label">INVESTED VALUE</span>
              <span className="pp-card-value">₹{fmt(totalInvested)}</span>
            </div>
            <div className="pp-card">
              <span className="pp-card-label">CURRENT VALUE</span>
              <span className="pp-card-value">₹{fmt(totalCurrent)}</span>
            </div>
            <div className={`pp-card pp-card--pnl ${totalPnL >= 0 ? 'positive' : 'negative'}`}>
              <span className="pp-card-label">TOTAL P&amp;L</span>
              <span className="pp-card-value pp-pnl-main">
                {totalPnL >= 0 ? '+' : ''}₹{fmt(totalPnL)}
              </span>
              <span className="pp-pnl-pct">
                {totalPnL >= 0 ? '▲' : '▼'} {Math.abs(totalPnLPct)}%
              </span>
            </div>
            <div className="pp-card">
              <span className="pp-card-label">HOLDINGS</span>
              <span className="pp-card-value">{holdings.length}</span>
            </div>
          </div>

          {/* Holdings Table */}
          {enriched.length === 0 ? (
            <div className="pp-empty">
              <span className="pp-empty-icon">📊</span>
              <p>No holdings yet. Place a buy order to get started.</p>
            </div>
          ) : (
            <div className="pp-table-wrap">
              <table className="pp-table">
                <thead>
                  <tr>
                    <th>SYMBOL</th>
                    <th>QTY</th>
                    <th>AVG BUY PRICE</th>
                    <th>LTP</th>
                    <th>INVESTED</th>
                    <th>CURRENT VALUE</th>
                    <th>P&amp;L</th>
                    <th>CHANGE</th>
                  </tr>
                </thead>
                <tbody>
                  {enriched.map((h) => (
                    <tr key={h.id} className="pp-row">
                      <td>
                        <div className="pp-symbol-cell">
                          <span className="pp-symbol">{h.symbol}</span>
                        </div>
                      </td>
                      <td className="pp-mono">{h.quantity}</td>
                      <td className="pp-mono">₹{fmt(h.avgBuyPrice)}</td>
                      <td className="pp-mono pp-ltp">₹{fmt(h.ltp)}</td>
                      <td className="pp-mono">₹{fmt(h.investedValue)}</td>
                      <td className="pp-mono">₹{fmt(h.currentValue)}</td>
                      <td>
                        <span className={`pp-pnl ${h.pnl >= 0 ? 'pp-pnl--pos' : 'pp-pnl--neg'}`}>
                          {h.pnl >= 0 ? '+' : ''}₹{fmt(h.pnl)}
                        </span>
                      </td>
                      <td>
                        <span className={`pp-change ${h.pnl >= 0 ? 'pp-change--pos' : 'pp-change--neg'}`}>
                          {h.pnl >= 0 ? '▲' : '▼'} {Math.abs(h.pnlPct)}%
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}
    </div>
  );
}
