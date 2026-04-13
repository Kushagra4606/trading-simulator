import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getStockBySymbol } from '../api/stocks';
import OrderModal from '../components/OrderModal';
import useAuthStore from '../store/authStore';
import useWebSocketStore from '../store/useWebSocketStore';

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

  if (loading) return <div style={{ padding: '24px' }}>Loading...</div>;
  if (!stock) return null;

  const upperCircuit = (parseFloat(stock.currentPrice) * (1 + stock.circuitLimitPercent / 100)).toFixed(2);
  const lowerCircuit = (parseFloat(stock.currentPrice) * (1 - stock.circuitLimitPercent / 100)).toFixed(2);

  // Use live WebSocket price if available, else fall back to HTTP-fetched price
  const displayPrice = livePrice ?? parseFloat(stock.currentPrice);

  return (
    <div style={{ padding: '24px', maxWidth: '800px', margin: '0 auto' }}>
      <button onClick={() => navigate('/stocks')} style={{ marginBottom: '20px', cursor: 'pointer' }}>
        ← Back to Market
      </button>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div>
          <h1 style={{ margin: 0 }}>{stock.symbol}</h1>
          <p style={{ color: '#6b7280', margin: '4px 0 0' }}>{stock.name}</p>
        </div>
        <div style={{ textAlign: 'right' }}>
          <div style={{ fontSize: '32px', fontWeight: '700', color: '#111827' }}>
            ₹{parseFloat(displayPrice).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
          </div>
          <div style={{ color: '#6b7280', fontSize: '13px', display: 'flex', gap: '8px', justifyContent: 'flex-end', alignItems: 'center' }}>
            <span>Last Traded Price</span>
            {livePrice && <span style={{ color: '#10B981', fontWeight: '600' }}>● LIVE</span>}
          </div>
        </div>
      </div>

      <hr style={{ margin: '24px 0' }} />

      {/* Trade Button */}
      <div style={{ display: 'flex', gap: '12px', marginBottom: '24px' }}>
        <button
          onClick={() => setShowModal(true)}
          style={{
            padding: '12px 32px',
            background: '#238636',
            color: '#fff',
            border: 'none',
            borderRadius: '8px',
            fontFamily: 'IBM Plex Mono, monospace',
            fontWeight: '600',
            fontSize: '14px',
            cursor: 'pointer',
            letterSpacing: '0.06em',
          }}
        >
          BUY / SELL
        </button>
      </div>

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

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '16px' }}>
        <InfoCard label="Sector" value={stock.sector} />
        <InfoCard label="Market Cap" value={stock.marketCap} />
        <InfoCard label="Base Price" value={`₹${stock.basePrice}`} />
        <InfoCard label="Circuit Limit" value={`±${stock.circuitLimitPercent}%`} />
        <InfoCard label="Upper Circuit" value={`₹${upperCircuit}`} color="#10B981" />
        <InfoCard label="Lower Circuit" value={`₹${lowerCircuit}`} color="#EF4444" />
      </div>
    </div>
  );
}

function InfoCard({ label, value, color }) {
  return (
    <div style={{ background: '#f9fafb', padding: '16px', borderRadius: '10px', border: '1px solid #e5e7eb' }}>
      <div style={{ fontSize: '12px', color: '#6b7280', marginBottom: '6px' }}>{label}</div>
      <div style={{ fontSize: '18px', fontWeight: '600', color: color || '#111827' }}>{value}</div>
    </div>
  );
}