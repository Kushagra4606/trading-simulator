import { useState } from 'react'
import useAuthStore from '../store/authStore'
import { useNavigate } from 'react-router-dom'

export default function Register() {
  const [form, setForm] = useState({ name: '', email: '', password: '' })
  const [error, setError] = useState('')
  const register = useAuthStore(s => s.register)
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      await register(form.name, form.email, form.password)
      navigate('/dashboard')
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed')
    }
  }

  return (
    <div style={{ maxWidth: 400, margin: '80px auto' }}>
      <h2>Create Account</h2>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      <form onSubmit={handleSubmit}>
        <input placeholder="Name" value={form.name}
          onChange={e => setForm({...form, name: e.target.value})} /><br/>
        <input placeholder="Email" value={form.email}
          onChange={e => setForm({...form, email: e.target.value})} /><br/>
        <input placeholder="Password" type="password" value={form.password}
          onChange={e => setForm({...form, password: e.target.value})} /><br/>
        <button type="submit">Register</button>
      </form>
    </div>
  )
}