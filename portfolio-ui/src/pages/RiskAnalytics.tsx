import React, { useEffect, useState } from 'react';
import apiClient from '../api/client';

/* ── Types ── */

interface VaRMetrics {
  historicalSimulation: number;
  parametric: number;
  monteCarlo: number;
}

interface HoldingBeta {
  ticker: string;
  name: string;
  beta: number;
  weight: number;
}

interface StressScenario {
  name: string;
  description: string;
  marketShockPercent: number;
  estimatedLoss: number;
  estimatedLossPercent: number;
}

interface MonteCarloResult {
  simulations: number;
  meanReturn: number;
  percentile5: number;
  percentile25: number;
  median: number;
  percentile75: number;
  percentile95: number;
}

interface RiskData {
  portfolioId: number;
  portfolioName: string;
  portfolioValue: number;
  baseCurrency: string;
  var: VaRMetrics;
  cvar95: number;
  cvar99: number;
  annualizedVolatility: number;
  dailyVolatility: number;
  portfolioBeta: number;
  holdingBetas: HoldingBeta[];
  portfolioAlpha: number;
  sharpeRatio: number;
  sortinoRatio: number;
  treynorRatio: number;
  maxDrawdown: number;
  maxDrawdownPeakDate: string;
  maxDrawdownTroughDate: string;
  stressTests: StressScenario[];
  monteCarlo: MonteCarloResult;
  confidenceLevel: number;
  timeHorizonDays: number;
  lookbackDays: number;
}

interface Portfolio {
  id: number;
  name: string;
}

/* ── Helpers ── */

const fmt = (n: number | null | undefined, decimals = 2): string => {
  if (n == null || isNaN(n)) return '--';
  return n.toLocaleString('en-US', { minimumFractionDigits: decimals, maximumFractionDigits: decimals });
};

const fmtUsd = (n: number | null | undefined): string => {
  if (n == null || isNaN(n)) return '$--';
  return '$' + Math.abs(n).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
};

const pctColor = (val: number | null | undefined): string => {
  if (val == null) return '#333';
  return val < 0 ? '#e53935' : val > 0 ? '#43a047' : '#333';
};

const card = {
  background: '#fff',
  padding: '1.5rem',
  borderRadius: 8,
  boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
};

/* ── Component ── */

const RiskAnalytics: React.FC = () => {
  const [portfolios, setPortfolios] = useState<Portfolio[]>([]);
  const [selectedPortfolio, setSelectedPortfolio] = useState<number | null>(null);
  const [confidenceLevel, setConfidenceLevel] = useState(0.95);
  const [timeHorizon, setTimeHorizon] = useState(1);
  const [lookbackDays, setLookbackDays] = useState(252);
  const [riskData, setRiskData] = useState<RiskData | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState<'overview' | 'var' | 'holdings' | 'stress' | 'montecarlo'>('overview');

  // Load portfolios on mount
  useEffect(() => {
    apiClient.get('/v1/portfolios').then(res => {
      setPortfolios(res.data);
      if (res.data.length > 0) setSelectedPortfolio(res.data[0].id);
    }).catch(() => setError('Failed to load portfolios'));
  }, []);

  const fetchRiskAnalytics = async () => {
    if (!selectedPortfolio) return;
    setLoading(true);
    setError('');
    try {
      const res = await apiClient.get(`/v1/risk/portfolio/${selectedPortfolio}`, {
        params: { confidenceLevel, timeHorizonDays: timeHorizon, lookbackDays },
      });
      setRiskData(res.data);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to compute risk analytics';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  // Auto-fetch when portfolio selection changes
  useEffect(() => {
    if (selectedPortfolio) fetchRiskAnalytics();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedPortfolio]);

  const tabStyle = (tab: string) => ({
    padding: '0.5rem 1.25rem',
    border: 'none',
    borderBottom: activeTab === tab ? '2px solid #1976d2' : '2px solid transparent',
    background: 'transparent',
    color: activeTab === tab ? '#1976d2' : '#666',
    fontWeight: activeTab === tab ? 600 : 400,
    cursor: 'pointer',
    fontSize: '0.9rem',
  });

  return (
    <div>
      <h1>Risk Analytics</h1>

      {/* ── Controls ── */}
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
          <label style={{ display: 'block', fontSize: '0.8rem', color: '#666', marginBottom: 4 }}>Confidence</label>
          <select value={confidenceLevel} onChange={e => setConfidenceLevel(Number(e.target.value))}
                  style={{ padding: '0.5rem', borderRadius: 4, border: '1px solid #ccc' }}>
            <option value={0.90}>90%</option>
            <option value={0.95}>95%</option>
            <option value={0.99}>99%</option>
          </select>
        </div>
        <div>
          <label style={{ display: 'block', fontSize: '0.8rem', color: '#666', marginBottom: 4 }}>Time Horizon</label>
          <select value={timeHorizon} onChange={e => setTimeHorizon(Number(e.target.value))}
                  style={{ padding: '0.5rem', borderRadius: 4, border: '1px solid #ccc' }}>
            <option value={1}>1 Day</option>
            <option value={10}>10 Days</option>
            <option value={30}>30 Days</option>
          </select>
        </div>
        <div>
          <label style={{ display: 'block', fontSize: '0.8rem', color: '#666', marginBottom: 4 }}>Lookback</label>
          <select value={lookbackDays} onChange={e => setLookbackDays(Number(e.target.value))}
                  style={{ padding: '0.5rem', borderRadius: 4, border: '1px solid #ccc' }}>
            <option value={126}>6 Months</option>
            <option value={252}>1 Year</option>
            <option value={504}>2 Years</option>
          </select>
        </div>
        <button
          onClick={fetchRiskAnalytics}
          disabled={loading || !selectedPortfolio}
          style={{
            padding: '0.5rem 1.5rem', background: '#1976d2', color: '#fff', border: 'none',
            borderRadius: 4, cursor: loading ? 'wait' : 'pointer', fontWeight: 600,
          }}
        >
          {loading ? 'Computing...' : 'Recalculate'}
        </button>
      </div>

      {error && <div style={{ color: '#e53935', marginBottom: '1rem' }}>{error}</div>}

      {loading && (
        <div style={{ textAlign: 'center', padding: '3rem', color: '#888' }}>
          Computing risk analytics... This may take a moment.
        </div>
      )}

      {riskData && !loading && (
        <>
          {/* ── Summary Cards ── */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem', marginBottom: '1.5rem' }}>
            <div style={card}>
              <h3 style={{ color: '#888', fontSize: '0.8rem', margin: 0 }}>Portfolio Value</h3>
              <p style={{ fontSize: '1.4rem', fontWeight: 700, margin: '0.5rem 0' }}>{fmtUsd(riskData.portfolioValue)}</p>
              <span style={{ fontSize: '0.75rem', color: '#888' }}>{riskData.portfolioName}</span>
            </div>
            <div style={card}>
              <h3 style={{ color: '#888', fontSize: '0.8rem', margin: 0 }}>VaR ({(riskData.confidenceLevel * 100).toFixed(0)}%)</h3>
              <p style={{ fontSize: '1.4rem', fontWeight: 700, margin: '0.5rem 0', color: '#e53935' }}>
                {fmtUsd(riskData.var?.historicalSimulation)}
              </p>
              <span style={{ fontSize: '0.75rem', color: '#888' }}>{riskData.timeHorizonDays}-day Historical Sim</span>
            </div>
            <div style={card}>
              <h3 style={{ color: '#888', fontSize: '0.8rem', margin: 0 }}>Max Drawdown</h3>
              <p style={{ fontSize: '1.4rem', fontWeight: 700, margin: '0.5rem 0', color: '#e53935' }}>
                {fmt(riskData.maxDrawdown * 100)}%
              </p>
              <span style={{ fontSize: '0.75rem', color: '#888' }}>
                {riskData.maxDrawdownPeakDate} to {riskData.maxDrawdownTroughDate}
              </span>
            </div>
            <div style={card}>
              <h3 style={{ color: '#888', fontSize: '0.8rem', margin: 0 }}>Sharpe Ratio</h3>
              <p style={{ fontSize: '1.4rem', fontWeight: 700, margin: '0.5rem 0', color: pctColor(riskData.sharpeRatio) }}>
                {fmt(riskData.sharpeRatio)}
              </p>
              <span style={{ fontSize: '0.75rem', color: '#888' }}>Risk-adjusted return</span>
            </div>
            <div style={card}>
              <h3 style={{ color: '#888', fontSize: '0.8rem', margin: 0 }}>Portfolio Beta</h3>
              <p style={{ fontSize: '1.4rem', fontWeight: 700, margin: '0.5rem 0' }}>
                {fmt(riskData.portfolioBeta)}
              </p>
              <span style={{ fontSize: '0.75rem', color: '#888' }}>vs S&P 500</span>
            </div>
            <div style={card}>
              <h3 style={{ color: '#888', fontSize: '0.8rem', margin: 0 }}>Annualized Vol</h3>
              <p style={{ fontSize: '1.4rem', fontWeight: 700, margin: '0.5rem 0' }}>
                {fmt(riskData.annualizedVolatility * 100)}%
              </p>
              <span style={{ fontSize: '0.75rem', color: '#888' }}>Daily: {fmt(riskData.dailyVolatility * 100)}%</span>
            </div>
          </div>

          {/* ── Tabs ── */}
          <div style={{ borderBottom: '1px solid #e0e0e0', marginBottom: '1.5rem', display: 'flex', gap: 0 }}>
            <button style={tabStyle('overview')} onClick={() => setActiveTab('overview')}>Overview</button>
            <button style={tabStyle('var')} onClick={() => setActiveTab('var')}>VaR Detail</button>
            <button style={tabStyle('holdings')} onClick={() => setActiveTab('holdings')}>Holdings Beta</button>
            <button style={tabStyle('stress')} onClick={() => setActiveTab('stress')}>Stress Tests</button>
            <button style={tabStyle('montecarlo')} onClick={() => setActiveTab('montecarlo')}>Monte Carlo</button>
          </div>

          {/* ── Overview Tab ── */}
          {activeTab === 'overview' && (
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <div style={card}>
                <h3 style={{ marginTop: 0 }}>Risk Ratios</h3>
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                  <tbody>
                    {[
                      ['Sharpe Ratio', riskData.sharpeRatio],
                      ['Sortino Ratio', riskData.sortinoRatio],
                      ['Treynor Ratio', riskData.treynorRatio],
                      ['Jensen\'s Alpha', riskData.portfolioAlpha],
                      ['Portfolio Beta', riskData.portfolioBeta],
                    ].map(([label, value]) => (
                      <tr key={String(label)} style={{ borderBottom: '1px solid #f0f0f0' }}>
                        <td style={{ padding: '0.6rem 0', color: '#555' }}>{label}</td>
                        <td style={{ padding: '0.6rem 0', textAlign: 'right', fontWeight: 600, color: pctColor(value as number) }}>
                          {fmt(value as number, 4)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <div style={card}>
                <h3 style={{ marginTop: 0 }}>Volatility & Drawdown</h3>
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                  <tbody>
                    {[
                      ['Annualized Volatility', `${fmt(riskData.annualizedVolatility * 100)}%`],
                      ['Daily Volatility', `${fmt(riskData.dailyVolatility * 100)}%`],
                      ['Max Drawdown', `${fmt(riskData.maxDrawdown * 100)}%`],
                      ['Drawdown Peak', riskData.maxDrawdownPeakDate || '--'],
                      ['Drawdown Trough', riskData.maxDrawdownTroughDate || '--'],
                      ['CVaR (95%)', fmtUsd(riskData.cvar95)],
                      ['CVaR (99%)', fmtUsd(riskData.cvar99)],
                    ].map(([label, value]) => (
                      <tr key={String(label)} style={{ borderBottom: '1px solid #f0f0f0' }}>
                        <td style={{ padding: '0.6rem 0', color: '#555' }}>{label}</td>
                        <td style={{ padding: '0.6rem 0', textAlign: 'right', fontWeight: 600 }}>{value}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* ── VaR Detail Tab ── */}
          {activeTab === 'var' && (
            <div style={card}>
              <h3 style={{ marginTop: 0 }}>Value at Risk — {riskData.timeHorizonDays}-Day, {(riskData.confidenceLevel * 100).toFixed(0)}% Confidence</h3>
              <p style={{ color: '#666', fontSize: '0.85rem', marginBottom: '1.5rem' }}>
                Maximum expected loss at the {(riskData.confidenceLevel * 100).toFixed(0)}% confidence level
                over a {riskData.timeHorizonDays}-day horizon using {riskData.lookbackDays} days of historical data.
              </p>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '1rem' }}>
                {[
                  { label: 'Historical Simulation', value: riskData.var?.historicalSimulation, desc: 'Based on actual historical return distribution' },
                  { label: 'Parametric (Variance-Covariance)', value: riskData.var?.parametric, desc: 'Assumes normally distributed returns' },
                  { label: 'Monte Carlo', value: riskData.var?.monteCarlo, desc: `Based on ${riskData.monteCarlo?.simulations?.toLocaleString()} simulations` },
                ].map(item => (
                  <div key={item.label} style={{ background: '#fafafa', padding: '1.25rem', borderRadius: 8, textAlign: 'center' }}>
                    <div style={{ fontSize: '0.8rem', color: '#888', marginBottom: 8 }}>{item.label}</div>
                    <div style={{ fontSize: '1.6rem', fontWeight: 700, color: '#e53935' }}>{fmtUsd(item.value)}</div>
                    <div style={{ fontSize: '0.75rem', color: '#aaa', marginTop: 6 }}>{item.desc}</div>
                  </div>
                ))}
              </div>
              <div style={{ marginTop: '1.5rem', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div style={{ background: '#fff3e0', padding: '1rem', borderRadius: 8 }}>
                  <div style={{ fontSize: '0.8rem', color: '#e65100' }}>CVaR / Expected Shortfall (95%)</div>
                  <div style={{ fontSize: '1.3rem', fontWeight: 700, color: '#e65100' }}>{fmtUsd(riskData.cvar95)}</div>
                  <div style={{ fontSize: '0.75rem', color: '#bf360c', marginTop: 4 }}>Average loss in the worst 5% of scenarios</div>
                </div>
                <div style={{ background: '#ffebee', padding: '1rem', borderRadius: 8 }}>
                  <div style={{ fontSize: '0.8rem', color: '#b71c1c' }}>CVaR / Expected Shortfall (99%)</div>
                  <div style={{ fontSize: '1.3rem', fontWeight: 700, color: '#b71c1c' }}>{fmtUsd(riskData.cvar99)}</div>
                  <div style={{ fontSize: '0.75rem', color: '#b71c1c', marginTop: 4 }}>Average loss in the worst 1% of scenarios</div>
                </div>
              </div>
            </div>
          )}

          {/* ── Holdings Beta Tab ── */}
          {activeTab === 'holdings' && (
            <div style={card}>
              <h3 style={{ marginTop: 0 }}>Holdings Beta vs S&P 500</h3>
              <p style={{ color: '#666', fontSize: '0.85rem', marginBottom: '1rem' }}>
                Portfolio Beta: <strong>{fmt(riskData.portfolioBeta)}</strong> — a beta {'>'} 1 means more volatile than the market.
              </p>
              <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                  <tr style={{ borderBottom: '2px solid #e0e0e0' }}>
                    <th style={{ textAlign: 'left', padding: '0.6rem 0.5rem', color: '#666', fontSize: '0.8rem' }}>Ticker</th>
                    <th style={{ textAlign: 'left', padding: '0.6rem 0.5rem', color: '#666', fontSize: '0.8rem' }}>Name</th>
                    <th style={{ textAlign: 'right', padding: '0.6rem 0.5rem', color: '#666', fontSize: '0.8rem' }}>Weight</th>
                    <th style={{ textAlign: 'right', padding: '0.6rem 0.5rem', color: '#666', fontSize: '0.8rem' }}>Beta</th>
                    <th style={{ textAlign: 'left', padding: '0.6rem 0.5rem', color: '#666', fontSize: '0.8rem' }}>Risk Level</th>
                  </tr>
                </thead>
                <tbody>
                  {(riskData.holdingBetas || []).map(h => {
                    const beta = h.beta;
                    let riskLabel = 'Market-like';
                    let riskColor = '#1976d2';
                    if (beta > 1.5) { riskLabel = 'High'; riskColor = '#e53935'; }
                    else if (beta > 1.1) { riskLabel = 'Above Avg'; riskColor = '#ff9800'; }
                    else if (beta < 0.5) { riskLabel = 'Low'; riskColor = '#43a047'; }
                    else if (beta < 0.9) { riskLabel = 'Below Avg'; riskColor = '#66bb6a'; }

                    return (
                      <tr key={h.ticker} style={{ borderBottom: '1px solid #f0f0f0' }}>
                        <td style={{ padding: '0.6rem 0.5rem', fontWeight: 600 }}>{h.ticker}</td>
                        <td style={{ padding: '0.6rem 0.5rem', color: '#555' }}>{h.name || h.ticker}</td>
                        <td style={{ padding: '0.6rem 0.5rem', textAlign: 'right' }}>{fmt(h.weight * 100)}%</td>
                        <td style={{ padding: '0.6rem 0.5rem', textAlign: 'right', fontWeight: 600 }}>{fmt(beta)}</td>
                        <td style={{ padding: '0.6rem 0.5rem' }}>
                          <span style={{
                            padding: '2px 8px', borderRadius: 12, fontSize: '0.75rem',
                            background: riskColor + '20', color: riskColor, fontWeight: 600,
                          }}>{riskLabel}</span>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}

          {/* ── Stress Tests Tab ── */}
          {activeTab === 'stress' && (
            <div style={card}>
              <h3 style={{ marginTop: 0 }}>Stress Testing — Historical Scenarios</h3>
              <p style={{ color: '#666', fontSize: '0.85rem', marginBottom: '1rem' }}>
                Estimated portfolio impact under historical market crash scenarios, scaled by portfolio beta ({fmt(riskData.portfolioBeta)}).
              </p>
              <div style={{ display: 'grid', gap: '1rem' }}>
                {(riskData.stressTests || []).map(s => (
                  <div key={s.name} style={{ background: '#fafafa', padding: '1.25rem', borderRadius: 8, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div style={{ flex: 1 }}>
                      <div style={{ fontWeight: 600, fontSize: '1rem' }}>{s.name}</div>
                      <div style={{ fontSize: '0.8rem', color: '#888', marginTop: 4 }}>{s.description}</div>
                      <div style={{ fontSize: '0.8rem', color: '#999', marginTop: 2 }}>Market shock: {fmt(s.marketShockPercent)}%</div>
                    </div>
                    <div style={{ textAlign: 'right', minWidth: 160 }}>
                      <div style={{ fontSize: '1.3rem', fontWeight: 700, color: '#e53935' }}>-{fmtUsd(s.estimatedLoss)}</div>
                      <div style={{ fontSize: '0.85rem', color: '#e53935' }}>{fmt(s.estimatedLossPercent)}%</div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* ── Monte Carlo Tab ── */}
          {activeTab === 'montecarlo' && riskData.monteCarlo && (
            <div style={card}>
              <h3 style={{ marginTop: 0 }}>Monte Carlo Simulation Results</h3>
              <p style={{ color: '#666', fontSize: '0.85rem', marginBottom: '1.5rem' }}>
                {riskData.monteCarlo.simulations.toLocaleString()} simulations over {riskData.timeHorizonDays}-day horizon.
              </p>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))', gap: '1rem', marginBottom: '1.5rem' }}>
                {[
                  { label: '5th Percentile', value: riskData.monteCarlo.percentile5, color: '#e53935' },
                  { label: '25th Percentile', value: riskData.monteCarlo.percentile25, color: '#ff9800' },
                  { label: 'Median', value: riskData.monteCarlo.median, color: '#1976d2' },
                  { label: 'Mean', value: riskData.monteCarlo.meanReturn, color: '#1976d2' },
                  { label: '75th Percentile', value: riskData.monteCarlo.percentile75, color: '#43a047' },
                  { label: '95th Percentile', value: riskData.monteCarlo.percentile95, color: '#2e7d32' },
                ].map(item => (
                  <div key={item.label} style={{ background: '#fafafa', padding: '1rem', borderRadius: 8, textAlign: 'center' }}>
                    <div style={{ fontSize: '0.75rem', color: '#888', marginBottom: 6 }}>{item.label}</div>
                    <div style={{ fontSize: '1.2rem', fontWeight: 700, color: item.color }}>
                      {fmt(item.value * 100)}%
                    </div>
                  </div>
                ))}
              </div>
              <div style={{ background: '#e3f2fd', padding: '1rem', borderRadius: 8 }}>
                <div style={{ fontSize: '0.85rem', color: '#1565c0' }}>
                  <strong>Interpretation:</strong> Based on {riskData.monteCarlo.simulations.toLocaleString()} Monte Carlo simulations,
                  over the next {riskData.timeHorizonDays} day(s) the portfolio return is expected to fall
                  between {fmt(riskData.monteCarlo.percentile5 * 100)}% and {fmt(riskData.monteCarlo.percentile95 * 100)}% with
                  90% probability. The median expected return is {fmt(riskData.monteCarlo.median * 100)}%.
                </div>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default RiskAnalytics;
