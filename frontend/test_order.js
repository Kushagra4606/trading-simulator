import axios from 'axios';

async function test() {
  const API = 'http://localhost:8080/api';
  try {
    const regRes = await axios.post(`${API}/auth/register`, {
      name: 'TestUser', email: `test${Date.now()}@example.com`, password: 'password'
    });
    const token = regRes.data.token;
    console.log('Registered and got token:', token);
    
    // get my orders (should be empty, but test GET with token)
    const getRes = await axios.get(`${API}/orders`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    console.log('GET orders status:', getRes.status);
    
    // place an order
    const payload = {
      symbol: 'TCS', side: 'BUY', orderType: 'MARKET', quantity: 1, price: null
    };
    const postRes = await axios.post(`${API}/orders`, payload, {
      headers: { Authorization: `Bearer ${token}` }
    });
    console.log('POST order status:', postRes.status);
  } catch (error) {
    console.error('Error Status:', error.response?.status);
    console.error('Error Data:', error.response?.data);
    console.error('Error Config Headers:', error.config?.headers);
  }
}

test();
