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
  token: null,
  user: null,
  walletBalance: null,

  register: async (name, email, password) => {
    const { data } = await axios.post(`${API}/auth/register`, { name, email, password })
    set({ token: data.token, user: { name: data.name, email: data.email }, walletBalance: data.walletBalance })
  },

  login: async (email, password) => {
    const { data } = await axios.post(`${API}/auth/login`, { email, password })
    set({ token: data.token, user: { name: data.name, email: data.email }, walletBalance: data.walletBalance })
  },

  logout: () => set({ token: null, user: null, walletBalance: null }),
}))

export default useAuthStore
