import { useState } from 'react'
import useAuthStore from '../store/authStore'
import { useNavigate } from 'react-router-dom'

export default function Login() {
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const login = useAuthStore(s => s.login)
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      await login(form.email, form.password)
      navigate('/dashboard')
    } catch (err) {
      setError('Invalid credentials')
    }
  }

  return (
    <div style={{ maxWidth: 400, margin: '80px auto' }}>
      <h2>Login</h2>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      <form onSubmit={handleSubmit}>
        <input placeholder="Email" value={form.email}
          onChange={e => setForm({...form, email: e.target.value})} /><br/>
        <input placeholder="Password" type="password" value={form.password}
          onChange={e => setForm({...form, password: e.target.value})} /><br/>
        <button type="submit">Login</button>
      </form>
    </div>
  )
}