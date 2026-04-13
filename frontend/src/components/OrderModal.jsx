import { useState } from 'react';
import { placeOrder } from '../api/orders';
import './OrderModal.css';

export default function OrderModal({ stock, onClose, useAuthStore }) {
  const token = useAuthStore((s) => s.token);
  const [side, setSide] = useState('BUY');
  const [orderType, setOrderType] = useState('MARKET');
  const [quantity, setQuantity] = useState(1);
  const [limitPrice, setLimitPrice] = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState(null);

  const estimatedValue =
    orderType === 'LIMIT' && limitPrice
      ? (parseFloat(limitPrice) * quantity).toFixed(2)
      : stock?.ltp
      ? (parseFloat(stock.ltp) * quantity).toFixed(2)
      : null;

  const handleSubmit = async () => {
    if (!quantity || quantity < 1) {
      setMessage({ type: 'error', text: 'Quantity must be at least 1' });
      return;
    }
    if (orderType === 'LIMIT' && (!limitPrice || parseFloat(limitPrice) <= 0)) {
      setMessage({ type: 'error', text: 'Enter a valid limit price' });
      return;
    }

    setLoading(true);
    setMessage(null);

    try {
      const payload = {
        symbol: stock.symbol,
        side,
        orderType,
        quantity: parseInt(quantity),
        price: orderType === 'LIMIT' ? parseFloat(limitPrice) : null,
      };
      const res = await placeOrder(payload);
      setMessage({
        type: 'success',
        text: `Order placed! Status: ${res.data.status}`,
      });
      setTimeout(onClose, 1800);
    } catch (err) {
      setMessage({
        type: 'error',
        text: err.response?.data?.message || err.message,
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="om-overlay" onClick={onClose}>
      <div className="om-modal" onClick={(e) => e.stopPropagation()}>
        {/* Header */}
        <div className="om-header">
          <div className="om-stock-info">
            <span className="om-symbol">{stock?.symbol}</span>
            <span className="om-name">{stock?.name}</span>
          </div>
          <div className="om-ltp-badge">
            <span className="om-ltp-label">LTP</span>
            <span className="om-ltp-value">₹{stock?.ltp}</span>
          </div>
          <button className="om-close" onClick={onClose}>✕</button>
        </div>

        {/* Side Toggle */}
        <div className="om-side-toggle">
          <button
            className={`om-side-btn ${side === 'BUY' ? 'active-buy' : ''}`}
            onClick={() => setSide('BUY')}
          >
            BUY
          </button>
          <button
            className={`om-side-btn ${side === 'SELL' ? 'active-sell' : ''}`}
            onClick={() => setSide('SELL')}
          >
            SELL
          </button>
        </div>

        {/* Order Type */}
        <div className="om-field">
          <label className="om-label">ORDER TYPE</label>
          <div className="om-type-toggle">
            <button
              className={`om-type-btn ${orderType === 'MARKET' ? 'active' : ''}`}
              onClick={() => setOrderType('MARKET')}
            >
              Market
            </button>
            <button
              className={`om-type-btn ${orderType === 'LIMIT' ? 'active' : ''}`}
              onClick={() => setOrderType('LIMIT')}
            >
              Limit
            </button>
          </div>
        </div>

        {/* Limit Price */}
        {orderType === 'LIMIT' && (
          <div className="om-field">
            <label className="om-label">LIMIT PRICE (₹)</label>
            <input
              className="om-input"
              type="number"
              min="0.01"
              step="0.01"
              value={limitPrice}
              onChange={(e) => setLimitPrice(e.target.value)}
              placeholder={`Current: ₹${stock?.ltp}`}
            />
          </div>
        )}

        {/* Quantity */}
        <div className="om-field">
          <label className="om-label">QUANTITY</label>
          <div className="om-qty-row">
            <button
              className="om-qty-btn"
              onClick={() => setQuantity((q) => Math.max(1, q - 1))}
            >−</button>
            <input
              className="om-input om-qty-input"
              type="number"
              min="1"
              value={quantity}
              onChange={(e) => setQuantity(Math.max(1, parseInt(e.target.value) || 1))}
            />
            <button
              className="om-qty-btn"
              onClick={() => setQuantity((q) => q + 1)}
            >+</button>
          </div>
        </div>

        {/* Estimated Value */}
        {estimatedValue && (
          <div className="om-estimate">
            <span>Estimated {side === 'BUY' ? 'Cost' : 'Proceeds'}</span>
            <span className="om-estimate-value">₹{estimatedValue}</span>
          </div>
        )}

        {/* Message */}
        {message && (
          <div className={`om-message om-message--${message.type}`}>
            {message.text}
          </div>
        )}

        {/* Submit */}
        <button
          className={`om-submit om-submit--${side.toLowerCase()}`}
          onClick={handleSubmit}
          disabled={loading}
        >
          {loading ? 'Placing...' : `Place ${side} Order`}
        </button>
      </div>
    </div>
  );
}
