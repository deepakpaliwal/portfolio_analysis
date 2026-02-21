import React, { useEffect, useState } from 'react';
import apiClient from '../api/client';

/* ── Types ── */

interface CorrelatedPair {
  ticker1: string;
  ticker2: string;
  name1: string;
  name2: string;
  correlation: number;
  riskLevel: string;
}

interface HedgeSuggestion {
  holdingTicker: string;
  holdingName: string;
  hedgeType: string;
  hedgeInstrument: string;
  description: string;
  expectedCorrelation: number;
}

interface RollingCorrelation {
  ticker1: string;
  ticker2: string;
  correlation30d: number | null;
  correlation90d: number | null;
  correlation1y: number | null;
  trend: string;
}

interface CorrelationData {
  portfolioId: number;
  portfolioName: string;
  holdingCount: number;
  lookbackDays: number;
  tickers: string[];
  tickerNames: string[];
  correlationMatrix: number[][];
  highlyCorrelatedPairs: CorrelatedPair[];
  negativelyCorrelatedPairs: CorrelatedPair[];
  hedgeSuggestions: HedgeSuggestion[];
  rollingCorrelations: Record<string, RollingCorrelation>;
  diversificationScore: number;
  diversificationRating: string;
}

interface Portfolio {
  id: number;
  name: string;
}

/* ── Helpers ── */

const fmt = (n: number | null | undefined, decimals = 4): string => {
  if (n == null || isNaN(n)) return '--';
  return n.toFixed(decimals);
};

const corrColor = (val: number): string => {
  // Red for high positive, blue for negative, white for zero
  if (val >= 0.9) return '#b71c1c';
  if (val >= 0.7) return '#e53935';
  if (val >= 0.5) return '#ef9a9a';
  if (val >= 0.3) return '#ffcdd2';
  if (val > -0.3) return '#f5f5f5';
  if (val > -0.5) return '#bbdefb';
  if (val > -0.7) return '#64b5f6';
  return '#1565c0';
};

const corrTextColor = (val: number): string => {
  return Math.abs(val) >= 0.7 ? '#fff' : '#333';
};

const card: React.CSSProperties = {
  background: '#ffffff',
  padding: '1.25rem',
  borderRadius: 14,
  boxShadow: '0 10px 24px rgba(15,23,42,0.08)',
  border: '1px solid #E2E8F0',
};

/* ── Component ── */

const CorrelationAnalysis: React.FC = () => {
  const [portfolios, setPortfolios] = useState<Portfolio[]>([]);
  const [selectedPortfolio, setSelectedPortfolio] = useState<number | null>(null);
  const [lookbackDays, setLookbackDays] = useState(252);
  const [data, setData] = useState<CorrelationData | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState<'heatmap' | 'pairs' | 'hedging' | 'rolling' | 'diversification'>('heatmap');

  useEffect(() => {
    apiClient.get('/v1/portfolios').then(res => {
      setPortfolios(res.data);
      if (res.data.length > 0) setSelectedPortfolio(res.data[0].id);
    }).catch(() => setError('Failed to load portfolios'));
  }, []);

  const analyze = async () => {
    if (!selectedPortfolio) return;
    setLoading(true);
    setError('');
    try {
      const res = await apiClient.get(`/v1/correlation/portfolio/${selectedPortfolio}`, {
        params: { lookbackDays },
        timeout: 60_000,
      });
      setData(res.data);
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      const msg = axiosErr?.response?.data?.message || (err instanceof Error ? err.message : 'Analysis failed');
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const tabStyle = (tab: string): React.CSSProperties => ({
    padding: '0.5rem 1.25rem',
    border: 'none',
    borderBottom: activeTab === tab ? '2px solid #1976d2' : '2px solid transparent',
    background: 'transparent',
    color: activeTab === tab ? '#1976d2' : '#666',
    fontWeight: activeTab === tab ? 600 : 400,
    cursor: 'pointer',
    fontSize: '0.9rem',
  });

  const scoreColor = (score: number): string => {
    if (score >= 80) return '#2e7d32';
    if (score >= 60) return '#43a047';
    if (score >= 40) return '#ff9800';
    if (score >= 20) return '#e65100';
    return '#c62828';
  };

  return (
    <div>
      <h1>Correlation & Hedging Analysis</h1>

      {/* Controls */}
      <div style={{ ...card, marginBottom: '1.5rem', display: 'flex', gap: '1rem', flexWrap: 'wrap', alignItems: 'flex-end' }}>
        <div>
          <label style={{ display: 'block', fontSize: '0.8rem', color: '#666', marginBottom: 4 }}>Portfolio</label>
          <select
            value={selectedPortfolio ?? ''}
            onChange={e => setSelectedPortfolio(Number(e.target.value))}
            style={{ padding: '0.5rem', borderRadius: 4, border: '1px solid #ccc', minWidth: 180 }}
          >
            {portfolios.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
          </select>
        </div>
        <div>
          <label style={{ display: 'block', fontSize: '0.8rem', color: '#666', marginBottom: 4 }}>Lookback</label>
          <select value={lookbackDays} onChange={e => setLookbackDays(Number(e.target.value))}
                  style={{ padding: '0.5rem', borderRadius: 4, border: '1px solid #ccc' }}>
            <option value={63}>3 Months</option>
            <option value={126}>6 Months</option>
            <option value={252}>1 Year</option>
            <option value={504}>2 Years</option>
          </select>
        </div>
        <button
          onClick={analyze}
          disabled={loading || !selectedPortfolio}
          style={{
            padding: '0.5rem 1.5rem', background: '#1976d2', color: '#fff', border: 'none',
            borderRadius: 4, cursor: loading ? 'wait' : 'pointer', fontWeight: 600,
          }}
        >
          {loading ? 'Analyzing...' : 'Analyze Correlations'}
        </button>
      </div>

      {error && <div style={{ color: '#e53935', marginBottom: '1rem' }}>{error}</div>}

      {loading && (
        <div style={{ textAlign: 'center', padding: '3rem', color: '#888' }}>
          Computing correlation matrix... This may take a moment.
        </div>
      )}

      {data && !loading && (
        <>
          {/* Summary Cards */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem', marginBottom: '1.5rem' }}>
            <div style={card}>
              <h3 style={{ color: '#888', fontSize: '0.8rem', margin: 0 }}>Holdings Analyzed</h3>
              <p style={{ fontSize: '1.4rem', fontWeight: 700, margin: '0.5rem 0' }}>{data.holdingCount}</p>
              <span style={{ fontSize: '0.75rem', color: '#888' }}>{data.portfolioName}</span>
            </div>
            <div style={card}>
              <h3 style={{ color: '#888', fontSize: '0.8rem', margin: 0 }}>Diversification Score</h3>
              <p style={{ fontSize: '1.4rem', fontWeight: 700, margin: '0.5rem 0', color: scoreColor(data.diversificationScore) }}>
                {fmt(data.diversificationScore, 0)}/100
              </p>
              <span style={{ fontSize: '0.75rem', color: scoreColor(data.diversificationScore), fontWeight: 600 }}>
                {data.diversificationRating}
              </span>
            </div>
            <div style={card}>
              <h3 style={{ color: '#888', fontSize: '0.8rem', margin: 0 }}>High Correlation Pairs</h3>
              <p style={{ fontSize: '1.4rem', fontWeight: 700, margin: '0.5rem 0', color: data.highlyCorrelatedPairs.length > 0 ? '#e53935' : '#43a047' }}>
                {data.highlyCorrelatedPairs.length}
              </p>
              <span style={{ fontSize: '0.75rem', color: '#888' }}>
                {data.highlyCorrelatedPairs.length > 0 ? 'Concentration risk detected' : 'No concentration risk'}
              </span>
            </div>
            <div style={card}>
              <h3 style={{ color: '#888', fontSize: '0.8rem', margin: 0 }}>Natural Hedges</h3>
              <p style={{ fontSize: '1.4rem', fontWeight: 700, margin: '0.5rem 0', color: '#1976d2' }}>
                {data.negativelyCorrelatedPairs.length}
              </p>
              <span style={{ fontSize: '0.75rem', color: '#888' }}>Negatively correlated pairs</span>
            </div>
          </div>

          {/* Tabs */}
          <div style={{ borderBottom: '1px solid #e0e0e0', marginBottom: '1.5rem', display: 'flex', gap: 0 }}>
            <button style={tabStyle('heatmap')} onClick={() => setActiveTab('heatmap')}>Correlation Heatmap</button>
            <button style={tabStyle('pairs')} onClick={() => setActiveTab('pairs')}>Correlated Pairs</button>
            <button style={tabStyle('hedging')} onClick={() => setActiveTab('hedging')}>Hedge Suggestions</button>
            <button style={tabStyle('rolling')} onClick={() => setActiveTab('rolling')}>Rolling Correlations</button>
            <button style={tabStyle('diversification')} onClick={() => setActiveTab('diversification')}>Diversification</button>
          </div>

          {/* ── Heatmap Tab (FR-CH-002) ── */}
          {activeTab === 'heatmap' && (
            <div style={card}>
              <h3 style={{ marginTop: 0 }}>Correlation Matrix Heatmap</h3>
              <p style={{ color: '#666', fontSize: '0.85rem', marginBottom: '1rem' }}>
                Pairwise Pearson correlation over {data.lookbackDays} trading days. Red = high positive, Blue = negative.
              </p>
              <div style={{ overflowX: 'auto' }}>
                <table style={{ borderCollapse: 'collapse' }}>
                  <thead>
                    <tr>
                      <th style={{ padding: '0.4rem 0.6rem', fontSize: '0.75rem' }}></th>
                      {data.tickers.map(t => (
                        <th key={t} style={{ padding: '0.4rem 0.6rem', fontSize: '0.75rem', fontWeight: 600, writingMode: 'vertical-rl', textOrientation: 'mixed', maxHeight: 100 }}>
                          {t}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {data.tickers.map((ticker, i) => (
                      <tr key={ticker}>
                        <td style={{ padding: '0.4rem 0.6rem', fontSize: '0.75rem', fontWeight: 600, whiteSpace: 'nowrap' }}>
                          {ticker}
                        </td>
                        {data.correlationMatrix[i].map((val, j) => (
                          <td
                            key={j}
                            style={{
                              padding: '0.4rem 0.6rem',
                              background: corrColor(val),
                              color: corrTextColor(val),
                              textAlign: 'center',
                              fontSize: '0.75rem',
                              fontWeight: i === j ? 700 : 400,
                              minWidth: 52,
                              border: '1px solid #e0e0e0',
                            }}
                            title={`${ticker} vs ${data.tickers[j]}: ${val.toFixed(4)}`}
                          >
                            {val.toFixed(2)}
                          </td>
                        ))}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              {/* Legend */}
              <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem', alignItems: 'center', fontSize: '0.75rem' }}>
                <span style={{ color: '#666' }}>Legend:</span>
                {[
                  { color: '#1565c0', label: '-1.0' },
                  { color: '#64b5f6', label: '-0.5' },
                  { color: '#f5f5f5', label: '0.0' },
                  { color: '#ef9a9a', label: '+0.5' },
                  { color: '#e53935', label: '+0.7' },
                  { color: '#b71c1c', label: '+1.0' },
                ].map(item => (
                  <div key={item.label} style={{ display: 'flex', alignItems: 'center', gap: 3 }}>
                    <div style={{ width: 16, height: 16, background: item.color, borderRadius: 2, border: '1px solid #ddd' }}></div>
                    <span>{item.label}</span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* ── Correlated Pairs Tab (FR-CH-003/004) ── */}
          {activeTab === 'pairs' && (
            <div style={{ display: 'grid', gap: '1.5rem' }}>
              <div style={card}>
                <h3 style={{ marginTop: 0, color: '#e53935' }}>Highly Correlated Pairs (Concentration Risk)</h3>
                <p style={{ color: '#666', fontSize: '0.85rem', marginBottom: '1rem' }}>
                  Pairs with correlation &gt; 0.7 indicate concentration risk — they tend to move together.
                </p>
                {data.highlyCorrelatedPairs.length === 0 ? (
                  <p style={{ color: '#43a047', fontWeight: 600 }}>No highly correlated pairs found. Portfolio is well diversified.</p>
                ) : (
                  <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                    <thead>
                      <tr style={{ borderBottom: '2px solid #e0e0e0' }}>
                        <th style={{ textAlign: 'left', padding: '0.6rem 0.5rem', color: '#666', fontSize: '0.8rem' }}>Pair</th>
                        <th style={{ textAlign: 'right', padding: '0.6rem 0.5rem', color: '#666', fontSize: '0.8rem' }}>Correlation</th>
                        <th style={{ textAlign: 'left', padding: '0.6rem 0.5rem', color: '#666', fontSize: '0.8rem' }}>Risk Level</th>
                      </tr>
                    </thead>
                    <tbody>
                      {data.highlyCorrelatedPairs.map((p, i) => (
                        <tr key={i} style={{ borderBottom: '1px solid #f0f0f0' }}>
                          <td style={{ padding: '0.6rem 0.5rem' }}>
                            <strong>{p.ticker1}</strong> / <strong>{p.ticker2}</strong>
                            <div style={{ fontSize: '0.75rem', color: '#888' }}>{p.name1} / {p.name2}</div>
                          </td>
                          <td style={{ padding: '0.6rem 0.5rem', textAlign: 'right', fontWeight: 600, color: '#e53935' }}>
                            {fmt(p.correlation)}
                          </td>
                          <td style={{ padding: '0.6rem 0.5rem' }}>
                            <span style={{
                              padding: '2px 8px', borderRadius: 12, fontSize: '0.75rem',
                              background: p.riskLevel === 'Very High' ? '#ffcdd2' : '#ffe0b2',
                              color: p.riskLevel === 'Very High' ? '#c62828' : '#e65100',
                              fontWeight: 600,
                            }}>{p.riskLevel}</span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>

              <div style={card}>
                <h3 style={{ marginTop: 0, color: '#1976d2' }}>Negatively Correlated Pairs (Natural Hedges)</h3>
                <p style={{ color: '#666', fontSize: '0.85rem', marginBottom: '1rem' }}>
                  Pairs with correlation &lt; -0.3 provide natural hedging within your portfolio.
                </p>
                {data.negativelyCorrelatedPairs.length === 0 ? (
                  <p style={{ color: '#888' }}>No negatively correlated pairs found. Consider adding hedge positions.</p>
                ) : (
                  <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                    <thead>
                      <tr style={{ borderBottom: '2px solid #e0e0e0' }}>
                        <th style={{ textAlign: 'left', padding: '0.6rem 0.5rem', color: '#666', fontSize: '0.8rem' }}>Pair</th>
                        <th style={{ textAlign: 'right', padding: '0.6rem 0.5rem', color: '#666', fontSize: '0.8rem' }}>Correlation</th>
                        <th style={{ textAlign: 'left', padding: '0.6rem 0.5rem', color: '#666', fontSize: '0.8rem' }}>Hedge Strength</th>
                      </tr>
                    </thead>
                    <tbody>
                      {data.negativelyCorrelatedPairs.map((p, i) => (
                        <tr key={i} style={{ borderBottom: '1px solid #f0f0f0' }}>
                          <td style={{ padding: '0.6rem 0.5rem' }}>
                            <strong>{p.ticker1}</strong> / <strong>{p.ticker2}</strong>
                            <div style={{ fontSize: '0.75rem', color: '#888' }}>{p.name1} / {p.name2}</div>
                          </td>
                          <td style={{ padding: '0.6rem 0.5rem', textAlign: 'right', fontWeight: 600, color: '#1565c0' }}>
                            {fmt(p.correlation)}
                          </td>
                          <td style={{ padding: '0.6rem 0.5rem' }}>
                            <span style={{
                              padding: '2px 8px', borderRadius: 12, fontSize: '0.75rem',
                              background: '#e3f2fd', color: '#1565c0', fontWeight: 600,
                            }}>{p.riskLevel}</span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </div>
          )}

          {/* ── Hedge Suggestions Tab (FR-CH-005) ── */}
          {activeTab === 'hedging' && (
            <div style={card}>
              <h3 style={{ marginTop: 0 }}>Hedge Suggestions</h3>
              <p style={{ color: '#666', fontSize: '0.85rem', marginBottom: '1rem' }}>
                Recommended hedge instruments based on your holdings' sectors and overall portfolio exposure.
              </p>
              <div style={{ display: 'grid', gap: '0.75rem' }}>
                {data.hedgeSuggestions.map((s, i) => (
                  <div key={i} style={{
                    background: '#fafafa', padding: '1rem', borderRadius: 8,
                    display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                    borderLeft: `4px solid ${s.holdingTicker === 'PORTFOLIO' ? '#1976d2' : '#ff9800'}`,
                  }}>
                    <div style={{ flex: 1 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                        <span style={{
                          padding: '2px 8px', borderRadius: 12, fontSize: '0.7rem',
                          background: s.hedgeType === 'Put Option' ? '#e8eaf6' :
                                      s.hedgeType === 'Inverse ETF' ? '#fce4ec' :
                                      s.hedgeType === 'Volatility' ? '#fff3e0' : '#e8f5e9',
                          color: s.hedgeType === 'Put Option' ? '#283593' :
                                 s.hedgeType === 'Inverse ETF' ? '#c62828' :
                                 s.hedgeType === 'Volatility' ? '#e65100' : '#2e7d32',
                          fontWeight: 600,
                        }}>{s.hedgeType}</span>
                        <span style={{ fontSize: '0.8rem', color: '#888' }}>
                          for <strong>{s.holdingTicker}</strong> ({s.holdingName})
                        </span>
                      </div>
                      <div style={{ fontWeight: 600, fontSize: '0.95rem' }}>{s.hedgeInstrument}</div>
                      <div style={{ fontSize: '0.8rem', color: '#666', marginTop: 2 }}>{s.description}</div>
                    </div>
                    <div style={{ textAlign: 'right', minWidth: 100 }}>
                      <div style={{ fontSize: '0.75rem', color: '#888' }}>Expected Corr.</div>
                      <div style={{ fontSize: '1.1rem', fontWeight: 700, color: '#1565c0' }}>
                        {fmt(s.expectedCorrelation, 2)}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* ── Rolling Correlations Tab (FR-CH-006) ── */}
          {activeTab === 'rolling' && (
            <div style={card}>
              <h3 style={{ marginTop: 0 }}>Rolling Correlations</h3>
              <p style={{ color: '#666', fontSize: '0.85rem', marginBottom: '1rem' }}>
                Correlation over different time windows for the top pairs. Trends indicate regime changes.
              </p>
              {Object.keys(data.rollingCorrelations).length === 0 ? (
                <p style={{ color: '#888' }}>Insufficient data for rolling correlation analysis.</p>
              ) : (
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                  <thead>
                    <tr style={{ borderBottom: '2px solid #e0e0e0' }}>
                      <th style={{ textAlign: 'left', padding: '0.6rem 0.5rem', color: '#666', fontSize: '0.8rem' }}>Pair</th>
                      <th style={{ textAlign: 'right', padding: '0.6rem 0.5rem', color: '#666', fontSize: '0.8rem' }}>30-Day</th>
                      <th style={{ textAlign: 'right', padding: '0.6rem 0.5rem', color: '#666', fontSize: '0.8rem' }}>90-Day</th>
                      <th style={{ textAlign: 'right', padding: '0.6rem 0.5rem', color: '#666', fontSize: '0.8rem' }}>1-Year</th>
                      <th style={{ textAlign: 'left', padding: '0.6rem 0.5rem', color: '#666', fontSize: '0.8rem' }}>Trend</th>
                    </tr>
                  </thead>
                  <tbody>
                    {Object.entries(data.rollingCorrelations).map(([key, rc]) => (
                      <tr key={key} style={{ borderBottom: '1px solid #f0f0f0' }}>
                        <td style={{ padding: '0.6rem 0.5rem', fontWeight: 600 }}>{key}</td>
                        <td style={{ padding: '0.6rem 0.5rem', textAlign: 'right' }}>
                          {rc.correlation30d != null ? fmt(rc.correlation30d) : '--'}
                        </td>
                        <td style={{ padding: '0.6rem 0.5rem', textAlign: 'right' }}>
                          {rc.correlation90d != null ? fmt(rc.correlation90d) : '--'}
                        </td>
                        <td style={{ padding: '0.6rem 0.5rem', textAlign: 'right' }}>
                          {rc.correlation1y != null ? fmt(rc.correlation1y) : '--'}
                        </td>
                        <td style={{ padding: '0.6rem 0.5rem' }}>
                          <span style={{
                            padding: '2px 8px', borderRadius: 12, fontSize: '0.75rem', fontWeight: 600,
                            background: rc.trend === 'Increasing' ? '#ffcdd2' :
                                        rc.trend === 'Decreasing' ? '#c8e6c9' : '#f5f5f5',
                            color: rc.trend === 'Increasing' ? '#c62828' :
                                   rc.trend === 'Decreasing' ? '#2e7d32' : '#666',
                          }}>{rc.trend}</span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          )}

          {/* ── Diversification Tab (FR-CH-007) ── */}
          {activeTab === 'diversification' && (
            <div style={card}>
              <h3 style={{ marginTop: 0 }}>Portfolio Diversification Analysis</h3>
              <div style={{ textAlign: 'center', padding: '2rem' }}>
                <div style={{ fontSize: '3rem', fontWeight: 700, color: scoreColor(data.diversificationScore) }}>
                  {fmt(data.diversificationScore, 0)}
                </div>
                <div style={{ fontSize: '1.2rem', fontWeight: 600, color: scoreColor(data.diversificationScore), marginBottom: '0.5rem' }}>
                  {data.diversificationRating}
                </div>
                <div style={{ fontSize: '0.85rem', color: '#888', maxWidth: 500, margin: '0 auto' }}>
                  Score from 0-100 based on average absolute correlation between holdings.
                  Higher scores indicate better diversification (lower cross-asset correlation).
                </div>
              </div>

              <div style={{ marginTop: '2rem', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
                <div style={{ background: '#fafafa', padding: '1.25rem', borderRadius: 8 }}>
                  <h4 style={{ marginTop: 0, color: '#e53935' }}>Concentration Risks</h4>
                  {data.highlyCorrelatedPairs.length === 0 ? (
                    <p style={{ color: '#43a047', fontSize: '0.9rem' }}>No concentration risks found.</p>
                  ) : (
                    <ul style={{ margin: 0, paddingLeft: '1.2rem', fontSize: '0.9rem' }}>
                      {data.highlyCorrelatedPairs.slice(0, 5).map((p, i) => (
                        <li key={i} style={{ marginBottom: 4 }}>
                          <strong>{p.ticker1}/{p.ticker2}</strong>: {fmt(p.correlation, 2)} correlation
                        </li>
                      ))}
                    </ul>
                  )}
                </div>
                <div style={{ background: '#fafafa', padding: '1.25rem', borderRadius: 8 }}>
                  <h4 style={{ marginTop: 0, color: '#1976d2' }}>Recommendations</h4>
                  <ul style={{ margin: 0, paddingLeft: '1.2rem', fontSize: '0.9rem' }}>
                    {data.diversificationScore < 40 && (
                      <li style={{ marginBottom: 4 }}>Consider adding assets from different sectors or asset classes</li>
                    )}
                    {data.highlyCorrelatedPairs.length > 2 && (
                      <li style={{ marginBottom: 4 }}>Multiple highly correlated pairs detected — reduce sector concentration</li>
                    )}
                    {data.negativelyCorrelatedPairs.length === 0 && (
                      <li style={{ marginBottom: 4 }}>No natural hedges — consider adding bonds (TLT) or gold (GLD) for protection</li>
                    )}
                    {data.diversificationScore >= 60 && (
                      <li style={{ marginBottom: 4, color: '#43a047' }}>Portfolio shows good diversification across holdings</li>
                    )}
                    <li style={{ marginBottom: 4 }}>See the Hedge Suggestions tab for specific instrument recommendations</li>
                  </ul>
                </div>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default CorrelationAnalysis;
