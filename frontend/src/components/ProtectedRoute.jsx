import useAuthStore from '../store/authStore'
import { Navigate } from 'react-router-dom'
import Navbar from './Navbar'

export default function ProtectedRoute({ children }) {
  const token = useAuthStore(s => s.token)
  
  if (!token) {
    return <Navigate to="/login" replace />
  }

  return (
    <div className="app-container">
      <Navbar />
      <main className="main-content">
        {children}
      </main>
    </div>
  )
}