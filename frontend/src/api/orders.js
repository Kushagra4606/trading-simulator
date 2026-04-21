import axios from 'axios';

const API = 'http://localhost:8080/api';


export const placeOrder = (orderData) =>
  axios.post(`${API}/orders`, orderData);

export const getMyOrders = () =>
  axios.get(`${API}/orders`);

export const cancelOrder = (orderId) =>
  axios.delete(`${API}/orders/${orderId}`);

export const getHoldings = () =>
  axios.get(`${API}/portfolio/holdings`);