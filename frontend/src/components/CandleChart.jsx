import { useEffect, useLayoutEffect, useRef, useState } from 'react';
import { createChart, CandlestickSeries, HistogramSeries, LineSeries } from 'lightweight-charts';
import axios from 'axios';

const TIMEFRAMES = ['1m', '5m', '15m', '1D'];

const INDICATORS = [
  { key: 'sma20',  label: 'SMA 20',  color: '#f59e0b', panel: 'main' },
  { key: 'sma50',  label: 'SMA 50',  color: '#3b82f6', panel: 'main' },
  { key: 'ema20',  label: 'EMA 20',  color: '#a78bfa', panel: 'main' },
  { key: 'bb',     label: 'BB',      color: '#6ee7b7', panel: 'main' },
  { key: 'vwap',   label: 'VWAP',    color: '#f97316', panel: 'main' },
  { key: 'rsi',    label: 'RSI',     color: '#fb923c', panel: 'rsi'  },
  { key: 'macd',   label: 'MACD',    color: '#60a5fa', panel: 'macd' },
];

const BASE_CHART_OPTS = {
  layout: {
    background: { color: '#0a0a0a' },
    textColor: '#9ca3af',
  },
  grid: {
    vertLines: { color: '#1a1a2e' },
    horzLines: { color: '#1a1a2e' },
  },
  timeScale: {
    timeVisible: true,
    secondsVisible: false,
    borderColor: '#2d2d2d',
  },
  rightPriceScale: { borderColor: '#2d2d2d' },
};

export default function CandleChart({ symbol }) {
  const containerRef = useRef(null);
  const mainRef  = useRef(null);
  const rsiRef   = useRef(null);
  const macdRef  = useRef(null);

  const mainChart  = useRef(null);
  const rsiChart   = useRef(null);
  const macdChart  = useRef(null);
  const series     = useRef({});

  const [timeframe, setTimeframe]           = useState('1m');
  const [activeInds, setActiveInds]         = useState({});
  const [status, setStatus]                 = useState('');
  const [loading, setLoading]               = useState(true);

  // ── Initialise charts once ───────────────────────────────────────────────
  useEffect(() => {
    if (!mainRef.current) return;

    mainChart.current = createChart(mainRef.current, {
      ...BASE_CHART_OPTS,
      autoSize: true,
      height: 360,
    });

    series.current.candle = mainChart.current.addSeries(CandlestickSeries, {
      upColor: '#22c55e', downColor: '#ef4444',
      borderUpColor: '#22c55e', borderDownColor: '#ef4444',
      wickUpColor: '#22c55e', wickDownColor: '#ef4444',
    });

    series.current.volume = mainChart.current.addSeries(HistogramSeries, {
      priceFormat: { type: 'volume' },
      priceScaleId: 'volume',
    });
    mainChart.current.priceScale('volume').applyOptions({
      scaleMargins: { top: 0.82, bottom: 0 },
    });

    return () => {
      mainChart.current?.remove();
      rsiChart.current?.remove();
      macdChart.current?.remove();
    };
  }, []);

  // ── Fetch & render on symbol / timeframe / indicator change ─────────────
    useEffect(() => {
      fetchAndRender();
      const interval = setInterval(fetchAndRender, 60000);
      return () => clearInterval(interval);
    }, [symbol, timeframe, activeInds]);

    
  async function fetchAndRender() {
    if (!series.current.candle) return;
    setLoading(true);
    setStatus('');

    try {
      const params = {
        timeframe,
        limit: 300,
        sma20: !!activeInds.sma20,
        sma50: !!activeInds.sma50,
        ema20: !!activeInds.ema20,
        bb:    !!activeInds.bb,
        vwap:  !!activeInds.vwap,
        rsi:   !!activeInds.rsi,
        macd:  !!activeInds.macd,
      };

      const { data } = await axios.get(`http://localhost:8080/api/candles/${symbol}`, { params });

      if (!data.candles?.length) {
        setStatus('No candle data yet — place some trades first.');
        setLoading(false);
        return;
      }

      const toUnix = (iso) => Math.floor(new Date(iso).getTime() / 1000);

      // ── Candles ───────────────────────────────────────────────────────────
      series.current.candle.setData(data.candles.map(c => ({
        time:  toUnix(c.time),
        open:  Number(c.open),
        high:  Number(c.high),
        low:   Number(c.low),
        close: Number(c.close),
      })));

      // ── Volume ────────────────────────────────────────────────────────────
      series.current.volume.setData(data.candles.map(c => ({
        time:  toUnix(c.time),
        value: c.volume,
        color: Number(c.close) >= Number(c.open) ? '#22c55e33' : '#ef444433',
      })));

      // ── Overlay indicators (on main chart) ───────────────────────────────
      const overlays = {
        sma20:    { color: '#f59e0b', width: 2 },
        sma50:    { color: '#3b82f6', width: 2 },
        ema20:    { color: '#a78bfa', width: 2 },
        vwap:     { color: '#f97316', width: 2 },
        bbUpper:  { color: '#6ee7b7', width: 1 },
        bbMiddle: { color: '#6ee7b788', width: 1 },
        bbLower:  { color: '#6ee7b7', width: 1 },
      };

      Object.entries(overlays).forEach(([key, opts]) => {
        const values = data.indicators?.[key];
        if (!values) {
          // indicator was turned off — remove series if exists
          if (series.current[key]) {
            mainChart.current.removeSeries(series.current[key]);
            delete series.current[key];
          }
          return;
        }
        if (!series.current[key]) {
          series.current[key] = mainChart.current.addSeries(LineSeries, {
            color: opts.color,
            lineWidth: opts.width,
            priceLineVisible: false,
            lastValueVisible: false,
          });
        }
        series.current[key].setData(
          values
            .map((v, i) => v != null
              ? { time: toUnix(data.candles[i].time), value: Number(v) }
              : null)
            .filter(Boolean)
        );
      });

      // ── RSI panel ─────────────────────────────────────────────────────────
      const rsiValues = data.indicators?.rsi;
      if (rsiValues) {
        if (!series.current.rsi) {
          series.current.rsi = rsiChart.current.addSeries(LineSeries, {
            color: '#fb923c', lineWidth: 2,
            priceLineVisible: false,
          });
          series.current.rsiOB = rsiChart.current.addSeries(LineSeries, {
            color: '#ef444466', lineWidth: 1,
            priceLineVisible: false, lastValueVisible: false,
          });
          series.current.rsiOS = rsiChart.current.addSeries(LineSeries, {
            color: '#22c55e66', lineWidth: 1,
            priceLineVisible: false, lastValueVisible: false,
          });
        }
        const rsiData = rsiValues
          .map((v, i) => v != null
            ? { time: toUnix(data.candles[i].time), value: Number(v) }
            : null)
          .filter(Boolean);

        series.current.rsi.setData(rsiData);
        series.current.rsiOB.setData(rsiData.map(d => ({ time: d.time, value: 70 })));
        series.current.rsiOS.setData(rsiData.map(d => ({ time: d.time, value: 30 })));
      } else {
        // RSI turned off — clean up
        ['rsi', 'rsiOB', 'rsiOS'].forEach(k => {
          if (series.current[k]) {
            if (rsiChart.current) rsiChart.current.removeSeries(series.current[k]);
            delete series.current[k];
          }
        });
      }

      // ── MACD panel ────────────────────────────────────────────────────────
      const macdLine = data.indicators?.macdLine;
      if (macdLine) {
        if (!series.current.macdLine) {
          series.current.macdLine   = macdChart.current.addSeries(LineSeries, {
            color: '#60a5fa', lineWidth: 2, priceLineVisible: false,
          });
          series.current.macdSignal = macdChart.current.addSeries(LineSeries, {
            color: '#f97316', lineWidth: 2, priceLineVisible: false,
          });
          series.current.macdHist   = macdChart.current.addSeries(HistogramSeries, {
            priceLineVisible: false,
          });
        }
        const toLine = (arr) => arr
          .map((v, i) => v != null
            ? { time: toUnix(data.candles[i].time), value: Number(v) }
            : null)
          .filter(Boolean);

        series.current.macdLine.setData(toLine(data.indicators.macdLine));
        series.current.macdSignal.setData(toLine(data.indicators.macdSignal));
        series.current.macdHist.setData(
          data.indicators.macdHistogram
            .map((v, i) => v != null ? {
              time:  toUnix(data.candles[i].time),
              value: Number(v),
              color: Number(v) >= 0 ? '#22c55e88' : '#ef444488',
            } : null)
            .filter(Boolean)
        );
      } else {
        ['macdLine', 'macdSignal', 'macdHist'].forEach(k => {
          if (series.current[k]) {
            if (macdChart.current) macdChart.current.removeSeries(series.current[k]);
            delete series.current[k];
          }
        });
      }

      
      mainChart.current.timeScale().fitContent();

  } catch (e) {
    console.error('Chart error:', e);
    // Only show error if we have no data at all
    if (!series.current.candle) {
      setStatus('Failed to load chart data.');
    }
  } finally {
    setLoading(false);
  }
  }

  function toggleIndicator(key) {
    setActiveInds(prev => {
      const next = { ...prev };
      if (next[key]) delete next[key];
      else next[key] = true;
      return next;
    });
  }

  return (
    <div ref={containerRef} style={{ background: '#0a0a0a', borderRadius: 12, padding: 16, overflow: 'hidden' , width: '100%'}}>

      {/* ── Controls ──────────────────────────────────────────────────────── */}
      <div style={{ display: 'flex', gap: 8, marginBottom: 12,
                    flexWrap: 'wrap', alignItems: 'center' }}>

        {/* Timeframe buttons */}
        <div style={{ display: 'flex', gap: 4 }}>
          {TIMEFRAMES.map(tf => (
            <button key={tf} onClick={() => setTimeframe(tf)} style={{
              padding: '4px 12px', borderRadius: 6, border: 'none',
              cursor: 'pointer', fontFamily: 'IBM Plex Mono, monospace',
              fontSize: 12,
              background: timeframe === tf ? '#3b82f6' : '#1f2937',
              color: timeframe === tf ? '#fff' : '#6b7280',
            }}>
              {tf}
            </button>
          ))}
        </div>

        {/* Divider */}
        <div style={{ width: 1, height: 20, background: '#2d2d2d' }} />

        {/* Indicator toggles */}
        {INDICATORS.map(ind => (
          <button key={ind.key} onClick={() => toggleIndicator(ind.key)} style={{
            padding: '4px 10px', borderRadius: 6, cursor: 'pointer',
            fontFamily: 'IBM Plex Mono, monospace', fontSize: 11,
            border: `1px solid ${activeInds[ind.key] ? ind.color : '#2d2d2d'}`,
            background: activeInds[ind.key] ? ind.color + '22' : 'transparent',
            color: activeInds[ind.key] ? ind.color : '#4b5563',
            transition: 'all 0.15s',
          }}>
            {ind.label}
          </button>
        ))}
      </div>

      {/* ── Status / loading ──────────────────────────────────────────────── */}
      {loading && (
        <div style={{ color: '#4b5563', fontFamily: 'IBM Plex Mono',
                      fontSize: 12, padding: '8px 0' }}>
          Loading...
        </div>
      )}
      {status && !loading && (
        <div style={{ color: '#6b7280', fontFamily: 'IBM Plex Mono',
                      fontSize: 12, textAlign: 'center', padding: 32 }}>
          {status}
        </div>
      )}

      {/* ── Main chart ────────────────────────────────────────────────────── */}
      <div ref={mainRef} />

      {/* ── RSI panel ─────────────────────────────────────────────────────── */}
      {activeInds.rsi && (
        <div style={{ marginTop: 4 }}>
          <div style={{ color: '#fb923c', fontFamily: 'IBM Plex Mono',
                        fontSize: 10, padding: '2px 0', marginBottom: 2 }}>
            RSI (14) — overbought: 70 / oversold: 30
          </div>
          <div ref={(el) => {
  if (el && !rsiChart.current) {
    rsiChart.current = createChart(el, {
      ...BASE_CHART_OPTS, autoSize: true, height: 120,
    });
    fetchAndRender();
  }
}} />
        </div>
      )}

      {/* ── MACD panel ────────────────────────────────────────────────────── */}
      {activeInds.macd && (
        <div style={{ marginTop: 4 }}>
          <div style={{ color: '#60a5fa', fontFamily: 'IBM Plex Mono',
                        fontSize: 10, padding: '2px 0', marginBottom: 2 }}>
            MACD (12, 26, 9) — blue: MACD  orange: Signal
          </div>
          <div ref={(el) => {
  if (el && !macdChart.current) {
    macdChart.current = createChart(el, {
      ...BASE_CHART_OPTS, autoSize: true, height: 120,
    });
    fetchAndRender();
  }
}} />
        </div>
      )}

    </div>
  );
}