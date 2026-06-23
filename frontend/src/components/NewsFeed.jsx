import { useEffect } from 'react';
import useWebSocketStore from '../store/useWebSocketStore';
import './NewsFeed.css';

export default function NewsFeed({ initialNews = [] }) {
  const subscribeNews = useWebSocketStore((s) => s.subscribeNews);
  const connected     = useWebSocketStore((s) => s.connected);
  const liveNews      = useWebSocketStore((s) => s.newsEvents);

  // Merge: live events on top, initial (fetched from API) below, deduplicated
  const allNews = [
    ...liveNews,
    ...initialNews.filter(n => !liveNews.find(l => l.id === n.id)),
  ].slice(0, 20);

  useEffect(() => {
    if (!connected) return;
    const sub = subscribeNews();
    return () => sub?.unsubscribe();
  }, [connected]);

  const sentimentColor = (score) => {
    if (score > 0.1)  return '#3fb950'; // green
    if (score < -0.1) return '#f85149'; // red
    return '#8b949e';                   // neutral grey
  };

  const sentimentLabel = (score) => {
    if (score > 0.5)  return '▲ Bullish';
    if (score > 0.1)  return '↑ Positive';
    if (score < -0.5) return '▼ Bearish';
    if (score < -0.1) return '↓ Negative';
    return '→ Neutral';
  };

  const scopeBadgeColor = (scope) => {
    if (scope === 'COMPANY') return '#58a6ff';
    if (scope === 'SECTOR')  return '#e3b341';
    return '#bc8cff'; // MACRO
  };

  const formatTime = (createdAt) => {
    const date = new Date(createdAt);
    return date.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' });
  };

  return (
    <div className="news-feed">
      <div className="news-feed-header">
        <span className="news-feed-title">Market News</span>
        <span className={`news-live-dot ${connected ? 'live' : 'offline'}`}>
          {connected ? '● LIVE' : '○ Offline'}
        </span>
      </div>

      {allNews.length === 0 ? (
        <div className="news-empty">No news events yet. Waiting for market activity...</div>
      ) : (
        <div className="news-list">
          {allNews.map((event, idx) => (
            <div key={event.id ?? idx} className="news-item">
              <div className="news-item-top">
                <span
                  className="news-scope-badge"
                  style={{ color: scopeBadgeColor(event.scope) }}
                >
                  {event.scope}
                </span>
                {event.target && (
                  <span className="news-target">{event.target}</span>
                )}
                <span className="news-time">{formatTime(event.createdAt)}</span>
              </div>
              <div className="news-headline">{event.headline}</div>
              <div
                className="news-sentiment"
                style={{ color: sentimentColor(event.sentimentScore) }}
              >
                {sentimentLabel(event.sentimentScore)}
                <span className="news-score">
                  {event.sentimentScore > 0 ? '+' : ''}{parseFloat(event.sentimentScore).toFixed(2)}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
