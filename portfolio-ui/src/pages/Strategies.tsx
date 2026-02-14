import React, { useEffect, useState } from 'react';
import apiClient from '../api/client';

/* ── Types ── */

interface StrategyParam { name: string; label: string; type: string; defaultValue: string; description: string; }
interface StrategyDef { id: string; name: string; category: string; description: string; riskLevel: string; suitableFor: string[]; parameters: StrategyParam[]; }
interface TradeRecord { entryDate: string; exitDate: string; signal: string; entryPrice: number; exitPrice: number; returnPct: number; }
interface BacktestResult {
  strategyId: string; strategyName: string; ticker: string; lookbackDays: number;
  cagr: number; totalReturn: number; maxDrawdown: number; sharpeRatio: number; sortinoRatio: number;
  winRate: number; totalTrades: number; winningTrades: number; losingTrades: number;
  avgWin: number; avgLoss: number; profitFactor: number;
  benchmarkReturn: number; alpha: number; trades: TradeRecord[];
}
interface TradeSignal {
  ticker: string; holdingName: string; signal: string; rationale: string; strategySource: string;
  confidence: number; currentPrice: number; targetPrice: number; stopLoss: number;
}
interface TaxLossCandidate {
  ticker: string; name: string; purchasePrice: number; currentPrice: number;
  unrealizedLoss: number; unrealizedLossPct: number; suggestion: string;
}
interface PortfolioSignals { portfolioId: number; portfolioName: string; signals: TradeSignal[]; taxLossCandidates: TaxLossCandidate[]; }
interface Portfolio { id: number; name: string; }

/* ── Helpers ── */
const fmt = (n: number | null | undefined, d = 2): string => { if (n == null || isNaN(n)) return '--'; return n.toFixed(d); };
const fmtUsd = (n: number | null | undefined): string => { if (n == null || isNaN(n)) return '$--'; return '$' + Math.abs(n).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }); };
const pctColor = (v: number | null | undefined): string => { if (v == null) return '#333'; return v < 0 ? '#e53935' : v > 0 ? '#43a047' : '#333'; };
const signalColor = (s: string) => s === 'BUY' ? '#43a047' : s === 'SELL' ? '#e53935' : '#ff9800';
const card: React.CSSProperties = { background: '#fff', padding: '1.5rem', borderRadius: 8, boxShadow: '0 1px 3px rgba(0,0,0,0.1)' };
const riskColor = (r: string) => r === 'High' ? '#e53935' : r === 'Medium' ? '#ff9800' : '#43a047';

const Strategies: React.FC = () => {
  const [strategies, setStrategies] = useState<StrategyDef[]>([]);
  const [portfolios, setPortfolios] = useState<Portfolio[]>([]);
  const [selectedPortfolio, setSelectedPortfolio] = useState<number | null>(null);
  const [activeTab, setActiveTab] = useState<'catalog' | 'backtest' | 'signals'>('catalog');

  // Backtest state
  const [selectedStrategy, setSelectedStrategy] = useState('sma_crossover');
  const [ticker, setTicker] = useState('SPY');
  const [lookbackDays, setLookbackDays] = useState(504);
  const [backtestResult, setBacktestResult] = useState<BacktestResult | null>(null);
  const [backtesting, setBacktesting] = useState(false);

  // Signals state
  const [signalsData, setSignalsData] = useState<PortfolioSignals | null>(null);
  const [loadingSignals, setLoadingSignals] = useState(false);

  const [error, setError] = useState('');

  useEffect(() => {
    apiClient.get('/v1/strategies').then(r => setStrategies(r.data)).catch(() => {});
    apiClient.get('/v1/portfolios').then(r => {
      setPortfolios(r.data);
      if (r.data.length > 0) setSelectedPortfolio(r.data[0].id);
    }).catch(() => {});
  }, []);

  const runBacktest = async () => {
    setBacktesting(true); setError(''); setBacktestResult(null);
    try {
      const res = await apiClient.post('/v1/strategies/backtest', {
        strategyId: selectedStrategy, ticker: ticker.toUpperCase(), lookbackDays,
      }, { timeout: 60_000 });
      setBacktestResult(res.data);
    } catch (err: unknown) {
      const ax = err as { response?: { data?: { message?: string } } };
      setError(ax?.response?.data?.message || (err instanceof Error ? err.message : 'Backtest failed'));
    } finally { setBacktesting(false); }
  };

  const fetchSignals = async () => {
    if (!selectedPortfolio) return;
    setLoadingSignals(true); setError(''); setSignalsData(null);
    try {
      const res = await apiClient.get(`/v1/strategies/signals/portfolio/${selectedPortfolio}`, { timeout: 60_000 });
      setSignalsData(res.data);
    } catch (err: unknown) {
      const ax = err as { response?: { data?: { message?: string } } };
      setError(ax?.response?.data?.message || (err instanceof Error ? err.message : 'Failed to generate signals'));
    } finally { setLoadingSignals(false); }
  };

  const tabStyle = (tab: string): React.CSSProperties => ({
    padding: '0.5rem 1.25rem', border: 'none',
    borderBottom: activeTab === tab ? '2px solid #1976d2' : '2px solid transparent',
    background: 'transparent', color: activeTab === tab ? '#1976d2' : '#666',
    fontWeight: activeTab === tab ? 600 : 400, cursor: 'pointer', fontSize: '0.9rem',
  });

  return (
    <div>
      <h1>Strategy Engine</h1>

      {error && <div style={{ color: '#e53935', marginBottom: '1rem' }}>{error}</div>}

      <div style={{ borderBottom: '1px solid #e0e0e0', marginBottom: '1.5rem', display: 'flex', gap: 0 }}>
        <button style={tabStyle('catalog')} onClick={() => setActiveTab('catalog')}>Strategy Catalog</button>
        <button style={tabStyle('backtest')} onClick={() => setActiveTab('backtest')}>Backtest</button>
        <button style={tabStyle('signals')} onClick={() => setActiveTab('signals')}>Trade Signals</button>
      </div>

      {/* ── Catalog Tab ── */}
      {activeTab === 'catalog' && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))', gap: '1rem' }}>
          {strategies.map(s => (
            <div key={s.id} style={{ ...card, borderLeft: `4px solid ${riskColor(s.riskLevel)}` }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                <h3 style={{ margin: 0, fontSize: '1.05rem' }}>{s.name}</h3>
                <span style={{ padding: '2px 8px', borderRadius: 12, fontSize: '0.7rem', fontWeight: 600,
                  background: riskColor(s.riskLevel) + '20', color: riskColor(s.riskLevel) }}>{s.riskLevel} Risk</span>
              </div>
              <div style={{ fontSize: '0.75rem', color: '#888', marginBottom: 6 }}>{s.category}</div>
              <p style={{ fontSize: '0.85rem', color: '#555', margin: '0 0 0.75rem' }}>{s.description}</p>
              <div style={{ fontSize: '0.8rem', color: '#666', marginBottom: 8 }}>
                <strong>Suitable for:</strong> {s.suitableFor.join(', ')}
              </div>
              <div style={{ fontSize: '0.75rem', color: '#888' }}>
                Parameters: {s.parameters.map(p => p.label).join(', ')}
              </div>
              <button
                onClick={() => { setSelectedStrategy(s.id); setActiveTab('backtest'); }}
                style={{ marginTop: 12, padding: '0.4rem 1rem', background: '#1976d2', color: '#fff',
                  border: 'none', borderRadius: 4, cursor: 'pointer', fontSize: '0.8rem', fontWeight: 600 }}
              >Backtest This Strategy</button>
            </div>
          ))}
        </div>
      )}

      {/* ── Backtest Tab ── */}
      {activeTab === 'backtest' && (
        <>
          <div style={{ ...card, marginBottom: '1.5rem', display: 'flex', gap: '1rem', flexWrap: 'wrap', alignItems: 'flex-end' }}>
            <div>
              <label style={{ display: 'block', fontSize: '0.8rem', color: '#666', marginBottom: 4 }}>Strategy</label>
              <select value={selectedStrategy} onChange={e => setSelectedStrategy(e.target.value)}
                style={{ padding: '0.5rem', borderRadius: 4, border: '1px solid #ccc', minWidth: 180 }}>
                {strategies.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
              </select>
            </div>
            <div>
              <label style={{ display: 'block', fontSize: '0.8rem', color: '#666', marginBottom: 4 }}>Ticker</label>
              <input type="text" value={ticker} onChange={e => setTicker(e.target.value.toUpperCase())}
                style={{ padding: '0.5rem', borderRadius: 4, border: '1px solid #ccc', width: 100 }} />
            </div>
            <div>
              <label style={{ display: 'block', fontSize: '0.8rem', color: '#666', marginBottom: 4 }}>Lookback</label>
              <select value={lookbackDays} onChange={e => setLookbackDays(Number(e.target.value))}
                style={{ padding: '0.5rem', borderRadius: 4, border: '1px solid #ccc' }}>
                <option value={252}>1 Year</option>
                <option value={504}>2 Years</option>
                <option value={756}>3 Years</option>
                <option value={1260}>5 Years</option>
              </select>
            </div>
            <button onClick={runBacktest} disabled={backtesting || !ticker}
              style={{ padding: '0.5rem 1.5rem', background: '#1976d2', color: '#fff', border: 'none',
                borderRadius: 4, cursor: backtesting ? 'wait' : 'pointer', fontWeight: 600 }}>
              {backtesting ? 'Running...' : 'Run Backtest'}
            </button>
          </div>

          {backtesting && <div style={{ textAlign: 'center', padding: '3rem', color: '#888' }}>Running backtest...</div>}

          {backtestResult && !backtesting && (
            <>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))', gap: '0.75rem', marginBottom: '1.5rem' }}>
                {[
                  { label: 'Total Return', value: `${fmt(backtestResult.totalReturn)}%`, color: pctColor(backtestResult.totalReturn) },
                  { label: 'CAGR', value: `${fmt(backtestResult.cagr)}%`, color: pctColor(backtestResult.cagr) },
                  { label: 'Max Drawdown', value: `${fmt(backtestResult.maxDrawdown)}%`, color: '#e53935' },
                  { label: 'Sharpe Ratio', value: fmt(backtestResult.sharpeRatio), color: pctColor(backtestResult.sharpeRatio) },
                  { label: 'Win Rate', value: `${fmt(backtestResult.winRate, 1)}%`, color: backtestResult.winRate > 50 ? '#43a047' : '#e53935' },
                  { label: 'Total Trades', value: String(backtestResult.totalTrades), color: '#333' },
                  { label: 'Profit Factor', value: fmt(backtestResult.profitFactor), color: pctColor(backtestResult.profitFactor - 1) },
                  { label: 'Alpha vs SPY', value: backtestResult.alpha != null ? `${fmt(backtestResult.alpha)}%` : '--', color: pctColor(backtestResult.alpha) },
                ].map(item => (
                  <div key={item.label} style={card}>
                    <div style={{ fontSize: '0.75rem', color: '#888' }}>{item.label}</div>
                    <div style={{ fontSize: '1.2rem', fontWeight: 700, color: item.color, marginTop: 4 }}>{item.value}</div>
                  </div>
                ))}
              </div>

              {/* Trade History */}
              <div style={card}>
                <h3 style={{ marginTop: 0 }}>Trade History ({backtestResult.trades.length} trades)</h3>
                {backtestResult.trades.length === 0 ? (
                  <p style={{ color: '#888' }}>No trades generated in this period.</p>
                ) : (
                  <div style={{ maxHeight: 400, overflowY: 'auto' }}>
                    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                      <thead>
                        <tr style={{ borderBottom: '2px solid #e0e0e0', position: 'sticky', top: 0, background: '#fff' }}>
                          <th style={{ textAlign: 'left', padding: '0.5rem', color: '#666', fontSize: '0.8rem' }}>Entry</th>
                          <th style={{ textAlign: 'left', padding: '0.5rem', color: '#666', fontSize: '0.8rem' }}>Exit</th>
                          <th style={{ textAlign: 'right', padding: '0.5rem', color: '#666', fontSize: '0.8rem' }}>Entry Price</th>
                          <th style={{ textAlign: 'right', padding: '0.5rem', color: '#666', fontSize: '0.8rem' }}>Exit Price</th>
                          <th style={{ textAlign: 'right', padding: '0.5rem', color: '#666', fontSize: '0.8rem' }}>Return</th>
                        </tr>
                      </thead>
                      <tbody>
                        {backtestResult.trades.map((t, i) => (
                          <tr key={i} style={{ borderBottom: '1px solid #f0f0f0' }}>
                            <td style={{ padding: '0.5rem', fontSize: '0.85rem' }}>{t.entryDate}</td>
                            <td style={{ padding: '0.5rem', fontSize: '0.85rem' }}>{t.exitDate}</td>
                            <td style={{ padding: '0.5rem', textAlign: 'right' }}>{fmtUsd(t.entryPrice)}</td>
                            <td style={{ padding: '0.5rem', textAlign: 'right' }}>{fmtUsd(t.exitPrice)}</td>
                            <td style={{ padding: '0.5rem', textAlign: 'right', fontWeight: 600, color: pctColor(t.returnPct) }}>
                              {fmt(t.returnPct)}%
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            </>
          )}
        </>
      )}

      {/* ── Signals Tab ── */}
      {activeTab === 'signals' && (
        <>
          <div style={{ ...card, marginBottom: '1.5rem', display: 'flex', gap: '1rem', alignItems: 'flex-end' }}>
            <div>
              <label style={{ display: 'block', fontSize: '0.8rem', color: '#666', marginBottom: 4 }}>Portfolio</label>
              <select value={selectedPortfolio ?? ''} onChange={e => setSelectedPortfolio(Number(e.target.value))}
                style={{ padding: '0.5rem', borderRadius: 4, border: '1px solid #ccc', minWidth: 180 }}>
                {portfolios.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
              </select>
            </div>
            <button onClick={fetchSignals} disabled={loadingSignals || !selectedPortfolio}
              style={{ padding: '0.5rem 1.5rem', background: '#1976d2', color: '#fff', border: 'none',
                borderRadius: 4, cursor: loadingSignals ? 'wait' : 'pointer', fontWeight: 600 }}>
              {loadingSignals ? 'Generating...' : 'Generate Signals'}
            </button>
          </div>

          {loadingSignals && <div style={{ textAlign: 'center', padding: '3rem', color: '#888' }}>Analyzing holdings...</div>}

          {signalsData && !loadingSignals && (
            <div style={{ display: 'grid', gap: '1.5rem' }}>
              {/* Trade Signals */}
              <div style={card}>
                <h3 style={{ marginTop: 0 }}>Trade Signals — {signalsData.portfolioName}</h3>
                {signalsData.signals.length === 0 ? (
                  <p style={{ color: '#888' }}>No trade signals at this time. All holdings appear neutral.</p>
                ) : (
                  <div style={{ display: 'grid', gap: '0.75rem' }}>
                    {signalsData.signals.map((s, i) => (
                      <div key={i} style={{
                        background: '#fafafa', padding: '1rem', borderRadius: 8,
                        borderLeft: `4px solid ${signalColor(s.signal)}`,
                        display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                      }}>
                        <div style={{ flex: 1 }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                            <span style={{ padding: '2px 10px', borderRadius: 12, fontSize: '0.75rem', fontWeight: 700,
                              background: signalColor(s.signal) + '20', color: signalColor(s.signal) }}>{s.signal}</span>
                            <strong>{s.ticker}</strong>
                            <span style={{ color: '#888', fontSize: '0.85rem' }}>{s.holdingName}</span>
                          </div>
                          <div style={{ fontSize: '0.85rem', color: '#555', marginBottom: 4 }}>{s.rationale}</div>
                          <div style={{ fontSize: '0.75rem', color: '#888' }}>Source: {s.strategySource} | Confidence: {fmt(s.confidence * 100, 0)}%</div>
                        </div>
                        <div style={{ textAlign: 'right', minWidth: 140 }}>
                          <div style={{ fontSize: '0.8rem', color: '#888' }}>Current: {fmtUsd(s.currentPrice)}</div>
                          {s.targetPrice && <div style={{ fontSize: '0.8rem', color: '#43a047' }}>Target: {fmtUsd(s.targetPrice)}</div>}
                          {s.stopLoss && <div style={{ fontSize: '0.8rem', color: '#e53935' }}>Stop: {fmtUsd(s.stopLoss)}</div>}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Tax-Loss Harvesting */}
              {signalsData.taxLossCandidates.length > 0 && (
                <div style={card}>
                  <h3 style={{ marginTop: 0, color: '#e65100' }}>Tax-Loss Harvesting Opportunities</h3>
                  <p style={{ fontSize: '0.85rem', color: '#666', marginBottom: '1rem' }}>
                    Holdings with unrealized losses that could be sold to offset capital gains.
                  </p>
                  <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                    <thead>
                      <tr style={{ borderBottom: '2px solid #e0e0e0' }}>
                        <th style={{ textAlign: 'left', padding: '0.5rem', color: '#666', fontSize: '0.8rem' }}>Ticker</th>
                        <th style={{ textAlign: 'right', padding: '0.5rem', color: '#666', fontSize: '0.8rem' }}>Purchase</th>
                        <th style={{ textAlign: 'right', padding: '0.5rem', color: '#666', fontSize: '0.8rem' }}>Current</th>
                        <th style={{ textAlign: 'right', padding: '0.5rem', color: '#666', fontSize: '0.8rem' }}>Loss</th>
                        <th style={{ textAlign: 'right', padding: '0.5rem', color: '#666', fontSize: '0.8rem' }}>Loss %</th>
                        <th style={{ textAlign: 'left', padding: '0.5rem', color: '#666', fontSize: '0.8rem' }}>Suggestion</th>
                      </tr>
                    </thead>
                    <tbody>
                      {signalsData.taxLossCandidates.map((t, i) => (
                        <tr key={i} style={{ borderBottom: '1px solid #f0f0f0' }}>
                          <td style={{ padding: '0.5rem', fontWeight: 600 }}>{t.ticker}
                            <div style={{ fontSize: '0.75rem', color: '#888', fontWeight: 400 }}>{t.name}</div>
                          </td>
                          <td style={{ padding: '0.5rem', textAlign: 'right' }}>{fmtUsd(t.purchasePrice)}</td>
                          <td style={{ padding: '0.5rem', textAlign: 'right' }}>{fmtUsd(t.currentPrice)}</td>
                          <td style={{ padding: '0.5rem', textAlign: 'right', color: '#e53935', fontWeight: 600 }}>{fmtUsd(t.unrealizedLoss)}</td>
                          <td style={{ padding: '0.5rem', textAlign: 'right', color: '#e53935' }}>{fmt(t.unrealizedLossPct)}%</td>
                          <td style={{ padding: '0.5rem', fontSize: '0.8rem', color: '#555', maxWidth: 250 }}>{t.suggestion}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default Strategies;
