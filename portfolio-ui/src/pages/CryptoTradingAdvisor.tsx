import React, { useMemo, useState } from 'react';
import apiClient from '../api/client';

interface AdvisorData {
  ticker: string;
  name: string | null;
  industry: string | null;
  currentPrice: number | null;
  changePercent: number | null;
  recordsSynced: number;
  storedRecords: number;
  recommendation: string;
  rationale: string;
  indicators: {
    sma20: number; ema20: number; rsi14: number; macd: number; signal9: number; annualizedVolatility: number;
  };
  risk: { var95: number; var99: number };
  chart: { date: string; close: number }[];
}

interface SignalPoint {
  x: number;
  y: number;
  type: 'BUY' | 'SELL';
  date: string;
  price: number;
}

const card: React.CSSProperties = { background: '#fff', border: '1px solid #E2E8F0', borderRadius: 12, padding: '1rem' };

const movingAverage = (values: number[], period: number): Array<number | null> => {
  const out: Array<number | null> = [];
  for (let i = 0; i < values.length; i++) {
    if (i < period - 1) {
      out.push(null);
      continue;
    }
    let sum = 0;
    for (let j = i - period + 1; j <= i; j++) sum += values[j];
    out.push(sum / period);
  }
  return out;
};

const bollinger = (values: number[], period = 20, k = 2) => {
  const mid = movingAverage(values, period);
  const upper: Array<number | null> = [];
  const lower: Array<number | null> = [];
  for (let i = 0; i < values.length; i++) {
    if (i < period - 1 || mid[i] === null) {
      upper.push(null);
      lower.push(null);
      continue;
    }
    const window = values.slice(i - period + 1, i + 1);
    const mean = mid[i] as number;
    const variance = window.reduce((acc, v) => acc + (v - mean) ** 2, 0) / period;
    const sd = Math.sqrt(variance);
    upper.push(mean + k * sd);
    lower.push(mean - k * sd);
  }
  return { upper, lower };
};

const CryptoTradingAdvisor: React.FC = () => {
  const [ticker, setTicker] = useState('BTC-USD');
  const [positionValue, setPositionValue] = useState('10000');
  const [lookbackDays, setLookbackDays] = useState('252');
  const [fastPeriod, setFastPeriod] = useState('20');
  const [slowPeriod, setSlowPeriod] = useState('50');
  const [bbPeriod, setBbPeriod] = useState('20');
  const [showPrice, setShowPrice] = useState(true);
  const [showFast, setShowFast] = useState(true);
  const [showSlow, setShowSlow] = useState(true);
  const [showBollinger, setShowBollinger] = useState(true);
  const [showSignals, setShowSignals] = useState(true);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [data, setData] = useState<AdvisorData | null>(null);

  const runAdvisor = async () => {
    setLoading(true); setError('');
    try {
      const res = await apiClient.get(`/v1/advisor/crypto/${ticker.toUpperCase()}?positionValue=${positionValue}&lookbackDays=${lookbackDays}`);
      setData(res.data);
    } catch {
      setError('Failed to run crypto advisor for ticker');
    } finally { setLoading(false); }
  };

  const chart = useMemo(() => {
    if (!data?.chart?.length) {
      return { price: '', fast: '', slow: '', bbUpper: '', bbLower: '', signals: [] as SignalPoint[] };
    }

    const w = 760, h = 220;
    const close = data.chart.map((p) => p.close);
    const fast = movingAverage(close, Math.max(2, Number(fastPeriod) || 20));
    const slow = movingAverage(close, Math.max(3, Number(slowPeriod) || 50));
    const bb = bollinger(close, Math.max(5, Number(bbPeriod) || 20), 2);

    const all = [
      ...close,
      ...fast.filter((v): v is number => v !== null),
      ...slow.filter((v): v is number => v !== null),
      ...bb.upper.filter((v): v is number => v !== null),
      ...bb.lower.filter((v): v is number => v !== null),
    ];

    const min = Math.min(...all);
    const max = Math.max(...all);

    const mapY = (v: number) => h - ((v - min) / ((max - min) || 1)) * h;

    const mapPoints = (series: Array<number | null>) =>
      series
        .map((v, i) => {
          if (v === null) return '';
          const x = (i / (series.length - 1)) * w;
          return `${x},${mapY(v)}`;
        })
        .filter(Boolean)
        .join(' ');

    const signals: SignalPoint[] = [];
    for (let i = 1; i < close.length; i++) {
      const fPrev = fast[i - 1], sPrev = slow[i - 1], fNow = fast[i], sNow = slow[i];
      if (fPrev === null || sPrev === null || fNow === null || sNow === null) continue;
      if (fPrev <= sPrev && fNow > sNow) {
        signals.push({ x: (i / (close.length - 1)) * w, y: mapY(close[i]), type: 'BUY', date: data.chart[i].date, price: close[i] });
      } else if (fPrev >= sPrev && fNow < sNow) {
        signals.push({ x: (i / (close.length - 1)) * w, y: mapY(close[i]), type: 'SELL', date: data.chart[i].date, price: close[i] });
      }
    }

    return {
      price: mapPoints(close),
      fast: mapPoints(fast),
      slow: mapPoints(slow),
      bbUpper: mapPoints(bb.upper),
      bbLower: mapPoints(bb.lower),
      signals,
    };
  }, [data, fastPeriod, slowPeriod, bbPeriod]);

  return (
    <div>
      <h1>Crypto Trading Advisor</h1>
      <div style={{ ...card, marginBottom: 12, display: 'flex', gap: 10, alignItems: 'end', flexWrap: 'wrap' }}>
        <div><label>Crypto Symbol</label><br /><input value={ticker} onChange={(e) => setTicker(e.target.value.toUpperCase())} /></div>
        <div><label>Position Value (USD)</label><br /><input value={positionValue} onChange={(e) => setPositionValue(e.target.value)} /></div>
        <div><label>Lookback Days</label><br /><input value={lookbackDays} onChange={(e) => setLookbackDays(e.target.value)} style={{ width: 110 }} /></div>
        <button onClick={runAdvisor} disabled={loading}>{loading ? 'Analyzing...' : 'Run Advisor'}</button>
      </div>

      {error && <div style={{ ...card, background: '#FEF2F2', color: '#991B1B', marginBottom: 12 }}>{error}</div>}

      {data && (
        <>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit,minmax(220px,1fr))', gap: 12, marginBottom: 12 }}>
            <div style={card}><strong>{data.ticker}</strong><div>{data.name}</div><div>{data.industry}</div></div>
            <div style={card}><strong>Price</strong><div>${data.currentPrice?.toFixed(2) ?? '--'}</div><div>{data.changePercent?.toFixed(2)}%</div></div>
            <div style={card}><strong>Recommendation</strong><div style={{ fontSize: '1.2rem' }}>{data.recommendation}</div><div>{data.rationale}</div></div>
            <div style={card}><strong>Data</strong><div>New Synced: {data.recordsSynced}</div><div>Total Stored: {data.storedRecords}</div></div>
          </div>

          <div style={{ ...card, marginBottom: 12 }}>
            <h3 style={{ marginTop: 0 }}>Chart Display Options</h3>
            <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', alignItems: 'center' }}>
              <label><input type="checkbox" checked={showPrice} onChange={(e) => setShowPrice(e.target.checked)} /> Price</label>
              <label><input type="checkbox" checked={showFast} onChange={(e) => setShowFast(e.target.checked)} /> MA Fast</label>
              <input value={fastPeriod} onChange={(e) => setFastPeriod(e.target.value)} style={{ width: 64 }} title="Fast MA period" />
              <label><input type="checkbox" checked={showSlow} onChange={(e) => setShowSlow(e.target.checked)} /> MA Slow</label>
              <input value={slowPeriod} onChange={(e) => setSlowPeriod(e.target.value)} style={{ width: 64 }} title="Slow MA period" />
              <label><input type="checkbox" checked={showBollinger} onChange={(e) => setShowBollinger(e.target.checked)} /> Bollinger</label>
              <input value={bbPeriod} onChange={(e) => setBbPeriod(e.target.value)} style={{ width: 64 }} title="Bollinger period" />
              <label><input type="checkbox" checked={showSignals} onChange={(e) => setShowSignals(e.target.checked)} /> Trading Signals</label>
            </div>
          </div>

          <div style={card}>
            <h3 style={{ marginTop: 0 }}>Price Chart + Indicators + Signals</h3>
            <svg viewBox="0 0 760 220" width="100%" height="240" style={{ background: '#F8FAFC', borderRadius: 8 }}>
              {showBollinger && <polyline fill="none" stroke="#0EA5E9" strokeWidth="1.4" strokeDasharray="3 3" points={chart.bbUpper} />}
              {showBollinger && <polyline fill="none" stroke="#0EA5E9" strokeWidth="1.4" strokeDasharray="3 3" points={chart.bbLower} />}
              {showSlow && <polyline fill="none" stroke="#EA580C" strokeWidth="1.8" points={chart.slow} />}
              {showFast && <polyline fill="none" stroke="#9333EA" strokeWidth="1.8" points={chart.fast} />}
              {showPrice && <polyline fill="none" stroke="#2563EB" strokeWidth="2" points={chart.price} />}
              {showSignals && chart.signals.map((s, idx) => (
                <g key={`${s.type}-${idx}`}>
                  <circle cx={s.x} cy={s.y} r={4.5} fill={s.type === 'BUY' ? '#16A34A' : '#DC2626'} />
                  <title>{`${s.type} | ${s.date} | $${s.price.toFixed(2)}`}</title>
                </g>
              ))}
            </svg>
            <div style={{ marginTop: 8, fontSize: 12, color: '#475569' }}>
              Signals are based on Fast/Slow moving-average crossovers for crypto price action.
            </div>
          </div>
        </>
      )}
    </div>
  );
};

export default CryptoTradingAdvisor;
