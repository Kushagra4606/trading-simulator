import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Register from './pages/Register'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import ProtectedRoute from './components/ProtectedRoute'
import StocksPage from './pages/StocksPage';
import StockDetailPage from './pages/StockDetailPage';
import OrdersPage    from './pages/OrdersPage';
import PortfolioPage from './pages/PortfolioPage';
import useWebSocketStore from './store/useWebSocketStore';
import useAuthStore from './store/authStore'; 
import NotificationToast from './components/NotificationToast';
import { useEffect } from 'react';

export default function App() {
  const token = useAuthStore((s) => s.token);
  const { connect, disconnect } = useWebSocketStore();
   useEffect(() => {
    if (token) {
      connect(token);
    } else {
      disconnect();
    }
    return () => disconnect(); // cleanup on unmount
  }, [token]);
  return (
    <>
    <NotificationToast />
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/login" />} />
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />

        <Route path="/stocks" element={
          <ProtectedRoute><StocksPage /></ProtectedRoute>
        } />
        <Route path="/stocks/:symbol" element={
          <ProtectedRoute><StockDetailPage /></ProtectedRoute>
        } />
        
        <Route path="/dashboard" element={
          <ProtectedRoute><Dashboard /></ProtectedRoute>
        } />
        <Route path="/orders" element={
          <ProtectedRoute><OrdersPage /></ProtectedRoute>
        } />
        <Route path="/portfolio" element={
          <ProtectedRoute><PortfolioPage /></ProtectedRoute>
        } />
      </Routes>
    </BrowserRouter>
    </>
  )
}