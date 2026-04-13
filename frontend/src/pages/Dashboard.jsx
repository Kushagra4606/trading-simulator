import { useNavigate } from 'react-router-dom'
import useAuthStore from '../store/authStore'

export default function Dashboard() {
  const { user, walletBalance, logout } = useAuthStore()
  const navigate = useNavigate()

  return (
    <div>
      {/* Navbar */}
      <nav style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '14px 32px',
        background: '#1e293b',
        color: '#fff'
      }}>
        <div style={{ fontWeight: '700', fontSize: '20px', color: '#60a5fa' }}>
          TradeSim
        </div>

        {/* Nav Links */}
        <div style={{ display: 'flex', gap: '24px' }}>
          <NavLink onClick={() => navigate('/dashboard')}>Dashboard</NavLink>
          <NavLink onClick={() => navigate('/stocks')}>Market</NavLink>
          <NavLink onClick={() => navigate('/orders')}>Orders</NavLink>
          <NavLink onClick={() => navigate('/portfolio')}>Portfolio</NavLink>
        </div>

        {/* Right side — wallet + logout */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
          <span style={{ fontSize: '14px', color: '#94a3b8' }}>
            Wallet: <strong style={{ color: '#34d399' }}>
              ₹{walletBalance?.toLocaleString('en-IN')}
            </strong>
          </span>
          <button
            onClick={logout}
            style={{
              padding: '6px 16px',
              background: '#ef4444',
              color: '#fff',
              border: 'none',
              borderRadius: '6px',
              cursor: 'pointer',
              fontWeight: '500'
            }}
          >
            Logout
          </button>
        </div>
      </nav>

      {/* Page Content */}
      <div style={{ padding: '40px' }}>
        <h1>Welcome, {user?.name} </h1>
        <div style={{ fontSize: '24px', marginTop: '20px' }}>
          Wallet Balance: <strong>₹{walletBalance?.toLocaleString('en-IN')}</strong>
        </div>
        <div style={{ marginTop: '32px' }}>
          <button
            onClick={() => navigate('/stocks')}
            style={{
              padding: '12px 28px',
              background: '#1d4ed8',
              color: '#fff',
              border: 'none',
              borderRadius: '8px',
              cursor: 'pointer',
              fontSize: '16px',
              fontWeight: '600'
            }}
          >
            Browse Market →
          </button>
        </div>
      </div>
    </div>
  )
}

function NavLink({ onClick, children }) {
  return (
    <span
      onClick={onClick}
      style={{
        cursor: 'pointer',
        color: '#cbd5e1',
        fontWeight: '500',
        fontSize: '15px',
        padding: '4px 0',
        borderBottom: '2px solid transparent',
      }}
      onMouseEnter={e => e.target.style.color = '#fff'}
      onMouseLeave={e => e.target.style.color = '#cbd5e1'}
    >
      {children}
    </span>
  )
}