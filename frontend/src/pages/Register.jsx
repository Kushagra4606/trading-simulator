import { useState } from 'react';
import useAuthStore from '../store/authStore';
import { useNavigate, Link } from 'react-router-dom';
import './Auth.css';

export default function Register() {
  const [form, setForm] = useState({ name: '', email: '', password: '' });
  const [error, setError] = useState('');
  const register = useAuthStore(s => s.register);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await register(form.name, form.email, form.password);
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed');
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h2 className="auth-title">Create Account</h2>
        <p className="auth-subtitle">Join TradeSim to start your journey.</p>
        
        {error && <div className="auth-error">{error}</div>}
        
        <form className="auth-form" onSubmit={handleSubmit}>
          <div className="input-group">
            <label>Full Name</label>
            <input 
              className="auth-input"
              type="text"
              placeholder="John Doe" 
              value={form.name}
              onChange={e => setForm({...form, name: e.target.value})} 
              required
            />
          </div>

          <div className="input-group">
            <label>Email Address</label>
            <input 
              className="auth-input"
              type="email"
              placeholder="you@example.com" 
              value={form.email}
              onChange={e => setForm({...form, email: e.target.value})} 
              required
            />
          </div>
          
          <div className="input-group">
            <label>Password</label>
            <input 
              className="auth-input"
              type="password" 
              placeholder="••••••••" 
              value={form.password}
              onChange={e => setForm({...form, password: e.target.value})} 
              required
            />
          </div>
          
          <button className="auth-btn" type="submit">Sign Up</button>
        </form>

        <div className="auth-footer">
          Already have an account? <Link to="/login" className="auth-link">Log in</Link>
        </div>
      </div>
    </div>
  );
}