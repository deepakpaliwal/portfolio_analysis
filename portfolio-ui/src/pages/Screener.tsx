import React, { useEffect, useState } from 'react';
import apiClient from '../api/client';

type ScreenerTab = 'ticker' | 'sector' | 'custom' | 'saved';

// ── Ticker report types ──

interface PriceTarget { targetHigh: number; targetLow: number; targetMean: number; targetMedian: number; }
interface AnalystRec { period: string; strongBuy: number; buy: number; hold: number; sell: number; strongSell: number; }
interface EarningsData { period: string; actual: number | null; estimate: number | null; surprise: number | null; surprisePercent: number | null; }
interface FinancialStatement { period: string; year: number; quarter: string | null; revenue: number | null; netIncome: number | null; totalAssets: number | null; totalLiabilities: number | null; totalEquity: number | null; operatingCashFlow: number | null; }
interface SecFiling { form: string; filedDate: string; acceptedDate: string; reportUrl: string; }
interface TickerReport {
  ticker: string; name: string; industry: string; sector: string; country: string; currency: string; exchange: string; logo: string; weburl: string;
  currentPrice: number | null; previousClose: number | null; change: number | null; changePercent: number | null;
  weekHigh52: number | null; weekLow52: number | null; marketCap: number | null; peRatio: number | null; eps: number | null;
  dividendYield: number | null; beta: number | null; revenueGrowthTTM: number | null; earningsGrowthTTM: number | null;
  annualFinancials: FinancialStatement[]; quarterlyFinancials: FinancialStatement[];
  secFilings: SecFiling[]; recommendations: AnalystRec[]; priceTarget: PriceTarget | null; earnings: EarningsData[];
}

// ── Sector report types ──

interface StockPerformance { ticker: string; name: string; currentPrice: number | null; changePercent: number | null; marketCap: number | null; peRatio: number | null; }
interface RotationSignal { signal: string; description: string; }
interface SectorReport {
  sector: string; sectorPerformancePercent: number | null; spPerformancePercent: number | null;
  relativePerformancePercent: number | null; averagePE: number | null; averageDividendYield: number | null; stockCount: number;
  topPerformers: StockPerformance[]; bottomPerformers: StockPerformance[];
  rotationSignals: RotationSignal[];
}

// ── Custom screen types ──

interface ScreenedStock {
  ticker: string; name: string; sector: string; industry: string;
  currentPrice: number | null; changePercent: number | null; marketCap: number | null;
  peRatio: number | null; eps: number | null; dividendYield: number | null; beta: number | null;
  weekHigh52: number | null; weekLow52: number | null;
}
interface ScreenResult { totalMatches: number; stocks: ScreenedStock[]; }

interface SavedReportSummary { id: number; reportType: string; target: string; createdAt: string; }

// ── Helpers ──

const fmt = (v: number | null | undefined, d = 2) => v != null ? Number(v).toLocaleString(undefined, { minimumFractionDigits: d, maximumFractionDigits: d }) : 'N/A';
const fmtB = (v: number | null | undefined) => { if (v == null) return 'N/A'; if (Math.abs(v) >= 1e6) return (v / 1e6).toFixed(1) + 'T'; if (Math.abs(v) >= 1e3) return (v / 1e3).toFixed(1) + 'B'; return fmt(v, 1) + 'M'; };
const pctColor = (v: number | null | undefined) => v == null ? '#888' : v >= 0 ? '#2e7d32' : '#c62828';

const inputStyle = { width: '100%', padding: '0.5rem', border: '1px solid #ccc', borderRadius: 4, boxSizing: 'border-box' as const };
const btnStyle = { padding: '0.5rem 1rem', background: '#1a1a2e', color: '#fff', border: 'none', borderRadius: 4, cursor: 'pointer' as const, fontWeight: 600 as const };
const cardStyle = { background: '#ffffff', padding: '1.25rem', borderRadius: 14, boxShadow: '0 10px 24px rgba(15,23,42,0.08)', border: '1px solid #E2E8F0' };
const tabStyle = (active: boolean) => ({ padding: '0.5rem 1rem', border: 'none', cursor: 'pointer' as const, fontWeight: 600 as const, borderBottom: active ? '2px solid #1a1a2e' : '2px solid transparent', background: 'transparent', color: active ? '#1a1a2e' : '#888' });

const SECTORS = ['Technology', 'Healthcare', 'Financials', 'Consumer Discretionary', 'Consumer Staples', 'Energy', 'Industrials', 'Utilities', 'Real Estate', 'Materials', 'Communication Services'];

const Screener: React.FC = () => {
  const [activeTab, setActiveTab] = useState<ScreenerTab>('ticker');

  // Ticker state
  const [tickerInput, setTickerInput] = useState('');
  const [tickerReport, setTickerReport] = useState<TickerReport | null>(null);
  const [tickerLoading, setTickerLoading] = useState(false);
  const [tickerError, setTickerError] = useState('');
  const [tickerSection, setTickerSection] = useState<'overview' | 'financials' | 'filings' | 'analysts'>('overview');

  // Sector state
  const [selectedSector, setSelectedSector] = useState('Technology');
  const [sectorReport, setSectorReport] = useState<SectorReport | null>(null);
  const [sectorLoading, setSectorLoading] = useState(false);

  // Custom screen state
  const [screenCriteria, setScreenCriteria] = useState({ sector: '', peRatioMax: '', dividendYieldMin: '', marketCapMin: '', betaMax: '', priceMax: '' });
  const [screenResult, setScreenResult] = useState<ScreenResult | null>(null);
  const [screenLoading, setScreenLoading] = useState(false);

  // Saved reports
  const [savedReports, setSavedReports] = useState<SavedReportSummary[]>([]);
  const [savingReport, setSavingReport] = useState(false);

  useEffect(() => { if (activeTab === 'saved') fetchSavedReports(); }, [activeTab]);

  // ── API calls ──

  const fetchTickerReport = async () => {
    if (!tickerInput.trim()) return;
    setTickerLoading(true); setTickerError(''); setTickerReport(null);
    try {
      const res = await apiClient.get(`/v1/screener/ticker/${tickerInput.trim().toUpperCase()}`);
      setTickerReport(res.data);
    } catch { setTickerError('Failed to fetch ticker report. Check the symbol and try again.'); }
    setTickerLoading(false);
  };

  const fetchSectorReport = async () => {
    setSectorLoading(true); setSectorReport(null);
    try {
      const res = await apiClient.get(`/v1/screener/sector/${encodeURIComponent(selectedSector)}`);
      setSectorReport(res.data);
    } catch { /* silently fail */ }
    setSectorLoading(false);
  };

  const runCustomScreen = async () => {
    setScreenLoading(true); setScreenResult(null);
    const body: Record<string, unknown> = {};
    if (screenCriteria.sector) body.sector = screenCriteria.sector;
    if (screenCriteria.peRatioMax) body.peRatioMax = parseFloat(screenCriteria.peRatioMax);
    if (screenCriteria.dividendYieldMin) body.dividendYieldMin = parseFloat(screenCriteria.dividendYieldMin);
    if (screenCriteria.marketCapMin) body.marketCapMin = parseFloat(screenCriteria.marketCapMin);
    if (screenCriteria.betaMax) body.betaMax = parseFloat(screenCriteria.betaMax);
    if (screenCriteria.priceMax) body.priceMax = parseFloat(screenCriteria.priceMax);
    try {
      const res = await apiClient.post('/v1/screener/screen', body);
      setScreenResult(res.data);
    } catch { setScreenResult({ totalMatches: 0, stocks: [] }); }
    setScreenLoading(false);
  };

  const fetchSavedReports = async () => {
    try { const res = await apiClient.get('/v1/screener/reports'); setSavedReports(res.data); } catch { /* */ }
  };

  const handleSaveReport = async (reportType: string, target: string, reportData: unknown) => {
    setSavingReport(true);
    try {
      await apiClient.post('/v1/screener/reports', { reportType, target, reportData });
      alert('Report saved!');
    } catch { alert('Failed to save report'); }
    setSavingReport(false);
  };

  const handleDeleteSavedReport = async (id: number) => {
    if (!confirm('Delete this saved report?')) return;
    try { await apiClient.delete(`/v1/screener/reports/${id}`); fetchSavedReports(); } catch { /* */ }
  };

  // ── Render ──

  return (
    <div>
      <h1>Stock & Sector Screener</h1>

      {/* Tabs */}
      <div style={{ borderBottom: '1px solid #eee', display: 'flex', gap: '0.5rem', marginBottom: '1.5rem' }}>
        <button style={tabStyle(activeTab === 'ticker')} onClick={() => setActiveTab('ticker')}>Ticker Screener</button>
        <button style={tabStyle(activeTab === 'sector')} onClick={() => setActiveTab('sector')}>Sector Screener</button>
        <button style={tabStyle(activeTab === 'custom')} onClick={() => setActiveTab('custom')}>Custom Screen</button>
        <button style={tabStyle(activeTab === 'saved')} onClick={() => setActiveTab('saved')}>Saved Reports</button>
      </div>

      {/* ════════ TICKER TAB ════════ */}
      {activeTab === 'ticker' && (
        <div>
          <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.5rem', alignItems: 'center' }}>
            <input value={tickerInput} onChange={e => setTickerInput(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && fetchTickerReport()}
              placeholder="Enter ticker (e.g. AAPL)" style={{ ...inputStyle, maxWidth: 250 }} />
            <button onClick={fetchTickerReport} disabled={tickerLoading} style={btnStyle}>
              {tickerLoading ? 'Loading...' : 'Analyze'}
            </button>
            {tickerReport && (
              <button onClick={() => handleSaveReport('TICKER', tickerReport.ticker, tickerReport)}
                disabled={savingReport} style={{ ...btnStyle, background: '#2e7d32' }}>Save Report</button>
            )}
          </div>
          {tickerError && <div style={{ background: '#fee', color: '#c00', padding: '0.75rem', borderRadius: 4, marginBottom: '1rem' }}>{tickerError}</div>}

          {tickerReport && (
            <div>
              {/* Profile header */}
              <div style={{ ...cardStyle, marginBottom: '1rem', display: 'flex', gap: '1rem', alignItems: 'center' }}>
                {tickerReport.logo && <img src={tickerReport.logo} alt="" style={{ width: 48, height: 48, borderRadius: 8 }} />}
                <div style={{ flex: 1 }}>
                  <h2 style={{ margin: 0 }}>{tickerReport.name || tickerReport.ticker} <span style={{ fontSize: '0.9rem', color: '#888' }}>{tickerReport.ticker}</span></h2>
                  <span style={{ color: '#666', fontSize: '0.85rem' }}>{tickerReport.industry} &middot; {tickerReport.exchange} &middot; {tickerReport.country}</span>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <div style={{ fontSize: '1.5rem', fontWeight: 700 }}>{tickerReport.currency} {fmt(tickerReport.currentPrice)}</div>
                  <div style={{ color: pctColor(tickerReport.changePercent), fontWeight: 600 }}>
                    {tickerReport.change != null ? (tickerReport.change >= 0 ? '+' : '') + fmt(tickerReport.change) : ''}{' '}
                    ({tickerReport.changePercent != null ? (tickerReport.changePercent >= 0 ? '+' : '') + fmt(tickerReport.changePercent) + '%' : 'N/A'})
                  </div>
                </div>
              </div>

              {/* Sub-tabs */}
              <div style={{ borderBottom: '1px solid #eee', display: 'flex', gap: '0.5rem', marginBottom: '1rem' }}>
                <button style={tabStyle(tickerSection === 'overview')} onClick={() => setTickerSection('overview')}>Overview</button>
                <button style={tabStyle(tickerSection === 'financials')} onClick={() => setTickerSection('financials')}>Financials</button>
                <button style={tabStyle(tickerSection === 'filings')} onClick={() => setTickerSection('filings')}>SEC Filings</button>
                <button style={tabStyle(tickerSection === 'analysts')} onClick={() => setTickerSection('analysts')}>Analysts</button>
              </div>

              {/* Overview */}
              {tickerSection === 'overview' && (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '1rem' }}>
                  {[
                    { label: 'Market Cap', value: fmtB(tickerReport.marketCap) },
                    { label: 'P/E Ratio', value: fmt(tickerReport.peRatio) },
                    { label: 'EPS', value: fmt(tickerReport.eps) },
                    { label: 'Dividend Yield', value: tickerReport.dividendYield != null ? fmt(tickerReport.dividendYield) + '%' : 'N/A' },
                    { label: '52-Week High', value: fmt(tickerReport.weekHigh52) },
                    { label: '52-Week Low', value: fmt(tickerReport.weekLow52) },
                    { label: 'Beta', value: fmt(tickerReport.beta) },
                    { label: 'Prev Close', value: fmt(tickerReport.previousClose) },
                    { label: 'Revenue Growth (TTM)', value: tickerReport.revenueGrowthTTM != null ? fmt(tickerReport.revenueGrowthTTM) + '%' : 'N/A' },
                    { label: 'Earnings Growth (TTM)', value: tickerReport.earningsGrowthTTM != null ? fmt(tickerReport.earningsGrowthTTM) + '%' : 'N/A' },
                  ].map(({ label, value }) => (
                    <div key={label} style={{ ...cardStyle, textAlign: 'center', padding: '1rem' }}>
                      <div style={{ fontSize: '0.8rem', color: '#888', marginBottom: 4 }}>{label}</div>
                      <div style={{ fontSize: '1.1rem', fontWeight: 700 }}>{value}</div>
                    </div>
                  ))}
                </div>
              )}

              {/* Financials */}
              {tickerSection === 'financials' && (
                <div>
                  <h3>Annual Financial Statements</h3>
                  <div style={{ overflowX: 'auto' }}>
                    <table style={{ width: '100%', borderCollapse: 'collapse', background: '#fff', borderRadius: 8 }}>
                      <thead><tr style={{ background: '#f9f9f9', textAlign: 'left' }}>
                        <th style={{ padding: '0.5rem 0.75rem' }}>Year</th>
                        <th style={{ padding: '0.5rem 0.75rem' }}>Revenue</th>
                        <th style={{ padding: '0.5rem 0.75rem' }}>Net Income</th>
                        <th style={{ padding: '0.5rem 0.75rem' }}>Total Assets</th>
                        <th style={{ padding: '0.5rem 0.75rem' }}>Total Equity</th>
                        <th style={{ padding: '0.5rem 0.75rem' }}>Op. Cash Flow</th>
                      </tr></thead>
                      <tbody>
                        {tickerReport.annualFinancials?.length > 0 ? tickerReport.annualFinancials.map((f, i) => (
                          <tr key={i} style={{ borderTop: '1px solid #eee' }}>
                            <td style={{ padding: '0.5rem 0.75rem', fontWeight: 600 }}>{f.year}</td>
                            <td style={{ padding: '0.5rem 0.75rem' }}>{f.revenue != null ? '$' + fmtB(f.revenue / 1e6) : 'N/A'}</td>
                            <td style={{ padding: '0.5rem 0.75rem' }}>{f.netIncome != null ? '$' + fmtB(f.netIncome / 1e6) : 'N/A'}</td>
                            <td style={{ padding: '0.5rem 0.75rem' }}>{f.totalAssets != null ? '$' + fmtB(f.totalAssets / 1e6) : 'N/A'}</td>
                            <td style={{ padding: '0.5rem 0.75rem' }}>{f.totalEquity != null ? '$' + fmtB(f.totalEquity / 1e6) : 'N/A'}</td>
                            <td style={{ padding: '0.5rem 0.75rem' }}>{f.operatingCashFlow != null ? '$' + fmtB(f.operatingCashFlow / 1e6) : 'N/A'}</td>
                          </tr>
                        )) : <tr><td colSpan={6} style={{ padding: '2rem', textAlign: 'center', color: '#888' }}>No annual data available</td></tr>}
                      </tbody>
                    </table>
                  </div>

                  <h3 style={{ marginTop: '1.5rem' }}>Quarterly Earnings</h3>
                  <div style={{ overflowX: 'auto' }}>
                    <table style={{ width: '100%', borderCollapse: 'collapse', background: '#fff', borderRadius: 8 }}>
                      <thead><tr style={{ background: '#f9f9f9', textAlign: 'left' }}>
                        <th style={{ padding: '0.5rem 0.75rem' }}>Period</th>
                        <th style={{ padding: '0.5rem 0.75rem' }}>EPS Actual</th>
                        <th style={{ padding: '0.5rem 0.75rem' }}>EPS Estimate</th>
                        <th style={{ padding: '0.5rem 0.75rem' }}>Surprise</th>
                        <th style={{ padding: '0.5rem 0.75rem' }}>Surprise %</th>
                      </tr></thead>
                      <tbody>
                        {tickerReport.earnings?.length > 0 ? tickerReport.earnings.map((e, i) => (
                          <tr key={i} style={{ borderTop: '1px solid #eee' }}>
                            <td style={{ padding: '0.5rem 0.75rem', fontWeight: 600 }}>{e.period}</td>
                            <td style={{ padding: '0.5rem 0.75rem' }}>{fmt(e.actual, 4)}</td>
                            <td style={{ padding: '0.5rem 0.75rem' }}>{fmt(e.estimate, 4)}</td>
                            <td style={{ padding: '0.5rem 0.75rem', color: pctColor(e.surprise) }}>{e.surprise != null ? (e.surprise >= 0 ? '+' : '') + fmt(e.surprise, 4) : 'N/A'}</td>
                            <td style={{ padding: '0.5rem 0.75rem', color: pctColor(e.surprisePercent) }}>{e.surprisePercent != null ? (e.surprisePercent >= 0 ? '+' : '') + fmt(e.surprisePercent) + '%' : 'N/A'}</td>
                          </tr>
                        )) : <tr><td colSpan={5} style={{ padding: '2rem', textAlign: 'center', color: '#888' }}>No earnings data available</td></tr>}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}

              {/* SEC Filings */}
              {tickerSection === 'filings' && (
                <div style={{ overflowX: 'auto' }}>
                  <table style={{ width: '100%', borderCollapse: 'collapse', background: '#fff', borderRadius: 8 }}>
                    <thead><tr style={{ background: '#f9f9f9', textAlign: 'left' }}>
                      <th style={{ padding: '0.5rem 0.75rem' }}>Form</th>
                      <th style={{ padding: '0.5rem 0.75rem' }}>Filed Date</th>
                      <th style={{ padding: '0.5rem 0.75rem' }}>Accepted</th>
                      <th style={{ padding: '0.5rem 0.75rem' }}>Link</th>
                    </tr></thead>
                    <tbody>
                      {tickerReport.secFilings?.length > 0 ? tickerReport.secFilings.map((f, i) => (
                        <tr key={i} style={{ borderTop: '1px solid #eee' }}>
                          <td style={{ padding: '0.5rem 0.75rem' }}>
                            <span style={{ padding: '2px 8px', borderRadius: 3, fontSize: '0.8rem', fontWeight: 600,
                              background: f.form === '10-K' ? '#e3f2fd' : f.form === '10-Q' ? '#e8f5e9' : '#fff3e0',
                              color: f.form === '10-K' ? '#1565c0' : f.form === '10-Q' ? '#2e7d32' : '#e65100' }}>{f.form}</span>
                          </td>
                          <td style={{ padding: '0.5rem 0.75rem' }}>{f.filedDate}</td>
                          <td style={{ padding: '0.5rem 0.75rem' }}>{f.acceptedDate}</td>
                          <td style={{ padding: '0.5rem 0.75rem' }}>{f.reportUrl ? <a href={f.reportUrl} target="_blank" rel="noreferrer" style={{ color: '#1a1a2e' }}>View</a> : '-'}</td>
                        </tr>
                      )) : <tr><td colSpan={4} style={{ padding: '2rem', textAlign: 'center', color: '#888' }}>No SEC filings available</td></tr>}
                    </tbody>
                  </table>
                </div>
              )}

              {/* Analysts */}
              {tickerSection === 'analysts' && (
                <div>
                  {/* Price Target */}
                  {tickerReport.priceTarget && (
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '1rem', marginBottom: '1.5rem' }}>
                      {[
                        { label: 'Target High', value: fmt(tickerReport.priceTarget.targetHigh) },
                        { label: 'Target Low', value: fmt(tickerReport.priceTarget.targetLow) },
                        { label: 'Target Mean', value: fmt(tickerReport.priceTarget.targetMean) },
                        { label: 'Target Median', value: fmt(tickerReport.priceTarget.targetMedian) },
                      ].map(({ label, value }) => (
                        <div key={label} style={{ ...cardStyle, textAlign: 'center', padding: '1rem' }}>
                          <div style={{ fontSize: '0.8rem', color: '#888', marginBottom: 4 }}>{label}</div>
                          <div style={{ fontSize: '1.1rem', fontWeight: 700 }}>${value}</div>
                        </div>
                      ))}
                    </div>
                  )}

                  {/* Recommendations */}
                  <h3>Analyst Recommendations</h3>
                  <table style={{ width: '100%', borderCollapse: 'collapse', background: '#fff', borderRadius: 8 }}>
                    <thead><tr style={{ background: '#f9f9f9', textAlign: 'left' }}>
                      <th style={{ padding: '0.5rem 0.75rem' }}>Period</th>
                      <th style={{ padding: '0.5rem 0.75rem' }}>Strong Buy</th>
                      <th style={{ padding: '0.5rem 0.75rem' }}>Buy</th>
                      <th style={{ padding: '0.5rem 0.75rem' }}>Hold</th>
                      <th style={{ padding: '0.5rem 0.75rem' }}>Sell</th>
                      <th style={{ padding: '0.5rem 0.75rem' }}>Strong Sell</th>
                    </tr></thead>
                    <tbody>
                      {tickerReport.recommendations?.length > 0 ? tickerReport.recommendations.map((r, i) => (
                        <tr key={i} style={{ borderTop: '1px solid #eee' }}>
                          <td style={{ padding: '0.5rem 0.75rem', fontWeight: 600 }}>{r.period}</td>
                          <td style={{ padding: '0.5rem 0.75rem', color: '#2e7d32', fontWeight: 600 }}>{r.strongBuy}</td>
                          <td style={{ padding: '0.5rem 0.75rem', color: '#4caf50' }}>{r.buy}</td>
                          <td style={{ padding: '0.5rem 0.75rem', color: '#ff9800' }}>{r.hold}</td>
                          <td style={{ padding: '0.5rem 0.75rem', color: '#f44336' }}>{r.sell}</td>
                          <td style={{ padding: '0.5rem 0.75rem', color: '#c62828', fontWeight: 600 }}>{r.strongSell}</td>
                        </tr>
                      )) : <tr><td colSpan={6} style={{ padding: '2rem', textAlign: 'center', color: '#888' }}>No recommendations available</td></tr>}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}

          {!tickerReport && !tickerLoading && !tickerError && (
            <div style={{ ...cardStyle, textAlign: 'center', color: '#888', padding: '3rem' }}>
              Enter a stock ticker and click "Analyze" to generate a comprehensive report.
            </div>
          )}
        </div>
      )}

      {/* ════════ SECTOR TAB ════════ */}
      {activeTab === 'sector' && (
        <div>
          <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.5rem', alignItems: 'center' }}>
            <select value={selectedSector} onChange={e => setSelectedSector(e.target.value)}
              style={{ ...inputStyle, maxWidth: 280 }}>
              {SECTORS.map(s => <option key={s} value={s}>{s}</option>)}
            </select>
            <button onClick={fetchSectorReport} disabled={sectorLoading} style={btnStyle}>
              {sectorLoading ? 'Loading...' : 'Analyze Sector'}
            </button>
            {sectorReport && (
              <button onClick={() => handleSaveReport('SECTOR', sectorReport.sector, sectorReport)}
                disabled={savingReport} style={{ ...btnStyle, background: '#2e7d32' }}>Save Report</button>
            )}
          </div>

          {sectorReport && (
            <div>
              {/* Performance cards */}
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '1rem', marginBottom: '1.5rem' }}>
                <div style={{ ...cardStyle, textAlign: 'center', padding: '1rem' }}>
                  <div style={{ fontSize: '0.8rem', color: '#888', marginBottom: 4 }}>Sector Performance</div>
                  <div style={{ fontSize: '1.25rem', fontWeight: 700, color: pctColor(sectorReport.sectorPerformancePercent) }}>
                    {sectorReport.sectorPerformancePercent != null ? (sectorReport.sectorPerformancePercent >= 0 ? '+' : '') + fmt(sectorReport.sectorPerformancePercent) + '%' : 'N/A'}
                  </div>
                </div>
                <div style={{ ...cardStyle, textAlign: 'center', padding: '1rem' }}>
                  <div style={{ fontSize: '0.8rem', color: '#888', marginBottom: 4 }}>S&P 500</div>
                  <div style={{ fontSize: '1.25rem', fontWeight: 700, color: pctColor(sectorReport.spPerformancePercent) }}>
                    {sectorReport.spPerformancePercent != null ? (sectorReport.spPerformancePercent >= 0 ? '+' : '') + fmt(sectorReport.spPerformancePercent) + '%' : 'N/A'}
                  </div>
                </div>
                <div style={{ ...cardStyle, textAlign: 'center', padding: '1rem' }}>
                  <div style={{ fontSize: '0.8rem', color: '#888', marginBottom: 4 }}>Average P/E</div>
                  <div style={{ fontSize: '1.25rem', fontWeight: 700 }}>{fmt(sectorReport.averagePE)}</div>
                </div>
                <div style={{ ...cardStyle, textAlign: 'center', padding: '1rem' }}>
                  <div style={{ fontSize: '0.8rem', color: '#888', marginBottom: 4 }}>Avg Div Yield</div>
                  <div style={{ fontSize: '1.25rem', fontWeight: 700 }}>{sectorReport.averageDividendYield != null ? fmt(sectorReport.averageDividendYield) + '%' : 'N/A'}</div>
                </div>
              </div>

              {/* Rotation Signals */}
              {sectorReport.rotationSignals?.length > 0 && (
                <div style={{ marginBottom: '1.5rem' }}>
                  {sectorReport.rotationSignals.map((s, i) => (
                    <div key={i} style={{ ...cardStyle, padding: '0.75rem 1rem', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                      <span style={{ padding: '2px 8px', borderRadius: 3, fontSize: '0.8rem', fontWeight: 700,
                        background: s.signal === 'OUTPERFORMING' ? '#e8f5e9' : s.signal === 'UNDERPERFORMING' ? '#ffebee' : '#e3f2fd',
                        color: s.signal === 'OUTPERFORMING' ? '#2e7d32' : s.signal === 'UNDERPERFORMING' ? '#c62828' : '#1565c0' }}>{s.signal.replace('_', ' ')}</span>
                      <span style={{ color: '#555' }}>{s.description}</span>
                    </div>
                  ))}
                </div>
              )}

              {/* Top / Bottom performers */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
                {[
                  { title: 'Top Performers', data: sectorReport.topPerformers },
                  { title: 'Bottom Performers', data: sectorReport.bottomPerformers },
                ].map(({ title, data }) => (
                  <div key={title}>
                    <h3 style={{ marginTop: 0 }}>{title}</h3>
                    <table style={{ width: '100%', borderCollapse: 'collapse', background: '#fff', borderRadius: 8 }}>
                      <thead><tr style={{ background: '#f9f9f9', textAlign: 'left' }}>
                        <th style={{ padding: '0.5rem 0.75rem' }}>Ticker</th>
                        <th style={{ padding: '0.5rem 0.75rem' }}>Name</th>
                        <th style={{ padding: '0.5rem 0.75rem' }}>Price</th>
                        <th style={{ padding: '0.5rem 0.75rem' }}>Change</th>
                      </tr></thead>
                      <tbody>
                        {data?.map((s, i) => (
                          <tr key={i} style={{ borderTop: '1px solid #eee', cursor: 'pointer' }}
                            onClick={() => { setTickerInput(s.ticker); setActiveTab('ticker'); }}>
                            <td style={{ padding: '0.5rem 0.75rem', fontWeight: 600 }}>{s.ticker}</td>
                            <td style={{ padding: '0.5rem 0.75rem' }}>{s.name}</td>
                            <td style={{ padding: '0.5rem 0.75rem' }}>${fmt(s.currentPrice)}</td>
                            <td style={{ padding: '0.5rem 0.75rem', color: pctColor(s.changePercent), fontWeight: 600 }}>
                              {s.changePercent != null ? (s.changePercent >= 0 ? '+' : '') + fmt(s.changePercent) + '%' : 'N/A'}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                ))}
              </div>
            </div>
          )}

          {!sectorReport && !sectorLoading && (
            <div style={{ ...cardStyle, textAlign: 'center', color: '#888', padding: '3rem' }}>
              Select a sector and click "Analyze Sector" to see performance data.
            </div>
          )}
        </div>
      )}

      {/* ════════ CUSTOM SCREEN TAB ════════ */}
      {activeTab === 'custom' && (
        <div>
          <div style={{ ...cardStyle, marginBottom: '1.5rem' }}>
            <h3 style={{ marginTop: 0, marginBottom: '1rem' }}>Screening Criteria</h3>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '0.75rem', marginBottom: '1rem' }}>
              <div>
                <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Sector</label>
                <select value={screenCriteria.sector} onChange={e => setScreenCriteria({ ...screenCriteria, sector: e.target.value })} style={inputStyle}>
                  <option value="">All</option>
                  {SECTORS.map(s => <option key={s} value={s}>{s}</option>)}
                </select>
              </div>
              <div>
                <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Max P/E Ratio</label>
                <input type="number" step="any" placeholder="e.g. 15" value={screenCriteria.peRatioMax}
                  onChange={e => setScreenCriteria({ ...screenCriteria, peRatioMax: e.target.value })} style={inputStyle} />
              </div>
              <div>
                <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Min Dividend Yield (%)</label>
                <input type="number" step="any" placeholder="e.g. 3" value={screenCriteria.dividendYieldMin}
                  onChange={e => setScreenCriteria({ ...screenCriteria, dividendYieldMin: e.target.value })} style={inputStyle} />
              </div>
              <div>
                <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Min Market Cap (M$)</label>
                <input type="number" step="any" placeholder="e.g. 10000" value={screenCriteria.marketCapMin}
                  onChange={e => setScreenCriteria({ ...screenCriteria, marketCapMin: e.target.value })} style={inputStyle} />
              </div>
              <div>
                <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Max Beta</label>
                <input type="number" step="any" placeholder="e.g. 1.5" value={screenCriteria.betaMax}
                  onChange={e => setScreenCriteria({ ...screenCriteria, betaMax: e.target.value })} style={inputStyle} />
              </div>
              <div>
                <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Max Price ($)</label>
                <input type="number" step="any" placeholder="e.g. 100" value={screenCriteria.priceMax}
                  onChange={e => setScreenCriteria({ ...screenCriteria, priceMax: e.target.value })} style={inputStyle} />
              </div>
            </div>
            <button onClick={runCustomScreen} disabled={screenLoading} style={btnStyle}>
              {screenLoading ? 'Screening...' : 'Run Screen'}
            </button>
          </div>

          {screenResult && (
            <div>
              <p style={{ marginBottom: '0.75rem', color: '#666' }}>{screenResult.totalMatches} stock(s) matched your criteria</p>
              <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', background: '#fff', borderRadius: 8 }}>
                  <thead><tr style={{ background: '#f9f9f9', textAlign: 'left' }}>
                    <th style={{ padding: '0.5rem 0.75rem' }}>Ticker</th>
                    <th style={{ padding: '0.5rem 0.75rem' }}>Name</th>
                    <th style={{ padding: '0.5rem 0.75rem' }}>Sector</th>
                    <th style={{ padding: '0.5rem 0.75rem' }}>Price</th>
                    <th style={{ padding: '0.5rem 0.75rem' }}>Change</th>
                    <th style={{ padding: '0.5rem 0.75rem' }}>Mkt Cap</th>
                    <th style={{ padding: '0.5rem 0.75rem' }}>P/E</th>
                    <th style={{ padding: '0.5rem 0.75rem' }}>EPS</th>
                    <th style={{ padding: '0.5rem 0.75rem' }}>Div Yield</th>
                    <th style={{ padding: '0.5rem 0.75rem' }}>Beta</th>
                  </tr></thead>
                  <tbody>
                    {screenResult.stocks.length > 0 ? screenResult.stocks.map((s, i) => (
                      <tr key={i} style={{ borderTop: '1px solid #eee', cursor: 'pointer' }}
                        onClick={() => { setTickerInput(s.ticker); setActiveTab('ticker'); }}>
                        <td style={{ padding: '0.5rem 0.75rem', fontWeight: 600 }}>{s.ticker}</td>
                        <td style={{ padding: '0.5rem 0.75rem' }}>{s.name}</td>
                        <td style={{ padding: '0.5rem 0.75rem' }}>{s.sector}</td>
                        <td style={{ padding: '0.5rem 0.75rem' }}>${fmt(s.currentPrice)}</td>
                        <td style={{ padding: '0.5rem 0.75rem', color: pctColor(s.changePercent), fontWeight: 600 }}>
                          {s.changePercent != null ? (s.changePercent >= 0 ? '+' : '') + fmt(s.changePercent) + '%' : 'N/A'}
                        </td>
                        <td style={{ padding: '0.5rem 0.75rem' }}>{fmtB(s.marketCap)}</td>
                        <td style={{ padding: '0.5rem 0.75rem' }}>{fmt(s.peRatio)}</td>
                        <td style={{ padding: '0.5rem 0.75rem' }}>{fmt(s.eps)}</td>
                        <td style={{ padding: '0.5rem 0.75rem' }}>{s.dividendYield != null ? fmt(s.dividendYield) + '%' : 'N/A'}</td>
                        <td style={{ padding: '0.5rem 0.75rem' }}>{fmt(s.beta)}</td>
                      </tr>
                    )) : <tr><td colSpan={10} style={{ padding: '2rem', textAlign: 'center', color: '#888' }}>No stocks matched your criteria.</td></tr>}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      )}

      {/* ════════ SAVED REPORTS TAB ════════ */}
      {activeTab === 'saved' && (
        <div>
          {savedReports.length > 0 ? (
            <table style={{ width: '100%', borderCollapse: 'collapse', background: '#fff', borderRadius: 8, boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
              <thead><tr style={{ background: '#f9f9f9', textAlign: 'left' }}>
                <th style={{ padding: '0.5rem 0.75rem' }}>Type</th>
                <th style={{ padding: '0.5rem 0.75rem' }}>Target</th>
                <th style={{ padding: '0.5rem 0.75rem' }}>Saved At</th>
                <th style={{ padding: '0.5rem 0.75rem' }}>Actions</th>
              </tr></thead>
              <tbody>
                {savedReports.map((r) => (
                  <tr key={r.id} style={{ borderTop: '1px solid #eee' }}>
                    <td style={{ padding: '0.5rem 0.75rem' }}>
                      <span style={{ padding: '2px 8px', borderRadius: 3, fontSize: '0.8rem', fontWeight: 600,
                        background: r.reportType === 'TICKER' ? '#e3f2fd' : '#e8f5e9',
                        color: r.reportType === 'TICKER' ? '#1565c0' : '#2e7d32' }}>{r.reportType}</span>
                    </td>
                    <td style={{ padding: '0.5rem 0.75rem', fontWeight: 600 }}>{r.target}</td>
                    <td style={{ padding: '0.5rem 0.75rem' }}>{new Date(r.createdAt).toLocaleString()}</td>
                    <td style={{ padding: '0.5rem 0.75rem' }}>
                      <button onClick={() => { setTickerInput(r.target); setActiveTab(r.reportType === 'TICKER' ? 'ticker' : 'sector'); }}
                        style={{ marginRight: 4, padding: '2px 8px', cursor: 'pointer', border: '1px solid #ccc', borderRadius: 3, background: '#fff' }}>View Live</button>
                      <button onClick={() => handleDeleteSavedReport(r.id)}
                        style={{ padding: '2px 8px', cursor: 'pointer', border: '1px solid #c00', borderRadius: 3, background: '#fff', color: '#c00' }}>Delete</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <div style={{ ...cardStyle, textAlign: 'center', color: '#888', padding: '3rem' }}>
              No saved reports yet. Use the Ticker or Sector screener and click "Save Report" to save one.
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default Screener;
