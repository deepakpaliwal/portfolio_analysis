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
  return { mid, upper, lower };
};

const TradingAdvisor: React.FC = () => {
  const [ticker, setTicker] = useState('AAPL');
  const [positionValue, setPositionValue] = useState('10000');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [data, setData] = useState<AdvisorData | null>(null);

  const runAdvisor = async () => {
    setLoading(true); setError('');
    try {
      const res = await apiClient.get(`/v1/advisor/${ticker.toUpperCase()}?positionValue=${positionValue}&lookbackDays=252`);
      setData(res.data);
    } catch {
      setError('Failed to run advisor for ticker');
    } finally { setLoading(false); }
  };

  const chartLines = useMemo(() => {
    if (!data?.chart?.length) return { price: '', ma20: '', ma50: '', bbUpper: '', bbLower: '' };

    const w = 760, h = 220;
    const close = data.chart.map((p) => p.close);
    const ma20 = movingAverage(close, 20);
    const ma50 = movingAverage(close, 50);
    const bb = bollinger(close, 20, 2);

    const all = [
      ...close,
      ...ma20.filter((v): v is number => v !== null),
      ...ma50.filter((v): v is number => v !== null),
      ...bb.upper.filter((v): v is number => v !== null),
      ...bb.lower.filter((v): v is number => v !== null),
    ];

    const min = Math.min(...all);
    const max = Math.max(...all);

    const mapPoints = (series: Array<number | null>) =>
      series
        .map((v, i) => {
          if (v === null) return '';
          const x = (i / (series.length - 1)) * w;
          const y = h - ((v - min) / ((max - min) || 1)) * h;
          return `${x},${y}`;
        })
        .filter(Boolean)
        .join(' ');

    return {
      price: mapPoints(close),
      ma20: mapPoints(ma20),
      ma50: mapPoints(ma50),
      bbUpper: mapPoints(bb.upper),
      bbLower: mapPoints(bb.lower),
    };
  }, [data]);

  return (
    <div>
      <h1>Single Stock Trading Advisor</h1>
      <div style={{ ...card, marginBottom: 12, display: 'flex', gap: 10, alignItems: 'end', flexWrap: 'wrap' }}>
        <div><label>Ticker</label><br /><input value={ticker} onChange={(e) => setTicker(e.target.value.toUpperCase())} /></div>
        <div><label>Position Value (USD)</label><br /><input value={positionValue} onChange={(e) => setPositionValue(e.target.value)} /></div>
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
            <h3 style={{ marginTop: 0 }}>Risk (VaR)</h3>
            <div>VaR 95%: ${data.risk.var95?.toFixed(2)}</div>
            <div>VaR 99%: ${data.risk.var99?.toFixed(2)}</div>
          </div>

          <div style={{ ...card, marginBottom: 12 }}>
            <h3 style={{ marginTop: 0 }}>Indicators</h3>
            <div>SMA20: {data.indicators.sma20?.toFixed(2)} | EMA20: {data.indicators.ema20?.toFixed(2)} | RSI14: {data.indicators.rsi14?.toFixed(2)}</div>
            <div>MACD: {data.indicators.macd?.toFixed(4)} | Signal: {data.indicators.signal9?.toFixed(4)} | Annual Vol: {(data.indicators.annualizedVolatility * 100).toFixed(2)}%</div>
          </div>

          <div style={card}>
            <h3 style={{ marginTop: 0 }}>Price Chart + Indicators</h3>
            <div style={{ fontSize: 12, color: '#475569', marginBottom: 8 }}>
              <span style={{ color: '#2563EB' }}>■ Price</span>{'  '}
              <span style={{ color: '#9333EA' }}>■ MA20</span>{'  '}
              <span style={{ color: '#EA580C' }}>■ MA50</span>{'  '}
              <span style={{ color: '#0EA5E9' }}>■ Bollinger Upper/Lower</span>
            </div>
            <svg viewBox="0 0 760 220" width="100%" height="240" style={{ background: '#F8FAFC', borderRadius: 8 }}>
              <polyline fill="none" stroke="#0EA5E9" strokeWidth="1.5" strokeDasharray="3 3" points={chartLines.bbUpper} />
              <polyline fill="none" stroke="#0EA5E9" strokeWidth="1.5" strokeDasharray="3 3" points={chartLines.bbLower} />
              <polyline fill="none" stroke="#EA580C" strokeWidth="1.8" points={chartLines.ma50} />
              <polyline fill="none" stroke="#9333EA" strokeWidth="1.8" points={chartLines.ma20} />
              <polyline fill="none" stroke="#2563EB" strokeWidth="2" points={chartLines.price} />
            </svg>
          </div>
        </>
      )}
    </div>
  );
};

export default TradingAdvisor;
