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

  const points = useMemo(() => {
    if (!data?.chart?.length) return '';
    const w = 760, h = 220;
    const values = data.chart.map((p) => p.close);
    const min = Math.min(...values), max = Math.max(...values);
    return data.chart.map((p, i) => {
      const x = (i / (data.chart.length - 1)) * w;
      const y = h - ((p.close - min) / ((max - min) || 1)) * h;
      return `${x},${y}`;
    }).join(' ');
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
            <h3 style={{ marginTop: 0 }}>Price Chart (1Y)</h3>
            <svg viewBox="0 0 760 220" width="100%" height="240" style={{ background: '#F8FAFC', borderRadius: 8 }}>
              <polyline fill="none" stroke="#2563EB" strokeWidth="2" points={points} />
            </svg>
          </div>
        </>
      )}
    </div>
  );
};

export default TradingAdvisor;
