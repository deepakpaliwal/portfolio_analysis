import React, { useEffect, useState } from 'react';
import apiClient from '../api/client';

type AssetClass = 'EQUITY' | 'CRYPTO' | 'OPTION';

interface TickerConfig {
  id: number;
  ticker: string;
  tickerName: string | null;
  assetClass: AssetClass;
  enabled: boolean;
  lastSyncDate: string | null;
  recordCount: number;
  lastRunAt: string | null;
  lastRunStatus: string | null;
  errorMessage: string | null;
}

interface ScheduleConfig {
  cron_expression: string;
  scheduler_enabled: string;
  rate_limit_ms: string;
}

interface MonitoringData {
  totalConfiguredTickers: number;
  enabledTickers: number;
  disabledTickers: number;
  totalBatchRecordCount: number;
  totalMarketHistoryRecords: number;
  lastBatchRunAt: string | null;
  latestTradeDate: string | null;
  assetClassCounts: Record<string, number>;
  statusCounts: Record<string, number>;
}

const card: React.CSSProperties = { background: '#fff', border: '1px solid #E2E8F0', borderRadius: 12, padding: '1rem' };
const input: React.CSSProperties = { padding: '0.5rem', border: '1px solid #CBD5E1', borderRadius: 8 };
const btn: React.CSSProperties = { padding: '0.5rem 0.85rem', border: 'none', borderRadius: 8, cursor: 'pointer', background: '#2563EB', color: '#fff' };

const BatchManagement: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'tickers' | 'schedule' | 'monitoring'>('tickers');
  const [tickers, setTickers] = useState<TickerConfig[]>([]);
  const [schedule, setSchedule] = useState<ScheduleConfig | null>(null);
  const [monitoring, setMonitoring] = useState<MonitoringData | null>(null);
  const [newTicker, setNewTicker] = useState('');
  const [newTickerName, setNewTickerName] = useState('');
  const [newAssetClass, setNewAssetClass] = useState<AssetClass>('EQUITY');
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const loadAll = async () => {
    const [t, s, m] = await Promise.all([
      apiClient.get('/v1/batch/tickers'),
      apiClient.get('/v1/batch/schedule'),
      apiClient.get('/v1/batch/monitoring'),
    ]);
    setTickers(t.data);
    setSchedule(s.data);
    setMonitoring(m.data);
  };

  useEffect(() => {
    setLoading(true);
    loadAll().catch(() => setError('Failed to load batch data')).finally(() => setLoading(false));
  }, []);

  const addTicker = async () => {
    if (!newTicker.trim()) return;
    try {
      await apiClient.post('/v1/batch/tickers', {
        ticker: newTicker.trim().toUpperCase(),
        tickerName: newTickerName.trim() || null,
        assetClass: newAssetClass,
      });
      setMessage('Ticker added');
      setNewTicker('');
      setNewTickerName('');
      await loadAll();
    } catch {
      setError('Failed to add ticker');
    }
  };

  const toggleTicker = async (t: TickerConfig) => {
    await apiClient.put(`/v1/batch/tickers/${t.id}`, { enabled: !t.enabled });
    await loadAll();
  };

  const runAll = async () => {
    const res = await apiClient.post('/v1/batch/run');
    const summary = res.data._summary;
    setMessage(`Run complete: ${summary.success} success, ${summary.errors} errors, ${summary.totalNewRecords} new records`);
    await loadAll();
  };

  const saveSchedule = async () => {
    if (!schedule) return;
    await apiClient.put('/v1/batch/schedule', schedule);
    setMessage('Schedule updated');
    await loadAll();
  };

  if (loading) return <div style={{ padding: '2rem' }}>Loading batch configuration...</div>;

  return (
    <div>
      <h1>Batch Price Management</h1>
      {message && <div style={{ ...card, background: '#ECFDF5', color: '#065F46', marginBottom: 12 }}>{message}</div>}
      {error && <div style={{ ...card, background: '#FEF2F2', color: '#991B1B', marginBottom: 12 }}>{error}</div>}

      <div style={{ display: 'flex', gap: 10, marginBottom: 16 }}>
        {(['tickers', 'schedule', 'monitoring'] as const).map((tab) => (
          <button key={tab} style={{ ...btn, background: activeTab === tab ? '#1D4ED8' : '#64748B' }} onClick={() => setActiveTab(tab)}>
            {tab[0].toUpperCase() + tab.slice(1)}
          </button>
        ))}
      </div>

      {activeTab === 'tickers' && (
        <>
          <div style={{ ...card, display: 'flex', gap: 10, flexWrap: 'wrap', marginBottom: 16 }}>
            <input style={input} value={newTicker} onChange={(e) => setNewTicker(e.target.value.toUpperCase())} placeholder="Ticker (AAPL/BTC-USD/OPTION SYMBOL)" />
            <input style={input} value={newTickerName} onChange={(e) => setNewTickerName(e.target.value)} placeholder="Name" />
            <select style={input} value={newAssetClass} onChange={(e) => setNewAssetClass(e.target.value as AssetClass)}>
              <option value="EQUITY">EQUITY</option>
              <option value="CRYPTO">CRYPTO</option>
              <option value="OPTION">OPTION</option>
            </select>
            <button style={btn} onClick={addTicker}>Add</button>
            <button style={{ ...btn, background: '#B45309' }} onClick={runAll}>Run Batch</button>
          </div>

          <div style={card}>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead><tr><th>Ticker</th><th>Asset</th><th>Records</th><th>Watermark</th><th>Status</th><th>Action</th></tr></thead>
              <tbody>
                {tickers.map((t) => (
                  <tr key={t.id}>
                    <td>{t.ticker}</td><td>{t.assetClass}</td><td>{t.recordCount}</td><td>{t.lastSyncDate ?? '--'}</td><td>{t.lastRunStatus ?? 'NEVER'}</td>
                    <td><button style={{ ...btn, background: t.enabled ? '#059669' : '#6B7280' }} onClick={() => toggleTicker(t)}>{t.enabled ? 'Disable' : 'Enable'}</button></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}

      {activeTab === 'schedule' && schedule && (
        <div style={card}>
          <div style={{ display: 'grid', gridTemplateColumns: '180px 1fr', gap: 10, alignItems: 'center' }}>
            <label>Cron Expression</label>
            <input style={input} value={schedule.cron_expression} onChange={(e) => setSchedule({ ...schedule, cron_expression: e.target.value })} />
            <label>Rate Limit (ms)</label>
            <input style={input} value={schedule.rate_limit_ms} onChange={(e) => setSchedule({ ...schedule, rate_limit_ms: e.target.value })} />
            <label>Scheduler Enabled</label>
            <input type="checkbox" checked={schedule.scheduler_enabled === 'true'} onChange={(e) => setSchedule({ ...schedule, scheduler_enabled: String(e.target.checked) })} />
          </div>
          <button style={{ ...btn, marginTop: 12 }} onClick={saveSchedule}>Save Schedule</button>
        </div>
      )}

      {activeTab === 'monitoring' && monitoring && (
        <div style={{ display: 'grid', gap: 12, gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))' }}>
          <div style={card}><strong>Total Configured</strong><div>{monitoring.totalConfiguredTickers}</div></div>
          <div style={card}><strong>Enabled / Disabled</strong><div>{monitoring.enabledTickers} / {monitoring.disabledTickers}</div></div>
          <div style={card}><strong>Batch Records</strong><div>{monitoring.totalBatchRecordCount}</div></div>
          <div style={card}><strong>Unified History Records</strong><div>{monitoring.totalMarketHistoryRecords}</div></div>
          <div style={card}><strong>Last Batch Run</strong><div>{monitoring.lastBatchRunAt ?? '--'}</div></div>
          <div style={card}><strong>Latest Trade Date</strong><div>{monitoring.latestTradeDate ?? '--'}</div></div>
          <div style={card}><strong>By Asset Class</strong><pre>{JSON.stringify(monitoring.assetClassCounts, null, 2)}</pre></div>
          <div style={card}><strong>Status Counts</strong><pre>{JSON.stringify(monitoring.statusCounts, null, 2)}</pre></div>
        </div>
      )}
    </div>
  );
};

export default BatchManagement;
