import { create } from 'zustand';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const useWebSocketStore = create((set, get) => ({
  client: null,
  connected: false,
  ltpMap: {},           // { SYMBOL: price }
  notifications: [],    // order fill alerts

  connect: (token) => {
    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      connectHeaders: {
        Authorization: `Bearer ${token}`,  // sent in STOMP CONNECT frame
      },
      reconnectDelay: 5000,  // auto-reconnect every 5s if disconnected

      onConnect: () => {
        set({ connected: true });
        console.log('[WS] Connected');

        // Subscribe to personal order fill notifications
        client.subscribe('/user/queue/orders', (message) => {
          const notification = JSON.parse(message.body);
          set((state) => ({
            notifications: [notification, ...state.notifications].slice(0, 50),
          }));
        });
      },

      onDisconnect: () => {
        set({ connected: false });
        console.log('[WS] Disconnected');
      },

      onStompError: (frame) => {
        console.error('[WS] STOMP error:', frame);
      },
    });

    client.activate();
    set({ client });
  },

  disconnect: () => {
    const { client } = get();
    if (client) {
      client.deactivate();
      set({ client: null, connected: false });
    }
  },

  // Subscribe to price updates for a specific symbol
  subscribeLtp: (symbol) => {
    const { client } = get();
    if (!client || !client.connected) return null;

    const sub = client.subscribe(`/topic/price/${symbol}`, (message) => {
      const { price } = JSON.parse(message.body);
      set((state) => ({
        ltpMap: { ...state.ltpMap, [symbol]: price },
      }));
    });

    return sub; // caller must call sub.unsubscribe() on cleanup
  },

  clearNotifications: () => set({ notifications: [] }),
}));

export default useWebSocketStore;