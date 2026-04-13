import { useState, useEffect } from 'react';
import { getMyOrders, cancelOrder } from '../api/orders';
import './OrdersPage.css';

export default function OrdersPage() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('ALL');
  const [cancelling, setCancelling] = useState(null);

  const fetchOrders = async () => {
    try {
      const res = await getMyOrders();
      setOrders(res.data);
    } catch (err) {
      console.error('Failed to fetch orders:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchOrders(); }, []);

  const handleCancel = async (orderId) => {
    setCancelling(orderId);
    try {
      await cancelOrder(orderId);
      await fetchOrders();
    } catch (err) {
      alert(err.response?.data?.message || 'Cancel failed');
    } finally {
      setCancelling(null);
    }
  };

  const filters = ['ALL', 'PENDING', 'PARTIAL', 'FILLED', 'CANCELLED'];

  const filtered = filter === 'ALL'
    ? orders
    : orders.filter((o) => o.status === filter);

  const statusColor = (s) => ({
    PENDING:   'status--pending',
    PARTIAL:   'status--partial',
    FILLED:    'status--filled',
    CANCELLED: 'status--cancelled',
  }[s] || '');

  const sideColor = (s) => s === 'BUY' ? 'side--buy' : 'side--sell';

  return (
    <div className="op-page">
      {/* Page Header */}
      <div className="op-header">
        <div>
          <h1 className="op-title">Order Book</h1>
          <p className="op-subtitle">Track and manage your orders</p>
        </div>
        <button className="op-refresh" onClick={fetchOrders}>↻ Refresh</button>
      </div>

      {/* Stats Row */}
      <div className="op-stats">
        {['ALL', 'PENDING', 'FILLED', 'CANCELLED'].map((s) => (
          <div key={s} className="op-stat-card">
            <span className="op-stat-label">{s}</span>
            <span className="op-stat-value">
              {s === 'ALL' ? orders.length : orders.filter(o => o.status === s).length}
            </span>
          </div>
        ))}
      </div>

      {/* Filter Tabs */}
      <div className="op-filters">
        {filters.map((f) => (
          <button
            key={f}
            className={`op-filter-btn ${filter === f ? 'active' : ''}`}
            onClick={() => setFilter(f)}
          >
            {f}
          </button>
        ))}
      </div>

      {/* Table */}
      {loading ? (
        <div className="op-loading">
          <div className="op-spinner" />
          <span>Loading orders...</span>
        </div>
      ) : filtered.length === 0 ? (
        <div className="op-empty">
          <span className="op-empty-icon">📋</span>
          <p>No {filter !== 'ALL' ? filter.toLowerCase() : ''} orders found</p>
        </div>
      ) : (
        <div className="op-table-wrap">
          <table className="op-table">
            <thead>
              <tr>
                <th>#</th>
                <th>SYMBOL</th>
                <th>SIDE</th>
                <th>TYPE</th>
                <th>PRICE</th>
                <th>QTY</th>
                <th>FILLED</th>
                <th>STATUS</th>
                <th>TIME</th>
                <th>ACTION</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((order) => (
                <tr key={order.id} className="op-row">
                  <td className="op-id">#{order.id}</td>
                  <td className="op-symbol">{order.symbol}</td>
                  <td>
                    <span className={`op-side ${sideColor(order.side)}`}>
                      {order.side}
                    </span>
                  </td>
                  <td className="op-type">{order.type}</td>
                  <td className="op-price">₹{order.price?.toFixed(2)}</td>
                  <td>{order.quantity}</td>
                  <td>
                    <div className="op-fill-wrap">
                      <span>{order.filledQuantity}/{order.quantity}</span>
                      <div className="op-fill-bar">
                        <div
                          className="op-fill-progress"
                          style={{ width: `${(order.filledQuantity / order.quantity) * 100}%` }}
                        />
                      </div>
                    </div>
                  </td>
                  <td>
                    <span className={`op-status ${statusColor(order.status)}`}>
                      {order.status}
                    </span>
                  </td>
                  <td className="op-time">
                    {new Date(order.createdAt).toLocaleTimeString('en-IN', {
                      hour: '2-digit', minute: '2-digit', second: '2-digit'
                    })}
                  </td>
                  <td>
                    {(order.status === 'PENDING' || order.status === 'PARTIAL') && (
                      <button
                        className="op-cancel-btn"
                        onClick={() => handleCancel(order.id)}
                        disabled={cancelling === order.id}
                      >
                        {cancelling === order.id ? '...' : 'Cancel'}
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
