import { useEffect, useRef } from 'react';
import useWebSocketStore from '../store/useWebSocketStore';

export default function NotificationToast() {
  const notifications = useWebSocketStore((s) => s.notifications);
  const latest = notifications[0];
  const prevIdRef = useRef(null);

  useEffect(() => {
    if (!latest) return;
    if (latest.orderId === prevIdRef.current) return; // don't re-show same notification
    prevIdRef.current = latest.orderId;

    // Simple browser-native toast using a div
    const toast = document.createElement('div');
    toast.textContent = ` Order ${latest.status} — ${latest.symbol} | ${latest.filledQty} qty @ ₹${latest.price}`;
    toast.style.cssText = `
      position: fixed;
      bottom: 24px;
      right: 24px;
      background: #1f2937;
      color: #f9fafb;
      padding: 14px 20px;
      border-radius: 10px;
      font-family: IBM Plex Mono, monospace;
      font-size: 13px;
      z-index: 9999;
      box-shadow: 0 4px 20px rgba(0,0,0,0.4);
      border-left: 4px solid #10B981;
      transition: opacity 0.4s ease;
    `;
    document.body.appendChild(toast);

    // Fade out and remove after 4 seconds
    setTimeout(() => {
      toast.style.opacity = '0';
      setTimeout(() => document.body.removeChild(toast), 400);
    }, 4000);
  }, [latest?.orderId]);

  return null; // no persistent DOM — toasts are injected imperatively
}