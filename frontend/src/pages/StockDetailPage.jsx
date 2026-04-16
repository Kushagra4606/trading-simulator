import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getStockBySymbol } from '../api/stocks';
import OrderModal from '../components/OrderModal';
import useAuthStore from '../store/authStore';
import useWebSocketStore from '../store/useWebSocketStore';
import './StockDetailPage.css';

export default function StockDetailPage() {
  const { symbol } = useParams();
  const subscribeLtp = useWebSocketStore((s) => s.subscribeLtp);
  const livePrice = useWebSocketStore((s) => s.ltpMap[symbol]);
  const navigate = useNavigate();
  const [stock, setStock] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
    const sub = subscribeLtp(symbol);
    getStockBySymbol(symbol)
      .then(res => setStock(res.data))
      .catch(() => navigate('/stocks'))
      .finally(() => setLoading(false));
    return () => sub?.unsubscribe();
  }, [symbol]);

  if (loading) return <div className="loading-container">Loading...</div>;
  if (!stock) return null;

  const upperCircuit = (parseFloat(stock.currentPrice) * (1 + stock.circuitLimitPercent / 100)).toFixed(2);
  const lowerCircuit = (parseFloat(stock.currentPrice) * (1 - stock.circuitLimitPercent / 100)).toFixed(2);

  const displayPrice = livePrice ?? parseFloat(stock.currentPrice);

  return (
    <div className="stock-detail-page">
      <button className="back-btn" onClick={() => navigate('/stocks')}>
        ← Back to Market
      </button>

      <div className="header-section">
        <div>
          <h1 className="stock-title">{stock.symbol}</h1>
          <p className="stock-subtitle">{stock.name}</p>
        </div>
        <div className="price-container">
          <div className="live-price">
            ₹{parseFloat(displayPrice).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </div>
          <div className="price-label">
            <span>Last Traded Price</span>
            {livePrice && (
              <span className="live-indicator">
                <span className="live-dot"></span> LIVE
              </span>
            )}
          </div>
        </div>
      </div>

      <hr className="divider" />

      {/* Trade Button */}
      <button
        className="trade-btn"
        onClick={() => setShowModal(true)}
      >
        BUY / SELL
      </button>

      {/* Order Modal */}
      {showModal && (
        <OrderModal
          stock={{
            symbol: stock.symbol,
            name: stock.name,
            ltp: stock.currentPrice,
          }}
          onClose={() => setShowModal(false)}
          useAuthStore={useAuthStore}
        />
      )}

      <div className="info-grid">
        <InfoCard label="Sector" value={stock.sector} />
        <InfoCard label="Market Cap" value={stock.marketCap} />
        <InfoCard label="Base Price" value={`₹${stock.basePrice}`} />
        <InfoCard label="Circuit Limit" value={`±${stock.circuitLimitPercent}%`} />
        <InfoCard label="Upper Circuit" value={`₹${upperCircuit}`} colorClass="text-profit" />
        <InfoCard label="Lower Circuit" value={`₹${lowerCircuit}`} colorClass="text-loss" />
      </div>
    </div>
  );
}

function InfoCard({ label, value, colorClass = "" }) {
  return (
    <div className="info-card">
      <div className="info-label">{label}</div>
      <div className={`info-value ${colorClass}`}>{value}</div>
    </div>
  );
}