// src/api/stocks.js
import axios from 'axios';

const API_BASE = 'http://localhost:8080/api';

export const getAllStocks = () =>
  axios.get(`${API_BASE}/stocks`);

export const getStockBySymbol = (symbol) =>
  axios.get(`${API_BASE}/stocks/${symbol}`);