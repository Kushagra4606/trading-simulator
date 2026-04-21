import { create } from 'zustand'
import axios from 'axios'

const API = 'http://localhost:8080/api'

// Attach JWT to every request automatically
axios.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

const useAuthStore = create((set) => ({
  // Initialize from localStorage so refresh doesn't lose session
  token: localStorage.getItem('token') || null,
  user: JSON.parse(localStorage.getItem('user')) || null,
  walletBalance: localStorage.getItem('walletBalance') || null,

  register: async (name, email, password) => {
    const { data } = await axios.post(`${API}/auth/register`, { name, email, password })
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify({ name: data.name, email: data.email }))
    localStorage.setItem('walletBalance', data.walletBalance)
    set({ token: data.token, user: { name: data.name, email: data.email }, walletBalance: data.walletBalance })
  },

  login: async (email, password) => {
    const { data } = await axios.post(`${API}/auth/login`, { email, password })
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify({ name: data.name, email: data.email }))
    localStorage.setItem('walletBalance', data.walletBalance)
    set({ token: data.token, user: { name: data.name, email: data.email }, walletBalance: data.walletBalance })
  },

  logout: () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    localStorage.removeItem('walletBalance')
    set({ token: null, user: null, walletBalance: null })
  },
}))

export default useAuthStore