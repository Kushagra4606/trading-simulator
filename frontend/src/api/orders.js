import axios from 'axios';
import useAuthStore from '../store/authStore';
const API = 'http://localhost:8080/api';


const getHeaders = () => ({
  headers: { Authorization: `Bearer ${useAuthStore.getState().token}` }
});


export const placeOrder = (orderData) =>
  axios.post(`${API}/orders`, orderData, getHeaders());

export const getMyOrders = () =>
  axios.get(`${API}/orders`, getHeaders());

export const cancelOrder = (orderId) =>
  axios.delete(`${API}/orders/${orderId}`, getHeaders());

export const getHoldings = () =>
  axios.get(`${API}/portfolio/holdings`, getHeaders());
