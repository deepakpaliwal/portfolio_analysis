import React, { useEffect, useState } from 'react';
import apiClient from '../api/client';

/* ── Types ── */

interface TickerConfig {
  id: number;
  ticker: string;
  tickerName: string | null;
  enabled: boolean;
  lastSyncDate: string | null;
  recordCount: number;
  lastRunAt: string | null;
  lastRunStatus: string | null;
  errorMessage: string | null;
  createdAt: string;
}

interface ScheduleConfig {
  cron_expression: string;
  scheduler_enabled: string;
  rate_limit_ms: string;
  scheduler_running: string;
}

/* ── Styles ── */

const card: React.CSSProperties = {
  background: '#fff',
  padding: '1.5rem',
  borderRadius: 8,
  boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
};

const btnPrimary: React.CSSProperties = {
  padding: '0.5rem 1.25rem',
  background: '#1976d2',
  color: '#fff',
  border: 'none',
  borderRadius: 4,
  cursor: 'pointer',
  fontWeight: 600,
  fontSize: '0.85rem',
};

const btnSuccess: React.CSSProperties = {
  ...btnPrimary,
  background: '#43a047',
};

const btnDanger: React.CSSProperties = {
  ...btnPrimary,
  background: '#e53935',
};

const btnWarning: React.CSSProperties = {
  ...btnPrimary,
  background: '#ff9800',
};

const inputStyle: React.CSSProperties = {
  padding: '0.5rem',
  borderRadius: 4,
  border: '1px solid #ccc',
  fontSize: '0.9rem',
};

/* ── Component ── */

const BatchManagement: React.FC = () => {
  const [tickers, setTickers] = useState<TickerConfig[]>([]);
  const [scheduleConfig, setScheduleConfig] = useState<ScheduleConfig | null>(null);
  const [newTicker, setNewTicker] = useState('');
  const [newTickerName, setNewTickerName] = useState('');
  const [loading, setLoading] = useState(false);
  const [running, setRunning] = useState(false);
  const [runningSingle, setRunningSingle] = useState<string | null>(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [activeTab, setActiveTab] = useState<'tickers' | 'schedule'>('tickers');

  // Editable schedule fields
  const [editCron, setEditCron] = useState('');
  const [editRateLimit, setEditRateLimit] = useState('');
  const [editEnabled, setEditEnabled] = useState(false);

  const showMessage = (msg: string, isError = false) => {
    if (isError) { setError(msg); setSuccess(''); }
    else { setSuccess(msg); setError(''); }
    setTimeout(() => { setError(''); setSuccess(''); }, 5000);
  };

  const fetchTickers = async () => {
    try {
      const res = await apiClient.get('/v1/batch/tickers');
      setTickers(res.data);
    } catch {
      showMessage('Failed to load tickers', true);
    }
  };

  const fetchScheduleConfig = async () => {
    try {
      const res = await apiClient.get('/v1/batch/schedule');
      setScheduleConfig(res.data);
      setEditCron(res.data.cron_expression || '');
      setEditRateLimit(res.data.rate_limit_ms || '3000');
      setEditEnabled(res.data.scheduler_enabled === 'true');
    } catch {
      showMessage('Failed to load schedule config', true);
    }
  };

  useEffect(() => {
    setLoading(true);
    Promise.all([fetchTickers(), fetchScheduleConfig()]).finally(() => setLoading(false));
  }, []);

  const addTicker = async () => {
    const ticker = newTicker.trim().toUpperCase();
    if (!ticker) return;
    try {
      await apiClient.post('/v1/batch/tickers', { ticker, tickerName: newTickerName.trim() || null });
      setNewTicker('');
      setNewTickerName('');
      showMessage(`Added ticker ${ticker}`);
      fetchTickers();
    } catch (err: unknown) {
      const axiosErr = err as { response?: { status?: number } };
      if (axiosErr.response?.status === 409) {
        showMessage(`Ticker ${ticker} already exists`, true);
      } else {
        showMessage('Failed to add ticker', true);
      }
    }
  };

  const toggleTicker = async (config: TickerConfig) => {
    try {
      await apiClient.put(`/v1/batch/tickers/${config.id}`, { enabled: !config.enabled });
      fetchTickers();
    } catch {
      showMessage('Failed to update ticker', true);
    }
  };

  const deleteTicker = async (config: TickerConfig) => {
    if (!confirm(`Remove ${config.ticker} from batch config?`)) return;
    try {
      await apiClient.delete(`/v1/batch/tickers/${config.id}`);
      showMessage(`Removed ${config.ticker}`);
      fetchTickers();
    } catch {
      showMessage('Failed to delete ticker', true);
    }
  };

  const runAll = async () => {
    setRunning(true);
    setError('');
    try {
      const res = await apiClient.post('/v1/batch/run', null, { timeout: 300_000 });
      const summary = res.data._summary;
      showMessage(`Batch complete: ${summary.success} ok, ${summary.errors} errors, ${summary.totalNewRecords} new records`);
      fetchTickers();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Batch run failed';
      showMessage(msg, true);
    } finally {
      setRunning(false);
    }
  };

  const runSingle = async (ticker: string) => {
    setRunningSingle(ticker);
    try {
      const res = await apiClient.post(`/v1/batch/run/${ticker}`, null, { timeout: 60_000 });
      showMessage(`${ticker}: ${res.data.newRecords} new records fetched`);
      fetchTickers();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : `Failed to fetch ${ticker}`;
      showMessage(msg, true);
    } finally {
      setRunningSingle(null);
    }
  };

  const saveScheduleConfig = async () => {
    try {
      const body: Record<string, string> = {};
      if (editCron !== scheduleConfig?.cron_expression) body.cron_expression = editCron;
      if (editRateLimit !== scheduleConfig?.rate_limit_ms) body.rate_limit_ms = editRateLimit;
      const enabledStr = editEnabled ? 'true' : 'false';
      if (enabledStr !== scheduleConfig?.scheduler_enabled) body.scheduler_enabled = enabledStr;

      if (Object.keys(body).length === 0) {
        showMessage('No changes to save');
        return;
      }

      const res = await apiClient.put('/v1/batch/schedule', body);
      setScheduleConfig(res.data);
      showMessage('Schedule config updated');
    } catch {
      showMessage('Failed to update schedule config', true);
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

  const statusBadge = (status: string | null): React.CSSProperties => {
    const color = status === 'OK' ? '#43a047' : status === 'ERROR' ? '#e53935' : '#888';
    return {
      padding: '2px 8px',
      borderRadius: 12,
      fontSize: '0.75rem',
      background: color + '20',
      color,
      fontWeight: 600,
    };
  };

  if (loading) {
    return <div style={{ textAlign: 'center', padding: '3rem', color: '#888' }}>Loading batch configuration...</div>;
  }

  return (
    <div>
      <h1>Batch Price Management</h1>

      {error && <div style={{ background: '#ffebee', color: '#c62828', padding: '0.75rem 1rem', borderRadius: 4, marginBottom: '1rem' }}>{error}</div>}
      {success && <div style={{ background: '#e8f5e9', color: '#2e7d32', padding: '0.75rem 1rem', borderRadius: 4, marginBottom: '1rem' }}>{success}</div>}

      {/* Tabs */}
      <div style={{ borderBottom: '1px solid #e0e0e0', marginBottom: '1.5rem', display: 'flex', gap: 0 }}>
        <button style={tabStyle('tickers')} onClick={() => setActiveTab('tickers')}>Ticker Management</button>
        <button style={tabStyle('schedule')} onClick={() => setActiveTab('schedule')}>Schedule Config</button>
      </div>

      {/* ── Ticker Management Tab ── */}
      {activeTab === 'tickers' && (
        <>
          {/* Add Ticker Form */}
          <div style={{ ...card, marginBottom: '1.5rem', display: 'flex', gap: '0.75rem', alignItems: 'flex-end', flexWrap: 'wrap' }}>
            <div>
              <label style={{ display: 'block', fontSize: '0.8rem', color: '#666', marginBottom: 4 }}>Ticker Symbol</label>
              <input
                type="text"
                value={newTicker}
                onChange={e => setNewTicker(e.target.value.toUpperCase())}
                placeholder="e.g. AAPL"
                style={{ ...inputStyle, width: 120 }}
                onKeyDown={e => e.key === 'Enter' && addTicker()}
              />
            </div>
            <div>
              <label style={{ display: 'block', fontSize: '0.8rem', color: '#666', marginBottom: 4 }}>Company Name (optional)</label>
              <input
                type="text"
                value={newTickerName}
                onChange={e => setNewTickerName(e.target.value)}
                placeholder="e.g. Apple Inc."
                style={{ ...inputStyle, width: 200 }}
                onKeyDown={e => e.key === 'Enter' && addTicker()}
              />
            </div>
            <button onClick={addTicker} style={btnPrimary} disabled={!newTicker.trim()}>
              Add Ticker
            </button>
            <div style={{ marginLeft: 'auto' }}>
              <button onClick={runAll} disabled={running || tickers.length === 0} style={btnWarning}>
                {running ? 'Running All...' : 'Run All Tickers'}
              </button>
            </div>
          </div>

          {/* Tickers Table */}
          <div style={card}>
            {tickers.length === 0 ? (
              <p style={{ color: '#888', textAlign: 'center', padding: '2rem' }}>
                No tickers configured. Add tickers above to start fetching price data.
              </p>
            ) : (
              <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                  <tr style={{ borderBottom: '2px solid #e0e0e0' }}>
                    <th style={{ textAlign: 'left', padding: '0.6rem 0.5rem', color: '#666', fontSize: '0.8rem' }}>Ticker</th>
                    <th style={{ textAlign: 'left', padding: '0.6rem 0.5rem', color: '#666', fontSize: '0.8rem' }}>Name</th>
                    <th style={{ textAlign: 'center', padding: '0.6rem 0.5rem', color: '#666', fontSize: '0.8rem' }}>Enabled</th>
                    <th style={{ textAlign: 'right', padding: '0.6rem 0.5rem', color: '#666', fontSize: '0.8rem' }}>Records</th>
                    <th style={{ textAlign: 'left', padding: '0.6rem 0.5rem', color: '#666', fontSize: '0.8rem' }}>Last Sync</th>
                    <th style={{ textAlign: 'left', padding: '0.6rem 0.5rem', color: '#666', fontSize: '0.8rem' }}>Status</th>
                    <th style={{ textAlign: 'left', padding: '0.6rem 0.5rem', color: '#666', fontSize: '0.8rem' }}>Last Run</th>
                    <th style={{ textAlign: 'center', padding: '0.6rem 0.5rem', color: '#666', fontSize: '0.8rem' }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {tickers.map(t => (
                    <tr key={t.id} style={{ borderBottom: '1px solid #f0f0f0' }}>
                      <td style={{ padding: '0.6rem 0.5rem', fontWeight: 600 }}>{t.ticker}</td>
                      <td style={{ padding: '0.6rem 0.5rem', color: '#555' }}>{t.tickerName || '--'}</td>
                      <td style={{ padding: '0.6rem 0.5rem', textAlign: 'center' }}>
                        <button
                          onClick={() => toggleTicker(t)}
                          style={{
                            background: 'none', border: 'none', cursor: 'pointer', fontSize: '1.2rem',
                            color: t.enabled ? '#43a047' : '#bbb',
                          }}
                          title={t.enabled ? 'Click to disable' : 'Click to enable'}
                        >
                          {t.enabled ? '\u2713' : '\u2717'}
                        </button>
                      </td>
                      <td style={{ padding: '0.6rem 0.5rem', textAlign: 'right', fontWeight: 600 }}>
                        {t.recordCount.toLocaleString()}
                      </td>
                      <td style={{ padding: '0.6rem 0.5rem', fontSize: '0.85rem', color: '#555' }}>
                        {t.lastSyncDate || 'Never'}
                      </td>
                      <td style={{ padding: '0.6rem 0.5rem' }}>
                        {t.lastRunStatus ? (
                          <span style={statusBadge(t.lastRunStatus)}>{t.lastRunStatus}</span>
                        ) : '--'}
                        {t.errorMessage && (
                          <div style={{ fontSize: '0.7rem', color: '#e53935', marginTop: 2, maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}
                               title={t.errorMessage}>
                            {t.errorMessage}
                          </div>
                        )}
                      </td>
                      <td style={{ padding: '0.6rem 0.5rem', fontSize: '0.8rem', color: '#888' }}>
                        {t.lastRunAt ? new Date(t.lastRunAt).toLocaleString() : 'Never'}
                      </td>
                      <td style={{ padding: '0.6rem 0.5rem', textAlign: 'center' }}>
                        <div style={{ display: 'flex', gap: 4, justifyContent: 'center' }}>
                          <button
                            onClick={() => runSingle(t.ticker)}
                            disabled={runningSingle === t.ticker}
                            style={{ ...btnSuccess, padding: '0.3rem 0.6rem', fontSize: '0.75rem' }}
                          >
                            {runningSingle === t.ticker ? 'Fetching...' : 'Fetch'}
                          </button>
                          <button
                            onClick={() => deleteTicker(t)}
                            style={{ ...btnDanger, padding: '0.3rem 0.6rem', fontSize: '0.75rem' }}
                          >
                            Remove
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </>
      )}

      {/* ── Schedule Config Tab ── */}
      {activeTab === 'schedule' && (
        <div style={{ ...card, maxWidth: 600 }}>
          <h3 style={{ marginTop: 0 }}>Scheduler Configuration</h3>

          <div style={{ marginBottom: '1.25rem' }}>
            <label style={{ display: 'block', fontSize: '0.8rem', color: '#666', marginBottom: 4 }}>Scheduler Enabled</label>
            <label style={{ display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer' }}>
              <input
                type="checkbox"
                checked={editEnabled}
                onChange={e => setEditEnabled(e.target.checked)}
                style={{ width: 18, height: 18 }}
              />
              <span style={{ fontSize: '0.9rem' }}>
                {editEnabled ? 'Enabled — scheduler will auto-run' : 'Disabled — manual runs only'}
              </span>
            </label>
            {scheduleConfig && (
              <div style={{ fontSize: '0.8rem', color: '#888', marginTop: 4 }}>
                Currently: {scheduleConfig.scheduler_running === 'true' ? 'Running' : 'Stopped'}
              </div>
            )}
          </div>

          <div style={{ marginBottom: '1.25rem' }}>
            <label style={{ display: 'block', fontSize: '0.8rem', color: '#666', marginBottom: 4 }}>
              Cron Expression
            </label>
            <input
              type="text"
              value={editCron}
              onChange={e => setEditCron(e.target.value)}
              style={{ ...inputStyle, width: '100%' }}
              placeholder="0 0 6 * * *"
            />
            <div style={{ fontSize: '0.75rem', color: '#888', marginTop: 4 }}>
              Spring cron format: sec min hour day month weekday. Default: "0 0 6 * * *" (daily at 6 AM)
            </div>
          </div>

          <div style={{ marginBottom: '1.5rem' }}>
            <label style={{ display: 'block', fontSize: '0.8rem', color: '#666', marginBottom: 4 }}>
              Rate Limit (ms between requests)
            </label>
            <input
              type="number"
              value={editRateLimit}
              onChange={e => setEditRateLimit(e.target.value)}
              style={{ ...inputStyle, width: 150 }}
              min={1000}
              step={500}
            />
            <div style={{ fontSize: '0.75rem', color: '#888', marginTop: 4 }}>
              Delay between Yahoo Finance API calls. Recommended: 3000ms or higher to avoid rate limiting.
            </div>
          </div>

          <button onClick={saveScheduleConfig} style={btnPrimary}>
            Save Configuration
          </button>
        </div>
      )}
    </div>
  );
};

export default BatchManagement;
