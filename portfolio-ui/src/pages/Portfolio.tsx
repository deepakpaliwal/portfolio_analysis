import React, { useEffect, useState, useRef } from 'react';
import { useAppDispatch, useAppSelector } from '../store/store';
import {
  setPortfolios,
  addPortfolio,
  removePortfolio,
  selectPortfolio,
  setLoading,
  setError,
} from '../store/slices/portfolioSlice';
import apiClient from '../api/client';

type Tab = 'holdings' | 'transactions' | 'allocation';

interface HoldingData {
  id: number;
  assetType: string;
  ticker: string;
  name: string;
  quantity: number;
  purchasePrice: number;
  purchaseDate: string;
  currency: string;
  sector: string;
  category: string;
}

interface TransactionData {
  id: number;
  holdingId: number;
  holdingTicker: string;
  transactionType: string;
  quantity: number;
  price: number;
  fees: number;
  executedAt: string;
  notes: string;
}

interface AllocationEntry {
  label: string;
  value: number;
  percentage: number;
}

interface AllocationData {
  byAssetType: AllocationEntry[];
  bySector: AllocationEntry[];
  byCurrency: AllocationEntry[];
  totalCostBasis: number;
}

const ASSET_TYPES = ['STOCK', 'BOND', 'OPTION', 'CASH', 'REAL_ESTATE', 'RETIREMENT_FUND', 'CRYPTOCURRENCY', 'REIT', 'ETF', 'MUTUAL_FUND'];

const Portfolio: React.FC = () => {
  const dispatch = useAppDispatch();
  const { portfolios, selectedPortfolioId, loading, error } = useAppSelector(
    (state) => state.portfolio
  );

  // Portfolio create form
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [newName, setNewName] = useState('');
  const [newDescription, setNewDescription] = useState('');
  const [newCurrency, setNewCurrency] = useState('USD');
  const [formError, setFormError] = useState('');
  const [creating, setCreating] = useState(false);

  // Tabs
  const [activeTab, setActiveTab] = useState<Tab>('holdings');

  // Holdings
  const [holdings, setHoldings] = useState<HoldingData[]>([]);
  const [showHoldingForm, setShowHoldingForm] = useState(false);
  const [editingHolding, setEditingHolding] = useState<HoldingData | null>(null);
  const [holdingForm, setHoldingForm] = useState({
    assetType: 'STOCK', ticker: '', name: '', quantity: '', purchasePrice: '',
    purchaseDate: '', currency: 'USD', sector: '', category: '',
  });
  const [holdingFormError, setHoldingFormError] = useState('');

  // Transactions
  const [transactions, setTransactions] = useState<TransactionData[]>([]);

  // Allocation
  const [allocation, setAllocation] = useState<AllocationData | null>(null);

  // CSV import
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [importMsg, setImportMsg] = useState('');

  const selectedPortfolio = portfolios.find((p) => p.id === selectedPortfolioId) || null;

  useEffect(() => { fetchPortfolios(); }, []);

  useEffect(() => {
    if (selectedPortfolioId) {
      fetchHoldings(selectedPortfolioId);
      setActiveTab('holdings');
      setAllocation(null);
      setTransactions([]);
    } else {
      setHoldings([]);
      setTransactions([]);
      setAllocation(null);
    }
  }, [selectedPortfolioId]);

  useEffect(() => {
    if (!selectedPortfolioId) return;
    if (activeTab === 'transactions') fetchTransactions(selectedPortfolioId);
    if (activeTab === 'allocation') fetchAllocation(selectedPortfolioId);
  }, [activeTab, selectedPortfolioId]);

  const fetchPortfolios = async () => {
    dispatch(setLoading(true));
    try {
      const response = await apiClient.get('/v1/portfolios');
      dispatch(setPortfolios(response.data));
    } catch { dispatch(setError('Failed to load portfolios')); }
  };

  const fetchHoldings = async (pid: number) => {
    try {
      const res = await apiClient.get(`/v1/portfolios/${pid}/holdings`);
      setHoldings(res.data);
    } catch { /* holdings come from portfolio too */ }
  };

  const fetchTransactions = async (pid: number) => {
    try {
      const res = await apiClient.get(`/v1/portfolios/${pid}/transactions`);
      setTransactions(res.data);
    } catch { setTransactions([]); }
  };

  const fetchAllocation = async (pid: number) => {
    try {
      const res = await apiClient.get(`/v1/portfolios/${pid}/allocation`);
      setAllocation(res.data);
    } catch { setAllocation(null); }
  };

  // Portfolio CRUD
  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError('');
    if (!newName.trim()) { setFormError('Portfolio name is required'); return; }
    setCreating(true);
    try {
      const response = await apiClient.post('/v1/portfolios', {
        name: newName.trim(), description: newDescription.trim() || null, baseCurrency: newCurrency,
      });
      dispatch(addPortfolio(response.data));
      dispatch(selectPortfolio(response.data.id));
      setNewName(''); setNewDescription(''); setNewCurrency('USD'); setShowCreateForm(false);
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setFormError(axiosErr.response?.data?.message || 'Failed to create portfolio');
    } finally { setCreating(false); }
  };

  const handleDeletePortfolio = async (id: number) => {
    if (!confirm('Delete this portfolio and all its holdings?')) return;
    try {
      await apiClient.delete(`/v1/portfolios/${id}`);
      dispatch(removePortfolio(id));
    } catch { dispatch(setError('Failed to delete portfolio')); }
  };

  // Holding CRUD
  const resetHoldingForm = () => {
    setHoldingForm({ assetType: 'STOCK', ticker: '', name: '', quantity: '', purchasePrice: '',
      purchaseDate: '', currency: 'USD', sector: '', category: '' });
    setEditingHolding(null);
    setShowHoldingForm(false);
    setHoldingFormError('');
  };

  const openEditHolding = (h: HoldingData) => {
    setEditingHolding(h);
    setHoldingForm({
      assetType: h.assetType, ticker: h.ticker, name: h.name || '',
      quantity: String(h.quantity), purchasePrice: String(h.purchasePrice),
      purchaseDate: h.purchaseDate, currency: h.currency, sector: h.sector || '', category: h.category || '',
    });
    setShowHoldingForm(true);
    setHoldingFormError('');
  };

  const handleSaveHolding = async (e: React.FormEvent) => {
    e.preventDefault();
    setHoldingFormError('');
    if (!selectedPortfolioId) return;

    const body = {
      assetType: holdingForm.assetType,
      ticker: holdingForm.ticker.trim(),
      name: holdingForm.name.trim() || null,
      quantity: parseFloat(holdingForm.quantity),
      purchasePrice: parseFloat(holdingForm.purchasePrice),
      purchaseDate: holdingForm.purchaseDate,
      currency: holdingForm.currency,
      sector: holdingForm.sector.trim() || null,
      category: holdingForm.category.trim() || null,
    };

    try {
      if (editingHolding) {
        await apiClient.put(`/v1/portfolios/${selectedPortfolioId}/holdings/${editingHolding.id}`, body);
      } else {
        await apiClient.post(`/v1/portfolios/${selectedPortfolioId}/holdings`, body);
      }
      resetHoldingForm();
      fetchHoldings(selectedPortfolioId);
      fetchPortfolios();
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string; fieldErrors?: Record<string, string> } } };
      const fe = axiosErr.response?.data?.fieldErrors;
      setHoldingFormError(fe ? Object.values(fe).join(', ') : (axiosErr.response?.data?.message || 'Failed to save holding'));
    }
  };

  const handleDeleteHolding = async (holdingId: number) => {
    if (!selectedPortfolioId || !confirm('Delete this holding?')) return;
    try {
      await apiClient.delete(`/v1/portfolios/${selectedPortfolioId}/holdings/${holdingId}`);
      fetchHoldings(selectedPortfolioId);
      fetchPortfolios();
    } catch { dispatch(setError('Failed to delete holding')); }
  };

  // CSV import
  const handleCsvImport = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file || !selectedPortfolioId) return;
    setImportMsg('');
    const formData = new FormData();
    formData.append('file', file);
    try {
      const res = await apiClient.post(`/v1/portfolios/${selectedPortfolioId}/holdings/import`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setImportMsg(`Imported ${res.data.imported} holdings`);
      fetchHoldings(selectedPortfolioId);
      fetchPortfolios();
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setImportMsg(axiosErr.response?.data?.message || 'Import failed');
    }
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  const inputStyle = { width: '100%', padding: '0.5rem', border: '1px solid #ccc', borderRadius: 4, boxSizing: 'border-box' as const };
  const btnStyle = { padding: '0.5rem 1rem', background: '#1a1a2e', color: '#fff', border: 'none', borderRadius: 4, cursor: 'pointer' as const };
  const tabStyle = (active: boolean) => ({
    padding: '0.5rem 1rem', border: 'none', cursor: 'pointer' as const, fontWeight: 600,
    borderBottom: active ? '2px solid #1a1a2e' : '2px solid transparent',
    background: 'transparent', color: active ? '#1a1a2e' : '#888',
  });

  return (
    <div>
      <h1>Portfolio</h1>

      {error && <div style={{ background: '#fee', color: '#c00', padding: '0.75rem', borderRadius: 4, marginBottom: '1rem' }}>{error}</div>}

      {/* Portfolio selector + actions */}
      <div style={{ display: 'flex', gap: '0.75rem', marginBottom: '1rem', alignItems: 'center', flexWrap: 'wrap' }}>
        <select value={selectedPortfolioId ?? ''} onChange={(e) => { const v = e.target.value; dispatch(selectPortfolio(v ? Number(v) : null)); }}
          style={{ padding: '0.5rem', borderRadius: 4, border: '1px solid #ccc', minWidth: 200 }}>
          <option value="">Select Portfolio...</option>
          {portfolios.map((p) => <option key={p.id} value={p.id}>{p.name} ({p.holdingCount} holdings)</option>)}
        </select>
        <button onClick={() => setShowCreateForm(!showCreateForm)} style={btnStyle}>
          {showCreateForm ? 'Cancel' : '+ New Portfolio'}
        </button>
        {loading && <span style={{ color: '#888' }}>Loading...</span>}
      </div>

      {/* Create Portfolio Form */}
      {showCreateForm && (
        <div style={{ background: '#fff', padding: '1.5rem', borderRadius: 8, boxShadow: '0 1px 3px rgba(0,0,0,0.1)', marginBottom: '1rem', maxWidth: 500 }}>
          <h3 style={{ marginTop: 0, marginBottom: '1rem' }}>Create New Portfolio</h3>
          <form onSubmit={handleCreate}>
            {formError && <div style={{ background: '#fee', color: '#c00', padding: '0.5rem', borderRadius: 4, marginBottom: '0.75rem', fontSize: '0.9rem' }}>{formError}</div>}
            <div style={{ marginBottom: '0.75rem' }}>
              <label style={{ display: 'block', marginBottom: 4, fontWeight: 600 }}>Name *</label>
              <input type="text" value={newName} onChange={(e) => setNewName(e.target.value)} placeholder="e.g. Retirement Fund" maxLength={200} required style={inputStyle} />
            </div>
            <div style={{ marginBottom: '0.75rem' }}>
              <label style={{ display: 'block', marginBottom: 4, fontWeight: 600 }}>Description</label>
              <input type="text" value={newDescription} onChange={(e) => setNewDescription(e.target.value)} placeholder="Optional" maxLength={500} style={inputStyle} />
            </div>
            <div style={{ marginBottom: '1rem' }}>
              <label style={{ display: 'block', marginBottom: 4, fontWeight: 600 }}>Base Currency</label>
              <select value={newCurrency} onChange={(e) => setNewCurrency(e.target.value)} style={{ ...inputStyle, width: 'auto', minWidth: 100 }}>
                {['USD','EUR','GBP','JPY','CAD','AUD','CHF','INR'].map(c => <option key={c} value={c}>{c}</option>)}
              </select>
            </div>
            <button type="submit" disabled={creating} style={{ ...btnStyle, fontWeight: 600 }}>{creating ? 'Creating...' : 'Create Portfolio'}</button>
          </form>
        </div>
      )}

      {/* Selected Portfolio */}
      {selectedPortfolio ? (
        <div style={{ background: '#fff', borderRadius: 8, boxShadow: '0 1px 3px rgba(0,0,0,0.1)', overflow: 'hidden' }}>
          {/* Header */}
          <div style={{ padding: '1rem', borderBottom: '1px solid #eee', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div>
              <h2 style={{ margin: 0 }}>{selectedPortfolio.name}</h2>
              {selectedPortfolio.description && <p style={{ margin: '0.25rem 0 0', color: '#666' }}>{selectedPortfolio.description}</p>}
              <span style={{ fontSize: '0.85rem', color: '#888' }}>Currency: {selectedPortfolio.baseCurrency} &middot; {selectedPortfolio.holdingCount} holding(s)</span>
            </div>
            <button onClick={() => handleDeletePortfolio(selectedPortfolio.id)} style={{ ...btnStyle, background: '#c00', fontSize: '0.85rem' }}>Delete Portfolio</button>
          </div>

          {/* Tabs */}
          <div style={{ borderBottom: '1px solid #eee', display: 'flex', gap: '0.5rem', padding: '0 1rem' }}>
            <button style={tabStyle(activeTab === 'holdings')} onClick={() => setActiveTab('holdings')}>Holdings</button>
            <button style={tabStyle(activeTab === 'transactions')} onClick={() => setActiveTab('transactions')}>Transactions</button>
            <button style={tabStyle(activeTab === 'allocation')} onClick={() => setActiveTab('allocation')}>Allocation</button>
          </div>

          {/* Holdings Tab */}
          {activeTab === 'holdings' && (
            <div style={{ padding: '1rem' }}>
              <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1rem', flexWrap: 'wrap' }}>
                <button onClick={() => { resetHoldingForm(); setShowHoldingForm(true); }} style={btnStyle}>+ Add Holding</button>
                <button onClick={() => fileInputRef.current?.click()} style={{ ...btnStyle, background: '#2e7d32' }}>Import CSV</button>
                <input ref={fileInputRef} type="file" accept=".csv" onChange={handleCsvImport} style={{ display: 'none' }} />
                {importMsg && <span style={{ alignSelf: 'center', color: importMsg.startsWith('Import') ? '#c00' : '#2e7d32', fontSize: '0.85rem' }}>{importMsg}</span>}
              </div>

              {/* Holding Form */}
              {showHoldingForm && (
                <div style={{ background: '#f9f9f9', padding: '1rem', borderRadius: 6, marginBottom: '1rem' }}>
                  <h4 style={{ marginTop: 0 }}>{editingHolding ? 'Edit Holding' : 'Add Holding'}</h4>
                  <form onSubmit={handleSaveHolding}>
                    {holdingFormError && <div style={{ background: '#fee', color: '#c00', padding: '0.5rem', borderRadius: 4, marginBottom: '0.5rem', fontSize: '0.85rem' }}>{holdingFormError}</div>}
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '0.5rem', marginBottom: '0.5rem' }}>
                      <div>
                        <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Type *</label>
                        <select value={holdingForm.assetType} onChange={e => setHoldingForm({...holdingForm, assetType: e.target.value})} style={inputStyle}>
                          {ASSET_TYPES.map(t => <option key={t} value={t}>{t.replace('_',' ')}</option>)}
                        </select>
                      </div>
                      <div>
                        <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Ticker *</label>
                        <input value={holdingForm.ticker} onChange={e => setHoldingForm({...holdingForm, ticker: e.target.value})} required style={inputStyle} placeholder="AAPL" />
                      </div>
                      <div>
                        <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Name</label>
                        <input value={holdingForm.name} onChange={e => setHoldingForm({...holdingForm, name: e.target.value})} style={inputStyle} placeholder="Apple Inc." />
                      </div>
                    </div>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr 1fr', gap: '0.5rem', marginBottom: '0.5rem' }}>
                      <div>
                        <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Quantity *</label>
                        <input type="number" step="any" value={holdingForm.quantity} onChange={e => setHoldingForm({...holdingForm, quantity: e.target.value})} required style={inputStyle} />
                      </div>
                      <div>
                        <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Purchase Price *</label>
                        <input type="number" step="any" value={holdingForm.purchasePrice} onChange={e => setHoldingForm({...holdingForm, purchasePrice: e.target.value})} required style={inputStyle} />
                      </div>
                      <div>
                        <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Purchase Date *</label>
                        <input type="date" value={holdingForm.purchaseDate} onChange={e => setHoldingForm({...holdingForm, purchaseDate: e.target.value})} required style={inputStyle} />
                      </div>
                      <div>
                        <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Currency</label>
                        <input value={holdingForm.currency} onChange={e => setHoldingForm({...holdingForm, currency: e.target.value})} maxLength={3} style={inputStyle} />
                      </div>
                    </div>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem', marginBottom: '0.75rem' }}>
                      <div>
                        <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Sector</label>
                        <input value={holdingForm.sector} onChange={e => setHoldingForm({...holdingForm, sector: e.target.value})} style={inputStyle} placeholder="Technology" />
                      </div>
                      <div>
                        <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Category</label>
                        <input value={holdingForm.category} onChange={e => setHoldingForm({...holdingForm, category: e.target.value})} style={inputStyle} placeholder="Growth" />
                      </div>
                    </div>
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                      <button type="submit" style={{ ...btnStyle, fontWeight: 600 }}>{editingHolding ? 'Update' : 'Add'}</button>
                      <button type="button" onClick={resetHoldingForm} style={{ ...btnStyle, background: '#888' }}>Cancel</button>
                    </div>
                  </form>
                </div>
              )}

              {/* Holdings Table */}
              <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                  <tr style={{ background: '#f9f9f9', textAlign: 'left' }}>
                    <th style={{ padding: '0.5rem 0.75rem' }}>Ticker</th>
                    <th style={{ padding: '0.5rem 0.75rem' }}>Name</th>
                    <th style={{ padding: '0.5rem 0.75rem' }}>Type</th>
                    <th style={{ padding: '0.5rem 0.75rem' }}>Qty</th>
                    <th style={{ padding: '0.5rem 0.75rem' }}>Cost</th>
                    <th style={{ padding: '0.5rem 0.75rem' }}>Total Cost</th>
                    <th style={{ padding: '0.5rem 0.75rem' }}>Category</th>
                    <th style={{ padding: '0.5rem 0.75rem' }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {holdings.length > 0 ? holdings.map((h) => (
                    <tr key={h.id} style={{ borderTop: '1px solid #eee' }}>
                      <td style={{ padding: '0.5rem 0.75rem', fontWeight: 600 }}>{h.ticker}</td>
                      <td style={{ padding: '0.5rem 0.75rem' }}>{h.name}</td>
                      <td style={{ padding: '0.5rem 0.75rem' }}>{h.assetType.replace('_',' ')}</td>
                      <td style={{ padding: '0.5rem 0.75rem' }}>{h.quantity}</td>
                      <td style={{ padding: '0.5rem 0.75rem' }}>${Number(h.purchasePrice).toFixed(2)}</td>
                      <td style={{ padding: '0.5rem 0.75rem' }}>${(h.quantity * h.purchasePrice).toFixed(2)}</td>
                      <td style={{ padding: '0.5rem 0.75rem' }}>{h.category || '-'}</td>
                      <td style={{ padding: '0.5rem 0.75rem' }}>
                        <button onClick={() => openEditHolding(h)} style={{ marginRight: 4, padding: '2px 8px', cursor: 'pointer', border: '1px solid #ccc', borderRadius: 3, background: '#fff' }}>Edit</button>
                        <button onClick={() => handleDeleteHolding(h.id)} style={{ padding: '2px 8px', cursor: 'pointer', border: '1px solid #c00', borderRadius: 3, background: '#fff', color: '#c00' }}>Del</button>
                      </td>
                    </tr>
                  )) : (
                    <tr><td colSpan={8} style={{ padding: '2rem', textAlign: 'center', color: '#888' }}>No holdings yet.</td></tr>
                  )}
                </tbody>
              </table>
            </div>
          )}

          {/* Transactions Tab */}
          {activeTab === 'transactions' && (
            <div style={{ padding: '1rem' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                  <tr style={{ background: '#f9f9f9', textAlign: 'left' }}>
                    <th style={{ padding: '0.5rem 0.75rem' }}>Date</th>
                    <th style={{ padding: '0.5rem 0.75rem' }}>Ticker</th>
                    <th style={{ padding: '0.5rem 0.75rem' }}>Type</th>
                    <th style={{ padding: '0.5rem 0.75rem' }}>Qty</th>
                    <th style={{ padding: '0.5rem 0.75rem' }}>Price</th>
                    <th style={{ padding: '0.5rem 0.75rem' }}>Fees</th>
                    <th style={{ padding: '0.5rem 0.75rem' }}>Notes</th>
                  </tr>
                </thead>
                <tbody>
                  {transactions.length > 0 ? transactions.map((tx) => (
                    <tr key={tx.id} style={{ borderTop: '1px solid #eee' }}>
                      <td style={{ padding: '0.5rem 0.75rem' }}>{new Date(tx.executedAt).toLocaleDateString()}</td>
                      <td style={{ padding: '0.5rem 0.75rem', fontWeight: 600 }}>{tx.holdingTicker}</td>
                      <td style={{ padding: '0.5rem 0.75rem' }}>
                        <span style={{ padding: '2px 6px', borderRadius: 3, fontSize: '0.8rem', fontWeight: 600,
                          background: tx.transactionType === 'BUY' ? '#e8f5e9' : tx.transactionType === 'SELL' ? '#ffebee' : '#e3f2fd',
                          color: tx.transactionType === 'BUY' ? '#2e7d32' : tx.transactionType === 'SELL' ? '#c62828' : '#1565c0',
                        }}>{tx.transactionType}</span>
                      </td>
                      <td style={{ padding: '0.5rem 0.75rem' }}>{tx.quantity}</td>
                      <td style={{ padding: '0.5rem 0.75rem' }}>${Number(tx.price).toFixed(2)}</td>
                      <td style={{ padding: '0.5rem 0.75rem' }}>{tx.fees ? `$${Number(tx.fees).toFixed(2)}` : '-'}</td>
                      <td style={{ padding: '0.5rem 0.75rem', color: '#666' }}>{tx.notes || '-'}</td>
                    </tr>
                  )) : (
                    <tr><td colSpan={7} style={{ padding: '2rem', textAlign: 'center', color: '#888' }}>No transactions recorded.</td></tr>
                  )}
                </tbody>
              </table>
            </div>
          )}

          {/* Allocation Tab */}
          {activeTab === 'allocation' && (
            <div style={{ padding: '1rem' }}>
              {allocation ? (
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '1.5rem' }}>
                  {[
                    { title: 'By Asset Type', data: allocation.byAssetType },
                    { title: 'By Sector', data: allocation.bySector },
                    { title: 'By Currency', data: allocation.byCurrency },
                  ].map(({ title, data }) => (
                    <div key={title}>
                      <h4 style={{ marginTop: 0, marginBottom: '0.5rem' }}>{title}</h4>
                      {data.length > 0 ? data.map((entry) => (
                        <div key={entry.label} style={{ display: 'flex', alignItems: 'center', marginBottom: '0.4rem' }}>
                          <div style={{ width: 120, fontSize: '0.85rem', fontWeight: 600 }}>{entry.label.replace('_',' ')}</div>
                          <div style={{ flex: 1, background: '#eee', borderRadius: 4, height: 18, marginRight: 8, overflow: 'hidden' }}>
                            <div style={{ width: `${entry.percentage}%`, background: '#1a1a2e', height: '100%', borderRadius: 4, minWidth: entry.percentage > 0 ? 4 : 0 }} />
                          </div>
                          <span style={{ fontSize: '0.85rem', minWidth: 50, textAlign: 'right' }}>{entry.percentage}%</span>
                        </div>
                      )) : <span style={{ color: '#888', fontSize: '0.85rem' }}>No data</span>}
                    </div>
                  ))}
                  <div style={{ gridColumn: '1 / -1', borderTop: '1px solid #eee', paddingTop: '0.75rem' }}>
                    <strong>Total Cost Basis:</strong> ${Number(allocation.totalCostBasis).toLocaleString(undefined, { minimumFractionDigits: 2 })}
                  </div>
                </div>
              ) : (
                <div style={{ textAlign: 'center', color: '#888', padding: '2rem' }}>Loading allocation data...</div>
              )}
            </div>
          )}
        </div>
      ) : (
        !showCreateForm && (
          <div style={{ background: '#fff', borderRadius: 8, boxShadow: '0 1px 3px rgba(0,0,0,0.1)', padding: '3rem', textAlign: 'center', color: '#888' }}>
            {portfolios.length === 0 ? 'No portfolios yet. Click "+ New Portfolio" to create one.' : 'Select a portfolio to view its details.'}
          </div>
        )
      )}
    </div>
  );
};

export default Portfolio;
